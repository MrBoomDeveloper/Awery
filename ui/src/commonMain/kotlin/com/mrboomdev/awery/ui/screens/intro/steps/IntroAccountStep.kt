package com.mrboomdev.awery.ui.screens.intro.steps

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.http
import com.mrboomdev.awery.core.utils.launchTrying
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.data.settings.collectAsState
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_account_outlined
import com.mrboomdev.awery.resources.username
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.toast
import com.mrboomdev.awery.ui.screens.intro.IntroDefaults
import com.mrboomdev.awery.ui.screens.intro.IntroDslWrapper
import com.mrboomdev.awery.ui.screens.intro.setAlignment
import com.mrboomdev.awery.ui.utils.classify
import com.mrboomdev.awery.ui.utils.times
import com.mrboomdev.navigation.core.plusAssign
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private class IntroAccountViewModel: ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    fun login(
        serverInstance: String,
        username: String, 
        password: String,
        onSuccess: (token: String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) = auth(
        method = "signin",
        serverInstance = serverInstance,
        username = username,
        password = password,
        onSuccess = onSuccess,
        onFailure = onFailure
    )

    fun register(
        serverInstance: String,
        username: String,
        password: String,
        onSuccess: (token: String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) = auth(
        method = "signup",
        serverInstance = serverInstance,
        username = username,
        password = password,
        onSuccess = onSuccess,
        onFailure = onFailure
    )

    private fun auth(
        serverInstance: String,
        username: String,
        password: String,
        method: String,
        onSuccess: (token: String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launchTrying(Dispatchers.Default, onCatch = {
            onFailure(it)

            viewModelScope.launch {
                _isLoading.emit(false)
            }
        }) {
            _isLoading.emit(true)

            val response = Awery.http.post(
                "$serverInstance/api/auth/$method"
            ) {
                setBody(JsonObject(mapOf(
                    "username" to JsonPrimitive(username),
                    "password" to JsonPrimitive(password)
                )).toString())
            }
            
            val token = Json.parseToJsonElement(
                response.bodyAsText()
            ).jsonObject["token"]!!.jsonPrimitive.content

            AwerySettings.username.set(username)
            AwerySettings.aweryServerToken.set(token)

            onSuccess(token)
        }
    }
}

@Serializable
data object IntroAccountStep: IntroStep {
    @Composable
    override fun Content(
        singleStep: Boolean, 
        contentPadding: PaddingValues
    ) {
        val autofillManager = LocalAutofillManager.current
        val toaster = LocalToaster.current
        val navigation = IntroDefaults.navigation.current()
        val (loginFocusRequester, passwordFocusRequester) = FocusRequester.createRefs()
        var username by rememberSaveable { mutableStateOf("") }
        val password = rememberTextFieldState()
        val coroutineScope = rememberCoroutineScope()
        
        val viewModel = viewModel { IntroAccountViewModel() }
        val isLoading by viewModel.isLoading.collectAsState()
        
        if(isLoading) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                CircularProgressIndicator()
            }
        }
        
        fun onFailure(throwable: Throwable) {
            toaster.toast(
                title = "Failed to login!",
                duration = 7_500,
                
                message = when(throwable) {
                    is ResponseException -> runBlocking { throwable.response.bodyAsText() }
                    else -> throwable.classify().message
                }
            )
        }
        
        IntroDslWrapper(contentPadding) {
            setAlignment(Alignment.CenterHorizontally)
            iconSize = 128.dp
            actionScale = 1.1f
            icon = { painterResource(Res.drawable.ic_account_outlined) }
            title = { "Awery ID" }
            description = { "You may use it to save data in the cloud and watch media together!" }
            
            secondaryContent = {
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = AwerySettings.aweryServerInstance.collectAsState().value,
                    onValueChange = { runBlocking { AwerySettings.aweryServerInstance.set(it) } },
                    label = { Text("Server instance") },
                    singleLine = true,

                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),

                    keyboardActions = KeyboardActions {
                        loginFocusRequester.requestFocus()
                    }
                )

                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(loginFocusRequester)
                        .semantics { contentType = ContentType.Username },
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(Res.string.username)) },
                    singleLine = true,
                    
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    
                    keyboardActions = KeyboardActions {
                        passwordFocusRequester.requestFocus()
                    }
                )
                
                Spacer(Modifier.height(8.dp))

                OutlinedSecureTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    state = password,
                    label = { Text("Password") }
                )
                
                Spacer(Modifier.weight(1f))
                
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = ButtonDefaults.TextButtonContentPadding * 2.5f,
                    onClick = {
                        navigation.push(IntroUserStep)
                        autofillManager?.cancel()
                    }
                ) {
                    Text("Continue without online account")
                }
            }
            
            addAction {
                text = "Login"
                onClick = {
                    viewModel.login(
                        serverInstance = AwerySettings.aweryServerInstance.value,
                        username = username,
                        password = password.text.toString(),
                        onFailure = ::onFailure,
                        
                        onSuccess = {
                            autofillManager?.commit()
                            toaster.toast("Logged in successfully!")

                            coroutineScope.launch(Dispatchers.Main) {
                                navigation += IntroUserStep
                            }
                        }
                    )
                }
            }

            addAction {
                text = "Register"
                onClick = {
                    viewModel.register(
                        serverInstance = AwerySettings.aweryServerInstance.value,
                        username = username,
                        password = password.text.toString(),
                        onFailure = ::onFailure,

                        onSuccess = {
                            autofillManager?.commit()
                            toaster.toast("Registered successfully!")

                            coroutineScope.launch(Dispatchers.Main) {
                                navigation += IntroUserStep
                            }
                        }
                    )
                }
            }
        }
    }
}