package com.katiba.app.ui.constitution

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.katiba.app.data.model.Chapter
import com.katiba.app.data.repository.ConstitutionRepository
import com.katiba.app.ui.theme.KatibaColors

/**
 * Bible reader-inspired Constitution screen with chapter/article navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstitutionScreen(
    onChapterClick: (Int) -> Unit,
    onPreambleClick: () -> Unit,
    onSchedulesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chapters = remember { ConstitutionRepository.chapters }
    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }
    val listState = rememberLazyListState()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Header alpha: transparent at top, solid when scrolled
    val headerAlpha by animateFloatAsState(
        targetValue = if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "headerAlpha"
    )

    // Build filtered list items
    data class ListItem(val type: String, val chapterNumber: Int, val title: String, val articleCount: Int, val chapter: Chapter? = null)

    val allItems = remember(chapters) {
        val items = mutableListOf<ListItem>()
        items.add(ListItem("preamble", 0, "Preamble", 0))
        chapters.forEach { ch ->
            items.add(ListItem("chapter", ch.number, ch.title, ch.articles.size, ch))
        }
        items.add(ListItem("schedule", 99, "Schedules", 6))
        items
    }

    val filteredItems = remember(searchQuery, allItems) {
        if (searchQuery.isBlank()) allItems
        else {
            val q = searchQuery.lowercase()
            allItems.filter { item ->
                item.title.lowercase().contains(q) ||
                "chapter ${item.chapterNumber}".lowercase().contains(q) ||
                item.chapterNumber.toString() == q ||
                item.type.contains(q)
            }
        }
    }

    // Request focus when search opens
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            kotlinx.coroutines.delay(100)
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Content
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 72.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredItems, key = { "${it.type}_${it.chapterNumber}" }) { item ->
                when (item.type) {
                    "preamble" -> ChapterListItem(
                        chapterNumber = 0,
                        chapterTitle = "Preamble",
                        articleCount = 0,
                        isPreamble = true,
                        onClick = onPreambleClick
                    )
                    "schedule" -> ChapterListItem(
                        chapterNumber = 99,
                        chapterTitle = "Schedules",
                        articleCount = 6,
                        isPreamble = false,
                        isSchedule = true,
                        onClick = onSchedulesClick
                    )
                    else -> ChapterListItem(
                        chapterNumber = item.chapterNumber,
                        chapterTitle = item.title,
                        articleCount = item.articleCount,
                        isPreamble = false,
                        onClick = { onChapterClick(item.chapterNumber) }
                    )
                }
            }
        }

        // Floating header / Search bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                // Normal header
                AnimatedVisibility(
                    visible = !isSearchActive,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
                    exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                ) {
                    TopAppBar(
                        title = {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "The Constitution",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "of Kenya, 2010",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        windowInsets = WindowInsets(0.dp)
                    )
                }

                // Search bar - expands from left like a filling progress bar
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn(animationSpec = tween(200)) + expandHorizontally(
                        animationSpec = tween(
                            durationMillis = 350,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        ),
                        expandFrom = Alignment.Start
                    ),
                    exit = fadeOut(animationSpec = tween(150)) + shrinkHorizontally(
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        ),
                        shrinkTowards = Alignment.Start
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(KatibaColors.KenyaGreen),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search chapters...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                isSearchActive = false
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterListView(
    chapters: List<Chapter>,
    onChapterClick: (Chapter) -> Unit,
    onPreambleClick: () -> Unit,
    onSchedulesClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Preamble item
        item {
            ChapterListItem(
                chapterNumber = 0,
                chapterTitle = "Preamble",
                articleCount = 0,
                isPreamble = true,
                onClick = onPreambleClick
            )
        }
        
        // Chapter items
        items(chapters) { chapter ->
            ChapterListItem(
                chapterNumber = chapter.number,
                chapterTitle = chapter.title,
                articleCount = chapter.articles.size,
                isPreamble = false,
                onClick = { onChapterClick(chapter) }
            )
        }
        
        // Schedules item
        item {
            ChapterListItem(
                chapterNumber = 99,
                chapterTitle = "Schedules",
                articleCount = 6,
                isPreamble = false,
                isSchedule = true,
                onClick = onSchedulesClick
            )
        }
    }
}

@Composable
private fun ChapterListItem(
    chapterNumber: Int,
    chapterTitle: String,
    articleCount: Int,
    isPreamble: Boolean,
    isSchedule: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Chapter number badge
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        isPreamble -> KatibaColors.BeadGold.copy(alpha = 0.15f)
                        isSchedule -> KatibaColors.KenyaRed.copy(alpha = 0.1f)
                        else -> KatibaColors.KenyaGreen.copy(alpha = 0.1f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isPreamble) {
                Text(
                    text = "📜",
                    style = MaterialTheme.typography.titleMedium
                )
            } else if (isSchedule) {
                Text(
                    text = "📋",
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Text(
                    text = chapterNumber.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KatibaColors.KenyaGreen
                )
            }
        }
        
        // Title and info
        Column(modifier = Modifier.weight(1f)) {
            if (!isPreamble && !isSchedule) {
                Text(
                    text = "Chapter $chapterNumber",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = chapterTitle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (articleCount > 0) {
                Text(
                    text = "$articleCount articles",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
