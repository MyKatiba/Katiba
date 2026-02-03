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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katiba.app.data.api.ChatMessage
import com.katiba.app.data.api.GeminiApiClient
import com.katiba.app.data.repository.ConstitutionRepository
import com.katiba.app.ui.theme.KatibaColors
import katiba.composeapp.generated.resources.Res
import katiba.composeapp.generated.resources.mzalendo1
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

    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showWelcomeScreen by remember { mutableStateOf(true) }

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
            geminiClient.close()
        }
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

        scope.launch {
            val result = geminiClient.sendMessage(messageText, history)
            isLoading = false

            result.fold(
                onSuccess = { response ->
                    val aiMessage = ChatMessage(content = response, isUser = false)
                    messages = messages + aiMessage
                },
                onFailure = { error ->
                    // Add error message to chat
                    val errorChatMessage = ChatMessage(
                        content = "I apologize, but I encountered an issue: ${error.message ?: "Unknown error"}. Please try again.",
                        isUser = false
                    )
                    messages = messages + errorChatMessage
                }
            )
        }
    }

    Scaffold(
        topBar = {
            KatibaAITopBar(onBackClick = onBackClick)
        },
        bottomBar = {
            KatibaAIChatInput(
                value = inputText,
                onValueChange = { inputText = it },
                onSend = { sendMessage() },
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (showWelcomeScreen && messages.isEmpty()) {
            // Welcome screen with mascot and topics
            WelcomeScreenContent(
                onSuggestionClick = { suggestion -> sendMessage(suggestion) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            // Chat conversation view
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

@Composable
private fun KatibaAITopBar(onBackClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(44.dp), // Explicit height - reduced by additional 10dp
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Katiba AI",
                style = MaterialTheme.typography.titleMedium, // Increased from titleSmall
                fontWeight = FontWeight.Bold, // Make it bolder
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = { /* Menu options */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun WelcomeScreenContent(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animated chat bubble state - single bubble with changing text
    var showBubble by remember { mutableStateOf(false) }
    var currentMessage by remember { mutableStateOf("Hujambo! I am Simba, your constitutional guide.") }
    val firstMessage = "Hujambo! I am Simba, your constitutional guide."
    val secondMessage = "How can I help you understand the Kenyan Constitution today?"

    // Trigger animations after a delay
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500)
        showBubble = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Gradient background - pale red on bottom left, pale green on bottom right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFAF8F5), // Light cream on top
                            Color(0xFFFAF8F5), // Light cream
                            Color(0xFFFFF5F5)  // Very pale red/pink at bottom
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Overlay gradient from bottom - pale green on right side
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color(0xFFF0FFF0).copy(alpha = 0.3f) // Very pale green on right
                        ),
                        startX = 0f,
                        endX = Float.POSITIVE_INFINITY
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Mascot image with chat bubble overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Mascot image
                Image(
                    painter = painterResource(Res.drawable.mzalendo1),
                    contentDescription = "Simba - Constitutional Guide Mascot",
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.CenterStart),
                    contentScale = ContentScale.Fit
                )

                // Chat bubble overlaid on image, positioned to the right
                androidx.compose.animation.AnimatedVisibility(
                    visible = showBubble,
                    enter = fadeIn() + expandVertically(),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 140.dp) // 160dp - 20dp = moved 10dp closer
                ) {
                    ChatBubbleContent(
                        currentMessage = currentMessage,
                        firstMessage = firstMessage,
                        secondMessage = secondMessage,
                        onFirstComplete = { currentMessage = secondMessage }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Try Asking section (Popular Topics removed)
            TryAskingSection(
                onSuggestionClick = onSuggestionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun TypewriterChatBubble(
    text: String,
    onComplete: () -> Unit
) {
    var displayedText by remember { mutableStateOf("") }
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(text) {
        while (currentIndex < text.length) {
            displayedText = text.substring(0, currentIndex + 1)
            currentIndex++
            kotlinx.coroutines.delay(30) // Typing speed
        }
        onComplete()
    }

    Box(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
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

@Composable
private fun ChatBubbleContent(
    currentMessage: String,
    firstMessage: String,
    secondMessage: String,
    onFirstComplete: () -> Unit
) {
    var localComplete by remember { mutableStateOf(false) }

    LaunchedEffect(localComplete) {
        if (localComplete) {
            kotlinx.coroutines.delay(1000) // 1 second delay
            onFirstComplete()
        }
    }

    TypewriterChatBubbleWithTriangle(
        text = currentMessage,
        onComplete = {
            if (currentMessage == firstMessage) {
                localComplete = true
            }
        }
    )
}

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

@Composable
private fun TypewriterChatBubbleWithTriangle(
    text: String,
    onComplete: () -> Unit
) {
    var displayedText by remember { mutableStateOf("") }
    var currentIndex by remember { mutableStateOf(0) }
    var textKey by remember { mutableStateOf(0) }

    // Reset animation when text changes
    LaunchedEffect(text) {
        displayedText = ""
        currentIndex = 0
        textKey++
    }

    LaunchedEffect(textKey) {
        while (currentIndex < text.length) {
            displayedText = text.substring(0, currentIndex + 1)
            currentIndex++
            kotlinx.coroutines.delay(30) // Typing speed
        }
        onComplete()
    }

    Box(
        modifier = Modifier
            .widthIn(max = 280.dp)
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
                color = androidx.compose.ui.graphics.Color(0xFFE8E8E8) // Match bubble color
            )
        }

        // Chat bubble
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp))
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
            Text(
                text = message.content,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

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

@Composable
private fun KatibaAIChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFFAF8F5) // Light cream background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Text input with send button inside (no border, no plus icon)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Text input with send button inside
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(24.dp),
                            clip = false
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Text field
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 40.dp, max = 120.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = "Write here...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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

                        // Send button inside the text box - grey when inactive, black when active
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (value.isNotBlank() && !isLoading)
                                        KatibaColors.KenyaBlack
                                    else
                                        Color.Gray.copy(alpha = 0.3f)
                                )
                                .clickable(
                                    enabled = value.isNotBlank() && !isLoading,
                                    onClick = onSend
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                // Custom send icon similar to the image
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
    }
}

