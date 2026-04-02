package com.huanchengfly.tieba.post.ui.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.widgets.compose.AlertDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogNegativeButton
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogPositiveButton
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogState
import com.huanchengfly.tieba.post.update.AppUpdateManifest

@Composable
fun AppUpdateDialog(
    dialogState: DialogState,
    manifest: AppUpdateManifest,
    onDownload: () -> Unit,
    onIgnore: () -> Unit,
) {
    AlertDialog(
        dialogState = dialogState,
        title = {
            Text(text = stringResource(id = R.string.title_dialog_update, manifest.versionName ?: ""))
        },
        content = {
            Column(
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 8.dp)
            ) {
                manifest.publishedAt?.let {
                    Text(text = stringResource(id = R.string.label_update_published_at, it))
                }
                manifest.channel?.let {
                    Text(text = stringResource(id = R.string.label_update_channel, it))
                }
                Text(text = manifest.changelog.orEmpty())
            }
        },
        buttons = {
            if (!manifest.apkUrl.isNullOrBlank()) {
                DialogPositiveButton(
                    text = stringResource(id = R.string.button_download_update),
                    onClick = onDownload
                )
            }
            DialogNegativeButton(
                text = stringResource(id = R.string.button_ignore_this_version),
                onClick = onIgnore
            )
            DialogNegativeButton(
                text = stringResource(id = R.string.button_remind_later)
            )
        }
    )
}
