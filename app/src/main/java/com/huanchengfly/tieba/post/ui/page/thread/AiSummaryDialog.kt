package com.huanchengfly.tieba.post.ui.page.thread

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.Button
import com.huanchengfly.tieba.post.utils.TiebaUtil

@Composable
fun AiSummaryDialog(
    summaryState: SummaryState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    if (summaryState == SummaryState.Idle) return

    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = summaryState !is SummaryState.Loading,
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ExtendedTheme.colors.windowBackground,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.title_ai_summary),
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = ExtendedTheme.colors.text,
                )

                when (summaryState) {
                    is SummaryState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                CircularProgressIndicator(
                                    color = ExtendedTheme.colors.primary,
                                )
                                Text(
                                    text = stringResource(id = R.string.ai_summary_loading),
                                    style = MaterialTheme.typography.body2,
                                    color = ExtendedTheme.colors.textSecondary,
                                )
                            }
                        }
                    }

                    is SummaryState.Success -> {
                        SelectionContainer {
                            Text(
                                text = summaryState.summary,
                                style = MaterialTheme.typography.body1,
                                color = ExtendedTheme.colors.text,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                                    .verticalScroll(rememberScrollState()),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = {
                                    TiebaUtil.copyText(context, summaryState.summary)
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(text = stringResource(id = R.string.ai_summary_copy))
                            }
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = ExtendedTheme.colors.text.copy(alpha = 0.1f),
                                    contentColor = ExtendedTheme.colors.text,
                                ),
                            ) {
                                Text(text = stringResource(id = R.string.ai_summary_close))
                            }
                        }
                    }

                    is SummaryState.Error -> {
                        Text(
                            text = stringResource(
                                id = R.string.ai_summary_error,
                                summaryState.message
                            ),
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.error,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = onRetry,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(text = stringResource(id = R.string.ai_summary_retry))
                            }
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = ExtendedTheme.colors.text.copy(alpha = 0.1f),
                                    contentColor = ExtendedTheme.colors.text,
                                ),
                            ) {
                                Text(text = stringResource(id = R.string.ai_summary_close))
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}
