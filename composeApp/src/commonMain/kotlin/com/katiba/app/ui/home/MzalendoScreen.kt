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
import androidx.compose.ui.graphics.RectangleShape
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
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            if (showWelcomeScreen && messages.isEmpty()) {
                WelcomeScreenContent(
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
                .statusBarsPadding()
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

// ─── Chat history drawer ─────────────────────────────────────────────────────

@Composable
private fun ChatHistoryDrawer(
    sessions: List<com.katiba.app.data.ChatSession>,
    currentSessionId: String?,
    onNewChat: () -> Unit,
    onLoadChat: (String) -> Unit,
    onDeleteChat: (String) -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp)
        ) {
            // Header
            Surface(
                color = KatibaColors.KenyaGreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Chat History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${sessions.size} conversation${if (sessions.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // New Chat button
            Surface(
                onClick = onNewChat,
                color = KatibaColors.KenyaGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = KatibaColors.KenyaGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New Chat",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KatibaColors.KenyaGreen
                    )
                }
            }

            // Chat sessions list
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    items(sessions) { session ->
                        val isActive = session.id == currentSessionId
                        Surface(
                            onClick = { onLoadChat(session.id) },
                            color = if (isActive) KatibaColors.KenyaGreen.copy(alpha = 0.1f)
                                    else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = session.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${session.messages.size} messages",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteChat(session.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete chat",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Welcome screen ──────────────────────────────────────────────────────────

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun WelcomeScreenContent(
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Mascot image with chat bubble overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.mzalendo1),
                    contentDescription = "Mzalendo - Constitutional Guide Mascot",
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.CenterStart),
                    contentScale = ContentScale.Fit
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = showBubble,
                    enter = fadeIn() + expandVertically(),
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

            Spacer(modifier = Modifier.height(24.dp))

            TryAskingSection(
                onSuggestionClick = onSuggestionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
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

// ─── Chat input (redesigned: Gemini-style bottom bar) ────────────────────────

@Composable
private fun KatibaAIChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val topBorderColor = Color(0xFFE0E0E0)

    Surface(
        modifier = modifier
            .drawBehind {
                // Subtle grey shadow line on top border
                drawLine(
                    color = topBorderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            },
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Text field row
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
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
        }
    }
}
