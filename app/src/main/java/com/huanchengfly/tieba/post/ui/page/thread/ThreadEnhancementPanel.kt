package com.huanchengfly.tieba.post.ui.page.thread

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.widgets.compose.SearchBox
import com.huanchengfly.tieba.post.ui.widgets.compose.SimplePredictiveBackHandler

private enum class ThreadEnhancementTab(@StringRes val titleRes: Int) {
    LzTimeline(R.string.title_thread_enhancement_lz_timeline),
    Search(R.string.title_thread_enhancement_search),
}

@Composable
fun ThreadEnhancementPanel(
    state: ThreadEnhancementState,
    onPostClick: (ThreadEnhancementPost) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }

    SimplePredictiveBackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.2f)
                .height(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape,
                )
                .align(Alignment.CenterHorizontally),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.button_back),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.title_thread_enhancement),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.message_thread_enhancement_loaded, state.posts.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
            ThreadEnhancementTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(stringResource(tab.titleRes)) },
                )
            }
        }

        when (ThreadEnhancementTab.entries[selectedTabIndex]) {
            ThreadEnhancementTab.LzTimeline -> ThreadEnhancementPostList(
                posts = state.lzPosts,
                emptyTitle = stringResource(R.string.message_thread_enhancement_lz_empty),
                onPostClick = onPostClick,
                modifier = Modifier.fillMaxSize(),
            )

            ThreadEnhancementTab.Search -> ThreadEnhancementSearch(
                state = state,
                query = query,
                onQueryChange = { query = it },
                onPostClick = onPostClick,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ThreadEnhancementSearch(
    state: ThreadEnhancementState,
    query: String,
    onQueryChange: (String) -> Unit,
    onPostClick: (ThreadEnhancementPost) -> Unit,
    modifier: Modifier = Modifier,
) {
    val results = remember(state, query) { state.search(query) }

    Column(modifier = modifier) {
        SearchBox(
            keyword = query,
            onKeywordChange = onQueryChange,
            placeholder = { Text(stringResource(R.string.hint_thread_enhancement_search)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        when {
            query.isBlank() -> ThreadEnhancementEmptyState(
                title = stringResource(R.string.message_thread_enhancement_search_start),
                modifier = Modifier.fillMaxSize(),
            )

            results.isEmpty() -> ThreadEnhancementEmptyState(
                title = stringResource(R.string.message_thread_enhancement_search_empty),
                modifier = Modifier.fillMaxSize(),
            )

            else -> {
                Text(
                    text = stringResource(R.string.message_thread_enhancement_search_results, results.size),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
                ThreadEnhancementPostList(
                    posts = results,
                    emptyTitle = "",
                    onPostClick = onPostClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ThreadEnhancementPostList(
    posts: List<ThreadEnhancementPost>,
    emptyTitle: String,
    onPostClick: (ThreadEnhancementPost) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (posts.isEmpty()) {
        ThreadEnhancementEmptyState(title = emptyTitle, modifier = modifier)
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(items = posts, key = ThreadEnhancementPost::postId) { post ->
            ThreadEnhancementPostItem(post = post, onClick = { onPostClick(post) })
        }
    }
}

@Composable
private fun ThreadEnhancementPostItem(
    post: ThreadEnhancementPost,
    onClick: () -> Unit,
) {
    val preview = when {
        post.isBlocked -> stringResource(R.string.message_thread_enhancement_blocked)
        post.previewText.isBlank() -> stringResource(R.string.message_thread_enhancement_no_text)
        else -> post.previewText
    }

    ListItem(
        headlineContent = {
            Text(text = stringResource(R.string.tip_post_floor, post.floor))
        },
        supportingContent = {
            Text(
                text = preview,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null)
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
    HorizontalDivider()
}

@Composable
private fun ThreadEnhancementEmptyState(
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
