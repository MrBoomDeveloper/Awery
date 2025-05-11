package com.mrboomdev.awery.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

@Composable
fun ExpandableText(
	modifier: Modifier = Modifier,
	state: ExpandableTextState = rememberExpandableTextState(),
	isSelectable: Boolean = false,
	text: String,
	color: Color = Color.Unspecified,
	fontSize: TextUnit = TextUnit.Unspecified,
	fontStyle: FontStyle? = null,
	fontWeight: FontWeight? = null,
	fontFamily: FontFamily? = null,
	letterSpacing: TextUnit = TextUnit.Unspecified,
	textDecoration: TextDecoration? = null,
	textAlign: TextAlign? = null,
	lineHeight: TextUnit = TextUnit.Unspecified,
	overflow: TextOverflow = TextOverflow.Ellipsis,
	softWrap: Boolean = true,
	maxLines: Int,
	minLines: Int = 1,
	style: TextStyle = LocalTextStyle.current,
	toggleButton: @Composable (ExpandableTextState) -> Unit
) {
	@Composable fun content() {
		Text(
			modifier = Modifier.animateContentSize(),
			text = text,
			color = color,
			fontSize = fontSize,
			fontStyle = fontStyle,
			fontWeight = fontWeight,
			fontFamily = fontFamily,
			letterSpacing = letterSpacing,
			textDecoration = textDecoration,
			textAlign = textAlign,
			lineHeight = lineHeight,
			overflow = overflow,
			softWrap = softWrap,
			maxLines = if(state.isExpanded) Int.MAX_VALUE else maxLines,
			minLines = minLines,
			style = style,
			onTextLayout = {
				state.textLayoutResult = it
			}
		)
	}
	
	Column(modifier = modifier) { 
		if(isSelectable) {
			SelectionContainer { 
				content()
			}
		} else {
			content()
		}
		
		if(state.isExpandable) {
			toggleButton(state)
		}
	}
}

class ExpandableTextState(isExpanded: Boolean = false) {
	internal var textLayoutResult by mutableStateOf<TextLayoutResult?>(null)
	var isExpanded by mutableStateOf(isExpanded)
	
	val isExpandable = true/*by derivedStateOf { 
		textLayoutResult?.didOverflowHeight ?: false || isExpanded
	}*/
	
	fun toggle() {
		isExpanded = !isExpanded
	}
	
	companion object {
		val Saver = Saver<ExpandableTextState, Boolean>(
			save = { it.isExpanded },
			restore = { ExpandableTextState(it) }
		)
	}
}

@Composable
fun rememberExpandableTextState(): ExpandableTextState {
	return rememberSaveable(saver = ExpandableTextState.Saver) { ExpandableTextState() }
}