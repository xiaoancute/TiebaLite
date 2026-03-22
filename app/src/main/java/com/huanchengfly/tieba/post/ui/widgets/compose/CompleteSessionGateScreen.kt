package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.revival.SessionHealth
import com.huanchengfly.tieba.post.revival.SessionHealthStatus

@Composable
fun CompleteSessionGateScreen(
    sessionHealth: SessionHealth,
    title: String,
    message: String,
    onResolveSession: (SessionHealthStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    TipScreen(
        title = {
            Text(text = title)
        },
        message = {
            Text(text = message)
        },
        actions = {
            Button(
                onClick = { onResolveSession(sessionHealth.status) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(
                        id = if (sessionHealth.status == SessionHealthStatus.LoggedOut) {
                            R.string.button_login
                        } else {
                            R.string.title_account_manage
                        }
                    )
                )
            }
        },
        modifier = modifier
    )
}
