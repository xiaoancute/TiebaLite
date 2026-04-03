package com.huanchengfly.tieba.post.ui.page.settings.about

import com.huanchengfly.tieba.post.arch.GlobalEvent

const val ABOUT_SOURCE_CODE_URL = "https://github.com/xiaoancute/TiebaLite"

fun buildManualCheckAppUpdateEvent(): GlobalEvent.CheckAppUpdate =
    GlobalEvent.CheckAppUpdate(manual = true)
