package com.huanchengfly.tieba.post.ui.page.settings.ai

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.ai.AiSummaryClient
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.ui.common.prefs.PrefsScreen
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.EditTextPref
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.TextPref
import com.huanchengfly.tieba.post.ui.page.settings.LeadingIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.AvatarIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import androidx.compose.material.rememberScaffoldState
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.utils.appPreferences
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Destination
@Composable
fun AiSettingsPage(
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isTesting by remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()

    MyScaffold(
        scaffoldState = scaffoldState,
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_ai_settings),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h6,
                    )
                },
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                }
            )
        },
    ) { paddingValues ->
        PrefsScreen(
            dataStore = context.dataStore,
            dividerThickness = 0.dp,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            prefsItem {
                EditTextPref(
                    key = "ai_api_base_url",
                    title = stringResource(id = R.string.title_ai_api_url),
                    summary = stringResource(id = R.string.summary_ai_api_url),
                    dialogTitle = stringResource(id = R.string.title_ai_api_url),
                    defaultValue = "",
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Link,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            prefsItem {
                EditTextPref(
                    key = "ai_api_key",
                    title = stringResource(id = R.string.title_ai_api_key),
                    summary = stringResource(id = R.string.summary_ai_api_key),
                    dialogTitle = stringResource(id = R.string.title_ai_api_key),
                    defaultValue = "",
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Key,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            prefsItem {
                EditTextPref(
                    key = "ai_model_name",
                    title = stringResource(id = R.string.title_ai_model),
                    summary = stringResource(id = R.string.summary_ai_model),
                    dialogTitle = stringResource(id = R.string.title_ai_model),
                    defaultValue = "deepseek-chat",
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.SmartToy,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            prefsItem {
                TextPref(
                    title = if (isTesting) {
                        stringResource(id = R.string.title_ai_test_connection) + "..."
                    } else {
                        stringResource(id = R.string.title_ai_test_connection)
                    },
                    enabled = !isTesting,
                    darkenOnDisable = true,
                    onClick = {
                        val prefs = context.appPreferences
                        val baseUrl = prefs.aiApiBaseUrl
                        val apiKey = prefs.aiApiKey
                        val model = prefs.aiModelName ?: "deepseek-chat"
                        if (baseUrl.isNullOrBlank() || apiKey.isNullOrBlank()) {
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    context.getString(R.string.ai_summary_not_configured)
                                )
                            }
                            return@TextPref
                        }
                        isTesting = true
                        coroutineScope.launch {
                            val result = AiSummaryClient.testConnection(baseUrl, apiKey, model)
                            isTesting = false
                            result.fold(
                                onSuccess = {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        context.getString(R.string.toast_ai_test_success)
                                    )
                                },
                                onFailure = {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        context.getString(
                                            R.string.toast_ai_test_failure,
                                            it.message ?: "Unknown"
                                        )
                                    )
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
