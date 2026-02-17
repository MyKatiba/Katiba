package com.katiba.app.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katiba.app.data.ChatHistoryStore
import com.katiba.app.data.api.ChatMessage
import com.katiba.app.data.api.GeminiApiClient
import com.katiba.app.data.repository.ConstitutionRepository
import com.katiba.app.ui.components.MarkdownText
import com.katiba.app.ui.theme.KatibaColors
import katiba.composeapp.generated.resources.Res
import katiba.composeapp.generated.resources.mzalendo1
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun MzalendoScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showWelcomeScreen by remember { mutableStateOf(true) }
    var currentSessionId by remember { mutableStateOf<String?>(null) }
    var chatSessions by remember { mutableStateOf(ChatHistoryStore.getChatSessions()) }
    var activeJob by remember { mutableStateOf<Job?>(null) }

    // Initialize GeminiApiClient with constitution context for RAG
    val geminiClient = remember {
        GeminiApiClient().apply {
            // Set constitution context for grounding responses
            if (ConstitutionRepository.isLoaded()) {
                setConstitutionContext(ConstitutionRepository.getContextSummary())
            }
        }
    }

    // Scroll to bottom when new message is added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Save current chat before leaving
            if (messages.isNotEmpty()) {
                if (currentSessionId != null) {
                    ChatHistoryStore.updateChatSession(currentSessionId!!, messages)
                } else {
                    ChatHistoryStore.saveChatSession(messages)
                }
            }
            geminiClient.close()
        }
    }

    fun startNewChat() {
        // Save current chat before starting new one
        if (messages.isNotEmpty()) {
            if (currentSessionId != null) {
                ChatHistoryStore.updateChatSession(currentSessionId!!, messages)
            } else {
                ChatHistoryStore.saveChatSession(messages)
            }
        }
        messages = emptyList()
        inputText = ""
        isLoading = false
        activeJob?.cancel()
        activeJob = null
        showWelcomeScreen = true
        currentSessionId = null
        chatSessions = ChatHistoryStore.getChatSessions()
    }

    fun loadChat(sessionId: String) {
        // Save current chat first
        if (messages.isNotEmpty()) {
            if (currentSessionId != null) {
                ChatHistoryStore.updateChatSession(currentSessionId!!, messages)
            } else {
                ChatHistoryStore.saveChatSession(messages)
            }
        }
        val session = ChatHistoryStore.loadChatSession(sessionId)
        if (session != null) {
            messages = session.messages
            currentSessionId = session.id
            showWelcomeScreen = false
            chatSessions = ChatHistoryStore.getChatSessions()
        }
    }

    fun stopGeneration() {
        activeJob?.cancel()
        activeJob = null
        isLoading = false
    }

    fun sendMessage(text: String = inputText.trim()) {
        val messageText = text.trim()
        if (messageText.isBlank() || isLoading) return

        inputText = ""
        showWelcomeScreen = false

        // Add user message
        val userMessage = ChatMessage(content = messageText, isUser = true)
        messages = messages + userMessage

        // Get conversation history (excluding current message)
        val history = messages.dropLast(1)

        isLoading = true

        activeJob = scope.launch {
            val result = geminiClient.sendMessage(messageText, history)
            isLoading = false
            activeJob = null

            result.fold(
                onSuccess = { response ->
                    val aiMessage = ChatMessage(content = response, isUser = false)
                    messages = messages + aiMessage
                },
                onFailure = { error ->
                    val errorChatMessage = ChatMessage(
                        content = "I apologize, but I encountered an issue: ${error.message ?: "Unknown error"}. Please try again.",
                        isUser = false
                    )
                    messages = messages + errorChatMessage
                }
            )
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatHistoryDrawer(
                sessions = chatSessions,
                currentSessionId = currentSessionId,
                onNewChat = {
                    startNewChat()
                    scope.launch { drawerState.close() }
                },
                onLoadChat = { sessionId ->
                    loadChat(sessionId)
                    scope.launch { drawerState.close() }
                },
                onDeleteChat = { sessionId ->
                    ChatHistoryStore.deleteChatSession(sessionId)
                    chatSessions = ChatHistoryStore.getChatSessions()
                    if (currentSessionId == sessionId) {
                        startNewChat()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                KatibaAITopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNewChatClick = { startNewChat() }
                )
            },
            bottomBar = {
                KatibaAIChatInput(
                    value = inputText,
                    onValueChange = { inputText = it },
                    onSend = { sendMessage() },
                    onStop = { stopGeneration() },
                    isLoading = isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            containerColor = Color.White, // Consistent white background
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            if (showWelcomeScreen && messages.isEmpty()) {
                WelcomeScreenContent(
                    isTyping = inputText.isNotEmpty(),
                    onSuggestionClick = { suggestion -> sendMessage(suggestion) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(messages) { message ->
                            ChatBubble(message = message)
                        }

                        if (isLoading) {
                            item {
                                LoadingIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Top bar ─────────────────────────────────────────────────────────────────

@Composable
private fun KatibaAITopBar(
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Chat history",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = "Katiba AI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = onNewChatClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New chat",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ─── Chat history drawer (redesigned to match reference image) ───────────────

@Composable
private fun ChatHistoryDrawer(
    sessions: List<com.katiba.app.data.ChatSession>,
    currentSessionId: String?,
    onNewChat: () -> Unit,
    onLoadChat: (String) -> Unit,
    onDeleteChat: (String) -> Unit
) {
    // Group sessions by time period
    val now = kotlinx.datetime.Clock.System.now()
    val today = now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date

    val groupedSessions = sessions.groupBy { session ->
        // Simple grouping based on session order (most recent first)
        // In a real app, you'd use the actual timestamp
        val index = sessions.indexOf(session)
        when {
            index == 0 -> "Today"
            index == 1 -> "Yesterday"
            index in 2..6 -> "Last 7 days"
            else -> "Last 30 days"
        }
    }

    ModalDrawerSheet(
        drawerContainerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp)
        ) {
            // Status bar padding
            Spacer(modifier = Modifier.statusBarsPadding())

            // New Chat button at top
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .shadow(2.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNewChat() }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "New chat",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF3F4F6))

            // Chat sessions list grouped by time
            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No previous chats",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    val timeGroups = listOf("Today", "Yesterday", "Last 7 days", "Last 30 days")

                    timeGroups.forEach { timeGroup ->
                        val sessionsInGroup = groupedSessions[timeGroup] ?: emptyList()
                        if (sessionsInGroup.isNotEmpty()) {
                            item {
                                Text(
                                    text = timeGroup,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            items(sessionsInGroup) { session ->
                                val isActive = session.id == currentSessionId
                                ChatHistoryItem(
                                    title = session.title,
                                    isActive = isActive,
                                    onClick = { onLoadChat(session.id) },
                                    onDelete = { onDeleteChat(session.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatHistoryItem(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFF0FDF4) else Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isActive) KatibaColors.KenyaGreen else Color.Black,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = MoreVertIcon,
                        contentDescription = "More options",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = KatibaColors.KenyaRed
                            )
                        }
                    )
                }
            }
        }
    }
}

// More vertical dots icon
private val MoreVertIcon: ImageVector
    get() = ImageVector.Builder(
        name = "MoreVert",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = androidx.compose.ui.graphics.SolidColor(Color.Black)) {
            // Three vertical dots
            moveTo(12f, 8f)
            arcTo(2f, 2f, 0f, false, true, 12f, 4f)
            arcTo(2f, 2f, 0f, false, true, 12f, 8f)
            moveTo(12f, 14f)
            arcTo(2f, 2f, 0f, false, true, 12f, 10f)
            arcTo(2f, 2f, 0f, false, true, 12f, 14f)
            moveTo(12f, 20f)
            arcTo(2f, 2f, 0f, false, true, 12f, 16f)
            arcTo(2f, 2f, 0f, false, true, 12f, 20f)
            close()
        }
    }.build()

// ─── Welcome screen ──────────────────────────────────────────────────────────

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun WelcomeScreenContent(
    isTyping: Boolean,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Static welcome messages - no animation to avoid flicker
    val firstMessage = "Hujambo! I am Mzalendo, your constitutional guide."
    val secondMessage = "How can I help you understand the Kenyan Constitution today?"
    var currentMessage by remember { mutableStateOf(firstMessage) }
    var showBubble by remember { mutableStateOf(false) }

    // Show the bubble after a brief delay, then swap to second message
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(400)
        showBubble = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFAF8F5),
                            Color(0xFFFAF8F5),
                            Color(0xFFFFF5F5)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color(0xFFF0FFF0).copy(alpha = 0.3f)
                        ),
                        startX = 0f,
                        endX = Float.POSITIVE_INFINITY
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (isTyping) Arrangement.Center else Arrangement.Top
        ) {
            // When not typing, add top spacing
            if (!isTyping) {
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Mascot image with chat bubble overlay
            // When typing, show smaller centered mascot without chat bubble
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = if (isTyping) Alignment.Center else Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(Res.drawable.mzalendo1),
                    contentDescription = "Mzalendo - Constitutional Guide Mascot",
                    modifier = Modifier
                        .size(if (isTyping) 120.dp else 160.dp),
                    contentScale = ContentScale.Fit
                )

                // Hide chat bubble when typing
                androidx.compose.animation.AnimatedVisibility(
                    visible = showBubble && !isTyping,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 140.dp)
                ) {
                    WelcomeChatBubble(
                        firstMessage = firstMessage,
                        secondMessage = secondMessage,
                        onFirstComplete = { currentMessage = secondMessage }
                    )
                }
            }

            // Hide prompt suggestions when typing
            AnimatedVisibility(
                visible = !isTyping,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))

                    TryAskingSection(
                        onSuggestionClick = onSuggestionClick,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ─── Welcome chat bubble (fixed: no flicker) ────────────────────────────────

@Composable
private fun WelcomeChatBubble(
    firstMessage: String,
    secondMessage: String,
    onFirstComplete: () -> Unit
) {
    var currentText by remember { mutableStateOf(firstMessage) }
    var displayedText by remember { mutableStateOf("") }
    var hasCompletedFirst by remember { mutableStateOf(false) }

    // Single LaunchedEffect — no conflicting keys, no race condition
    LaunchedEffect(currentText) {
        // Reset displayed text for new message
        displayedText = ""
        // Typewriter animation
        for (i in currentText.indices) {
            displayedText = currentText.substring(0, i + 1)
            kotlinx.coroutines.delay(30)
        }
        // After first message completes, wait and swap
        if (!hasCompletedFirst) {
            hasCompletedFirst = true
            kotlinx.coroutines.delay(1000)
            onFirstComplete()
            currentText = secondMessage
        }
    }

    Box(
        modifier = Modifier.widthIn(max = 280.dp)
    ) {
        // Speech triangle pointing left
        Canvas(
            modifier = Modifier
                .size(12.dp, 16.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-6).dp)
        ) {
            val trianglePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width, 0f)
                lineTo(0f, size.height / 2)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(
                path = trianglePath,
                color = Color(0xFFE8E8E8)
            )
        }

        // Chat bubble
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Text(
                text = displayedText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

// ─── Try Asking section ──────────────────────────────────────────────────────

@Composable
private fun TryAskingSection(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf(
        "What are my rights as a Kenyan citizen?",
        "Explain Article 27 on equality",
        "How does devolution work in Kenya?",
        "What is the Bill of Rights?"
    )

    Column(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Try asking:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        suggestions.forEach { suggestion ->
            SuggestionChip(
                onClick = { onSuggestionClick(suggestion) },
                label = {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = KatibaColors.KenyaGreen.copy(alpha = 0.1f)
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = true,
                    borderColor = KatibaColors.KenyaGreen.copy(alpha = 0.3f)
                ),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

// ─── Chat bubble ─────────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val backgroundColor = if (message.isUser) {
        KatibaColors.KenyaGreen
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (message.isUser) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val shape = if (message.isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            if (message.isUser) {
                Text(
                    text = message.content,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                MarkdownText(
                    markdown = message.content,
                    color = textColor,
                    headingColor = textColor
                )
            }
        }
    }
}

// ─── Loading indicator ───────────────────────────────────────────────────────

@Composable
private fun LoadingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                KatibaColors.KenyaGreen.copy(
                                    alpha = 0.4f + (index * 0.2f)
                                )
                            )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mzalendo is thinking...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Chat input (styled to match bottom navigation bar) ──────────────────────

@Composable
private fun KatibaAIChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    // Styled to match the bottom navigation bar
    Surface(
        modifier = modifier,
        color = Color.White,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Text field row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Text field — no background, no border
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp, max = 120.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Ask Mzalendo...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSend() }),
                        singleLine = false,
                        maxLines = 4
                    )
                }

                // Send / Stop button
                if (isLoading) {
                    // Stop button — red square on pale red circle (media player stop style)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(KatibaColors.KenyaRed.copy(alpha = 0.15f))
                            .clickable { onStop() },
                        contentAlignment = Alignment.Center
                    ) {
                        // Square stop icon
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(
                                    color = KatibaColors.KenyaRed,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                } else {
                    // Send button — grey when empty, black when has text
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (value.isNotBlank()) KatibaColors.KenyaBlack
                                else Color.Gray.copy(alpha = 0.3f)
                            )
                            .clickable(
                                enabled = value.isNotBlank(),
                                onClick = onSend
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (value.isNotBlank()) Color.White else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            // Extra space at the bottom for phone's navigation bar
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
