package com.katiba.app.ui.constitution

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.katiba.app.data.model.Article
import com.katiba.app.data.model.Chapter
import com.katiba.app.data.model.Clause
import com.katiba.app.data.repository.ArticleReadManager
import com.katiba.app.data.repository.ConstitutionRepository
import com.katiba.app.ui.theme.KatibaColors

/**
 * Reading view for constitution content with vertical scroll
 * Similar to Bible reader with verse numbers and chapter navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    chapterNumber: Int,
    articleNumber: Int,
    onBackClick: () -> Unit,
    onNavigateToArticle: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val chapters = remember { ConstitutionRepository.chapters }
    val chapter = remember(chapterNumber) {
        chapters.find { it.number == chapterNumber }
    }
    val article = remember(chapterNumber, articleNumber) {
        chapter?.articles?.find { it.number == articleNumber }
    }
    
    // Track article read
    LaunchedEffect(chapterNumber, articleNumber) {
        ArticleReadManager.recordArticleRead(chapterNumber, articleNumber)
    }
    
    val scrollState = rememberScrollState()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Request focus when search opens
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            kotlinx.coroutines.delay(100)
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            ReadingTopBar(
                onMenuClick = onBackClick,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchClick = { isSearchActive = true },
                onCloseSearch = {
                    searchQuery = ""
                    isSearchActive = false
                },
                focusRequester = focusRequester
            )
        },
        bottomBar = {
            if (chapter != null && article != null) {
                ReadingBottomBar(
                    chapter = chapter,
                    article = article,
                    onPreviousClick = {
                        val prevArticle = chapter.articles.find { it.number == articleNumber - 1 }
                        if (prevArticle != null) {
                            onNavigateToArticle(chapterNumber, prevArticle.number)
                        }
                    },
                    onNextClick = {
                        val nextArticle = chapter.articles.find { it.number == articleNumber + 1 }
                        if (nextArticle != null) {
                            onNavigateToArticle(chapterNumber, nextArticle.number)
                        }
                    },
                    hasPrevious = chapter.articles.any { it.number == articleNumber - 1 },
                    hasNext = chapter.articles.any { it.number == articleNumber + 1 }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (chapter != null && article != null) {
                // Article header
                ArticleHeader(
                    chapterTitle = chapter.title,
                    articleNumber = article.number
                )
                
                // Reading content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Article title
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Clauses
                    article.clauses.forEach { clause ->
                        ClauseContent(clause = clause, searchQuery = searchQuery)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Article not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadingTopBar(
    onMenuClick: () -> Unit,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onCloseSearch: () -> Unit,
    focusRequester: FocusRequester
) {
    Surface(
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
                    title = { },
                    navigationIcon = {
                        IconButton(
                            onClick = onMenuClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onSearchClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "2010",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
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
                        onValueChange = onSearchQueryChange,
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
                                    text = "Search in article...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onCloseSearch,
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

@Composable
private fun ArticleHeader(
    chapterTitle: String,
    articleNumber: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = chapterTitle.split(" - ").lastOrNull() ?: chapterTitle,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Serif),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = articleNumber.toString(),
            style = MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Serif),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ClauseContent(clause: Clause, searchQuery: String = "") {
    val annotatedText = buildAnnotatedString {
        // Clause number as superscript-style
        if (clause.number.isNotEmpty()) {
            withStyle(
                style = SpanStyle(
                    color = KatibaColors.KenyaGreen,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("(${clause.number}) ")
            }
        }
        
        // Clause text with highlighting
        val text = clause.text
        if (searchQuery.isNotBlank()) {
            val lowerText = text.lowercase()
            val lowerQuery = searchQuery.lowercase()
            var lastIndex = 0
            var startIndex = lowerText.indexOf(lowerQuery)
            while (startIndex >= 0) {
                // Text before match
                append(text.substring(lastIndex, startIndex))
                // Highlighted match
                withStyle(SpanStyle(background = Color(0xFFFFEB3B), color = Color.Black, fontWeight = FontWeight.Bold)) {
                    append(text.substring(startIndex, startIndex + searchQuery.length))
                }
                lastIndex = startIndex + searchQuery.length
                startIndex = lowerText.indexOf(lowerQuery, lastIndex)
            }
            // Remaining text
            append(text.substring(lastIndex))
        } else {
            withStyle(style = SpanStyle(color = Color.Unspecified)) {
                append(text)
            }
        }
    }
    
    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif),
        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f
    )
    
    // Sub-clauses
    if (clause.subClauses.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        clause.subClauses.forEach { subClause ->
            Column(
                modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 4.dp)
            ) {
                Row {
                    Text(
                        text = "(${subClause.label})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif),
                        color = KatibaColors.KenyaGreen,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(32.dp)
                    )
                    Text(
                        text = subClause.text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif),
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4f
                    )
                }
                // Sub-sub-clauses (i, ii, iii, etc.)
                if (subClause.subSubClauses.isNotEmpty()) {
                    subClause.subSubClauses.forEach { subSubClause ->
                        Row(
                            modifier = Modifier.padding(start = 32.dp, top = 4.dp, bottom = 4.dp)
                        ) {
                            Text(
                                text = "(${subSubClause.label})",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif),
                                color = KatibaColors.KenyaGreen.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(32.dp)
                            )
                            Text(
                                text = subSubClause.text,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif),
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4f
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadingBottomBar(
    chapter: Chapter,
    article: Article,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    hasPrevious: Boolean,
    hasNext: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Previous button - left edge, black circular
            IconButton(
                onClick = onPreviousClick,
                enabled = hasPrevious,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasPrevious) Color.Black
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    tint = if (hasPrevious) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Center - Article label
            Text(
                text = "Article ${article.number}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            // Next button - right edge, black circular
            IconButton(
                onClick = onNextClick,
                enabled = hasNext,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasNext) Color.Black
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = if (hasNext) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
