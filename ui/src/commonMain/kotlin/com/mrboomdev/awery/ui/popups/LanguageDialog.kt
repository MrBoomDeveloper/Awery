package com.mrboomdev.awery.ui.popups

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.resources.AweryLocales
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.cancel
import com.mrboomdev.awery.resources.confirm
import com.mrboomdev.awery.resources.select_language
import com.mrboomdev.awery.ui.components.AlertDialog
import org.jetbrains.compose.resources.stringResource
import java.util.Locale

internal expect fun getCurrentAppLocales(): List<Locale>
internal expect fun setAppLocale(locale: Locale)

private fun getCurrentAvailableLocale(availableLocales: List<Locale>): Locale {
    val currents = getCurrentAppLocales()

    val (selectedLocale, availableVariants) = currents.map { current ->
        current to availableLocales.filter {
            it.language == current.language
        }
    }.let {
        if(it.isEmpty()) {
            return Locale.ENGLISH
        } else it[0]
    }

    if(availableVariants.size == 1) {
        return availableVariants[0]
    }

    val selected = availableVariants.filter {
        it.country == selectedLocale.country
    }

    return if(selected.isNotEmpty()) {
        selected[0]
    } else availableVariants[0]
}

@Composable
fun LanguageDialog(
    onDismissRequest: () -> Unit
) {
    val locales = remember {
        AweryLocales.map {
            @Suppress("DEPRECATION") when(it) {
                "en" -> Locale("en", "US")
                "en-rIN" -> Locale("en", "IN")
                "zh-rCN" -> Locale.SIMPLIFIED_CHINESE
                else -> Locale.forLanguageTag(it)
            }
        }
            .groupBy { it.language }
            .map { (_, locales) ->
                if(locales.size > 1) {
                    locales.map { locale ->
                        if(locale.displayCountry.isBlank()) {
                            return@map locale to locale.displayLanguage.replaceFirstChar { it.uppercaseChar() }
                        }

                        locale to locale.displayLanguage.replaceFirstChar { it.uppercaseChar() } +
                                " (${locale.displayCountry.replaceFirstChar { it.uppercaseChar() }})"
                    }
                } else locales.map { locale -> locale to locale.displayLanguage.replaceFirstChar { it.uppercaseChar() } }
            }
            .flatten()
            .sortedBy { it.second }
    }

    var newLocale by remember {
        mutableStateOf(getCurrentAvailableLocale(locales.map { it.first }))
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        contentPadding = PaddingValues(vertical = 6.dp),
        title = { Text(stringResource(Res.string.select_language)) },

        confirmButton = {
            TextButton(
                onClick = {
                    setAppLocale(newLocale)
                    onDismissRequest()
                }
            ) {
                Text(stringResource(Res.string.confirm))
            }
        },

        cancelButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(Res.string.cancel))
            }
        }
    ) {
        LazyColumn(Modifier.fillMaxWidth()) {
            items(
                items = locales,
                key = { it }
            ) { (locale, name) ->
                val interactionSource = remember { MutableInteractionSource() }

                Row(
                    modifier = Modifier
                        .clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current,
                            role = Role.RadioButton,
                            onClick = { newLocale = locale }
                        )
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 1.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = name
                    )

                    RadioButton(
                        interactionSource = interactionSource,
                        selected = locale == newLocale,
                        onClick = { newLocale = locale }
                    )
                }
            }
        }
    }
}