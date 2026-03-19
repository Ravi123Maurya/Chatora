package com.ravi.chatora.presentation.chatora

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ravi.chatora.domain.models.Chatora
import com.ravi.chatora.presentation.chatora.components.ScrollToBottom
import com.ravi.chatora.presentation.chatora.components.VoiceChatDialog
import com.ravi.chatora.presentation.chatora.components.VoiceChatoraViewModel
import com.ravi.chatora.presentation.chatorahistory.ChatoraHistory
import com.ravi.chatora.ui.theme.AppColors
import com.ravi.chatora.utils.copyToClip
import com.ravi.chatora.utils.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Design tokens ────────────────────────────────────────────────────────────

private val CornerMd = 12.dp
private val CornerLg = 20.dp
private val CornerXl = 28.dp
private val SpaceXs = 4.dp
private val SpaceSm = 8.dp
private val SpaceMd = 12.dp
private val SpaceLg = 16.dp
private val SpaceXl = 24.dp

// ─── Screen ───────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatoraScreen(
    chatoraViewModel: ChatoraViewModel = hiltViewModel(),
    voiceChatoraViewModel: VoiceChatoraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var messageText by remember { mutableStateOf("") }
    val chatUiState by chatoraViewModel.chatUiState.collectAsStateWithLifecycle()
    val chatoraState by chatoraViewModel.getChatoraResponseState.collectAsState()
    val voiceState by voiceChatoraViewModel.voiceState.collectAsState()

    var lastAnimatedMessageId by rememberSaveable { mutableStateOf<Long?>(null) }
    var shouldStartVoiceChat by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) context.showToast("Microphone permission denied")
            else {
                context.showToast("Ready to listen"); shouldStartVoiceChat = true
            }
        }
    )

    LaunchedEffect(Unit) {
        if (chatUiState.isNotEmpty()) listState.animateScrollToItem(0)
    }


    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    BackHandler(enabled = pagerState.currentPage != 0) {
        coroutineScope.launch { pagerState.animateScrollToPage(0) }
    }

    HorizontalPager(state = pagerState, userScrollEnabled = false) { page ->
        when (page) {
            0 -> {
                Scaffold(
                    containerColor = AppColors.Background,
                    topBar = {
                        ChatoraTopBar(
                            modifier = Modifier.windowInsetsPadding(
                                WindowInsets.systemBars.only(WindowInsetsSides.Top)
                            ),
                            messageCount = chatUiState.size,
                            onMenuClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            },
                            onNewChat = { /*chatoraViewModel.clearChat()*/ }
                        )
                    },
                    bottomBar = {
                        ChatoraBottomBar(
                            isLoading = chatoraState is GetChatoraResponse.Loading,
                            messageText = messageText,
                            onMessageChange = { messageText = it },
                            onSendClick = {
                                if (messageText.isNotBlank()) {
                                    coroutineScope.launch {
                                        chatoraViewModel.getChatoraResponse(messageText)
                                        messageText = ""
                                        listState.animateScrollToItem(0)
                                    }
                                }
                            },
                            onVoiceChatClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) shouldStartVoiceChat = true
                                else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        )
                    },
                    floatingActionButton = {
                        AnimatedVisibility(
                            visible = listState.firstVisibleItemScrollOffset != 0,
                            enter = slideInVertically(animationSpec = tween(), initialOffsetY = { it/2 }),
                            exit = slideOutVertically(animationSpec = tween(), targetOffsetY = { it/2 })
                        ) {
                            ScrollToBottom(onClick = {
                                coroutineScope.launch { listState.animateScrollToItem(0) }
                            })
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColors.Background)
                            .padding(paddingValues)
                    ) {
                        if (chatUiState.isEmpty()) {
                            EmptyStateView()
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    top = SpaceLg,
                                    bottom = SpaceXl,
                                    start = SpaceMd,
                                    end = SpaceMd
                                ),
                                reverseLayout = true,
                                verticalArrangement = Arrangement.spacedBy(SpaceMd)
                            ) {
                                itemsIndexed(
                                    items = chatUiState.reversed(),
                                    key = { _, message -> message.id }
                                ) { index, message ->
                                    val shouldAnimate =
                                        index == 0 && message.isUser &&
                                                lastAnimatedMessageId != message.id.toLong()
                                    if (shouldAnimate) SideEffect {
                                        lastAnimatedMessageId = message.id.toLong()
                                    }
                                    ChatBubble(
                                        message = message,
                                        shouldAnimate = shouldAnimate,
                                        onDeleteClick = {
                                            coroutineScope.launch {
                                                delay(500)
                                                chatoraViewModel.deleterChatora(message.id)
                                            }
                                            context.showToast("Message deleted")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                ChatoraHistory(
                    history = listOf(),
                    onHistoryClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                    },
                    onBackClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                    }
                )
            }
        }
    }

    if (shouldStartVoiceChat) {
        VoiceChatDialog(
            voiceState = voiceState,
            onStartListening = { voiceChatoraViewModel.startVoiceChatora() },
            onStopListening = { voiceChatoraViewModel.stopVoiceChatora() },
            onSubmitClick = { messageText = voiceState.spokenText; shouldStartVoiceChat = false },
            onCancelClick = {
                voiceChatoraViewModel.stopVoiceChatora(); shouldStartVoiceChat = false
            }
        )
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpaceXl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing logo mark
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(AppColors.Primary.copy(alpha = 0.15f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
                .border(1.dp, AppColors.Primary.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "C",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary
            )
        }
        Spacer(Modifier.height(SpaceXl))
        Text(
            text = "How can I help you?",
            color = AppColors.OnSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(SpaceXs))
        Text(
            text = "Ask me anything — I'm here to think alongside you.",
            color = AppColors.OnSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(SpaceXl + SpaceLg))

        // Suggestion chips
        val suggestions = listOf(
            "✦  Summarise an article",
            "✦  Write some code",
            "✦  Explain a concept",
            "✦  Brainstorm ideas"
        )
        suggestions.forEach { suggestion ->
            SuggestionChip(suggestion)
            Spacer(Modifier.height(SpaceSm))
        }
    }
}

@Composable
private fun SuggestionChip(text: String) {
    Surface(
        shape = RoundedCornerShape(CornerLg),
        color = AppColors.SurfaceContainer,
        border = BorderStroke(1.dp, AppColors.OutlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth(0.85f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = SpaceLg, vertical = SpaceMd),
            color = AppColors.OnSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatoraTopBar(
    modifier: Modifier = Modifier,
    messageCount: Int = 0,
    onMenuClick: () -> Unit = {},
    onNewChat: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppColors.Background,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpaceMd, vertical = SpaceSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo + name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpaceMd),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(AppColors.Primary, AppColors.Tertiary)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "C",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "Chatora",
                            color = AppColors.OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                        if (messageCount > 0) {
                            Text(
                                text = "$messageCount messages",
                                color = AppColors.OnSurfaceVariant,
                                fontSize = 11.sp
                            )
                        } else {
                            Text(
                                text = "AI Assistant",
                                color = AppColors.OnSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onNewChat() }
                        .background(AppColors.PrimaryContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New chat",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.OnPrimaryContainer
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Message,
                        contentDescription = "Start New chat",
                        tint = AppColors.OnPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "History",
                        tint = AppColors.OnSurface
                    )
                }
            }
            // Bottom divider
            HorizontalDivider(
                color = AppColors.OutlineVariant.copy(alpha = 0.4f),
                thickness = 0.5.dp
            )
        }
    }
}

// ─── Bottom Bar ───────────────────────────────────────────────────────────────

@Composable
fun ChatoraBottomBar(
    isLoading: Boolean,
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onVoiceChatClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var hasMicClicked by remember { mutableStateOf(false) }

    LaunchedEffect(hasMicClicked) {
        if (hasMicClicked) {
            delay(1200); hasMicClicked = false
        }
    }

    val canSend = messageText.isNotBlank()

    Surface(
        modifier = Modifier.imePadding(),
        color = AppColors.Background,
        shadowElevation = 0.dp
    ) {
        Column {
            HorizontalDivider(
                color = AppColors.OutlineVariant.copy(alpha = 0.4f),
                thickness = 0.5.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Background)
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(horizontal = SpaceMd, vertical = SpaceMd),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(SpaceSm)
            ) {
                // Text field
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Message Chatora…",
                            color = AppColors.OnSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 15.sp
                        )
                    },
                    shape = RoundedCornerShape(CornerXl),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary.copy(alpha = 0.6f),
                        unfocusedBorderColor = AppColors.OutlineVariant.copy(alpha = 0.5f),
                        focusedContainerColor = AppColors.SurfaceContainer,
                        unfocusedContainerColor = AppColors.SurfaceContainer,
                        focusedTextColor = AppColors.OnSurface,
                        unfocusedTextColor = AppColors.OnSurface,
                        cursorColor = AppColors.Primary,
                    ),
                    maxLines = 5,
                    textStyle = LocalTextStyle.current.copy(fontSize = 15.sp)
                )

                // Voice button
                InputIconButton(
                    onClick = { hasMicClicked = true; onVoiceChatClick() },
                    isActive = hasMicClicked,
                    activeColor = AppColors.Secondary,
                    icon = Icons.Default.Mic,
                    contentDescription = "Voice input"
                )

                // Send button
                val sendBg = if (canSend) AppColors.Primary else AppColors.SurfaceContainerHigh
                val sendIcon = if (canSend) Color.White else AppColors.OnSurfaceVariant
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(sendBg)
                        .clickable(enabled = !isLoading) { onSendClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Send",
                            tint = sendIcon,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InputIconButton(
    onClick: () -> Unit,
    isActive: Boolean,
    activeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isActive) activeColor.copy(alpha = 0.15f)
                else AppColors.SurfaceContainerHigh
            )
            .border(
                1.dp,
                if (isActive) activeColor.copy(alpha = 0.5f)
                else AppColors.OutlineVariant.copy(alpha = 0.4f),
                CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = activeColor,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = AppColors.OnSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ─── Chat Bubble ──────────────────────────────────────────────────────────────

@Composable
fun ChatBubble(
    message: Chatora,
    shouldAnimate: Boolean,
    onDeleteClick: () -> Unit = {}
) {
    var pressOffset by remember { mutableStateOf(Offset.Zero) }
    var showMenu by remember { mutableStateOf(false) }
    var hasDeleteConfirmed by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    val alphaAnim = remember { Animatable(if (shouldAnimate && message.isUser) 0f else 1f) }
    val scaleAnim = remember { Animatable(if (shouldAnimate && message.isUser) 0.85f else 1f) }
    val offsetYAnim = remember { Animatable(if (shouldAnimate && message.isUser) 60f else 0f) }

    LaunchedEffect(Unit) {
        if (message.isUser && shouldAnimate) {
            launch { alphaAnim.animateTo(1f, tween(250)) }
            launch {
                scaleAnim.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            }
            launch { offsetYAnim.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) }
        }
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                alpha = alphaAnim.value
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
                translationY = offsetYAnim.value
            }
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { offset ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    pressOffset = offset
                    showMenu = true
                })
            }
    ) {
        AnimatedVisibility(
            visible = !hasDeleteConfirmed,
            exit = shrinkVertically(tween(400), shrinkTowards = Alignment.Top)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
            ) {
                // AI avatar
                if (!message.isUser) {
                    Box(
                        modifier = Modifier
                            .padding(end = SpaceSm)
                            .size(32.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(AppColors.Primary, AppColors.Tertiary)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "C",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Bubble
                Surface(
                    shape = RoundedCornerShape(
                        topStart = CornerLg,
                        topEnd = CornerLg,
                        bottomStart = if (message.isUser) CornerLg else SpaceXs,
                        bottomEnd = if (message.isUser) SpaceXs else CornerLg
                    ),
                    color = if (message.isUser) AppColors.Primary
                    else AppColors.SurfaceContainerHigh,
                    shadowElevation = if (message.isUser) 2.dp else 1.dp,
                    modifier = if (message.isUser) {
                        Modifier.widthIn(max = 290.dp)
                    } else {
                        Modifier
                            .weight(1f)
                            .border(
                                0.5.dp,
                                AppColors.OutlineVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(CornerLg, CornerLg, CornerLg, SpaceXs)
                            )
                    }
                ) {
                    SelectionContainer {
                        Column(modifier = Modifier.padding(SpaceMd)) {
                            MarkdownText(message)
                            Spacer(Modifier.height(SpaceXs))

                            // Footer
                            Row(
                                modifier = Modifier.align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(SpaceXs)
                            ) {
                                if (!message.isUser) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = "Copy",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                context.copyToClip(
                                                    clipboard,
                                                    message.message,
                                                    scope
                                                )
                                                context.showToast("Copied to clipboard")
                                            },
                                        tint = AppColors.OnSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                Text(
                                    text = message.timeStamp,
                                    fontSize = 10.sp,
                                    color = if (message.isUser) Color.White.copy(alpha = 0.6f)
                                    else AppColors.OnSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = with(density) { DpOffset(pressOffset.x.toDp(), pressOffset.y.toDp()) },
            containerColor = AppColors.Surface,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(CornerMd)
        ) {
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(SpaceSm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.ContentCopy,
                            null,
                            Modifier.size(16.dp),
                            tint = AppColors.OnSurfaceVariant
                        )
                        Text("Copy message", fontSize = 14.sp, color = AppColors.OnSurface)
                    }
                },
                onClick = {
                    context.copyToClip(clipboard, message.message, scope)
                    context.showToast("Copied")
                    showMenu = false
                }
            )
            HorizontalDivider(
                color = AppColors.OutlineVariant.copy(alpha = 0.4f),
                thickness = 0.5.dp
            )
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(SpaceSm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            null,
                            Modifier.size(16.dp),
                            tint = AppColors.Error
                        )
                        Text("Delete", fontSize = 14.sp, color = AppColors.Error)
                    }
                },
                onClick = {
                    onDeleteClick()
                    hasDeleteConfirmed = true
                    showMenu = false
                }
            )
        }
    }
}

// ─── Segments ─────────────────────────────────────────────────────────────────

sealed class Segment {
    data class Markdown(val content: String) : Segment()
    data class CodeBlock(val language: String, val code: String) : Segment()
}

fun parseSegments(input: String): List<Segment> {
    val result = mutableListOf<Segment>()
    val fenceRegex = Regex("""```(\w*)\n([\s\S]*?)```""")
    var lastEnd = 0
    fenceRegex.findAll(input).forEach { match ->
        if (match.range.first > lastEnd) result += Segment.Markdown(
            input.substring(
                lastEnd,
                match.range.first
            )
        )
        result += Segment.CodeBlock(
            match.groupValues[1].ifEmpty { "code" },
            match.groupValues[2].trimEnd()
        )
        lastEnd = match.range.last + 1
    }
    if (lastEnd < input.length) result += Segment.Markdown(input.substring(lastEnd))
    return result
}

// ─── Markdown Text ────────────────────────────────────────────────────────────

@Composable
fun MarkdownText(message: Chatora, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val segments = parseSegments(message.message)
    val isUser = message.isUser
    val textColor = if (isUser) AppColors.OnPrimary else AppColors.OnSurface

    Column(modifier = modifier) {
        segments.forEach { segment ->
            when (segment) {
                is Segment.CodeBlock -> {
                    Spacer(Modifier.height(SpaceSm))
                    CodeBlockView(language = segment.language, code = segment.code)
                    Spacer(Modifier.height(SpaceSm))
                }

                is Segment.Markdown -> {
                    val annotated = buildMarkdownAnnotatedString(segment.content, textColor)
                    if (annotated.text.isNotBlank()) {
                        Text(
                            text = annotated,
                            fontSize = 15.sp,
                            color = textColor,
                            lineHeight = 23.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── Code Block ───────────────────────────────────────────────────────────────

@Composable
fun CodeBlockView(language: String, code: String) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000); copied = false
        }
    }

    val bgColor = Color(0xFFF0F4FF)
    val headerBg = Color(0xFFE3EAFF)
    val borderColor = Color(0xFFCDD9F5)
    val codeTextColor = Color(0xFF2D3757)
    val langLabelColor = Color(0xFF4361C2)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerMd))
            .border(1.dp, borderColor, RoundedCornerShape(CornerMd))
            .background(bgColor)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .padding(horizontal = SpaceMd, vertical = SpaceSm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpaceXs)
            ) {
                // Traffic lights
                listOf(Color(0xFFFF6058), Color(0xFFFFBD2E), Color(0xFF28CA41)).forEach { c ->
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(c)
                    )
                }
                Spacer(Modifier.width(SpaceSm))
                Text(
                    text = language.lowercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = langLabelColor,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(SpaceSm))
                    .background(if (copied) AppColors.Success.copy(0.15f) else Color.Transparent)
                    .clickable {
                        context.copyToClip(clipboard, code, scope)
                        context.showToast("Code copied")
                        copied = true
                    }
                    .padding(horizontal = SpaceSm, vertical = SpaceXs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpaceXs)
            ) {
                Icon(
                    imageVector = if (copied) Icons.Default.Check else Icons.Rounded.ContentCopy,
                    contentDescription = "Copy",
                    modifier = Modifier.size(14.dp),
                    tint = if (copied) AppColors.Success else langLabelColor
                )
                Text(
                    text = if (copied) "Copied!" else "Copy",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (copied) AppColors.Success else langLabelColor,
                    fontSize = 11.sp
                )
            }
        }

        HorizontalDivider(color = borderColor, thickness = 0.5.dp)

        // Code
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = SpaceMd, vertical = SpaceMd)
        ) {
            Text(
                text = code,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = codeTextColor,
                lineHeight = 20.sp,
                softWrap = false
            )
        }
    }
}

// ─── Markdown Builder ─────────────────────────────────────────────────────────

fun buildMarkdownAnnotatedString(text: String, textColor: Color): AnnotatedString {
    val headingStyle = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
    val h3Style = SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    val boldStyle = SpanStyle(fontWeight = FontWeight.Bold, color = AppColors.Primary)
    val inlineCodeStyle = SpanStyle(
        fontFamily = FontFamily.Monospace, fontSize = 12.sp,
        background = Color(0xFFE8F0FF), color = Color(0xFF3358CC)
    )
    val bulletParagraph = ParagraphStyle(textIndent = TextIndent(restLine = 8.sp))

    return buildAnnotatedString {
        text.trimEnd().lines().forEachIndexed { index, line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("## ") -> {
                    withStyle(headingStyle) { append(trimmed.removePrefix("## ")) }
                    append("\n")
                }

                trimmed.startsWith("### ") -> {
                    withStyle(h3Style) { append(trimmed.removePrefix("### ")) }
                    append("\n")
                }

                trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                    val content = trimmed.removePrefix("* ").removePrefix("- ")
                    withStyle(bulletParagraph) {
                        append("• ")
                        processInlineStyles(content, boldStyle, inlineCodeStyle)
                    }
                    append("\n")
                }

                trimmed.isEmpty() -> append("\n")
                else -> {
                    processInlineStyles(trimmed, boldStyle, inlineCodeStyle)
                    if (index < text.trimEnd().lines().lastIndex) append("\n")
                }
            }
        }
    }
}

private fun AnnotatedString.Builder.processInlineStyles(
    text: String, boldStyle: SpanStyle, inlineCodeStyle: SpanStyle
) {
    val pattern = Regex("""\*\*(.*?)\*\*|`([^`]+)`""")
    var lastIndex = 0
    pattern.findAll(text).forEach { match ->
        if (match.range.first > lastIndex) append(text.substring(lastIndex, match.range.first))
        when {
            match.groupValues[1].isNotEmpty() -> withStyle(boldStyle) { append(match.groupValues[1]) }
            match.groupValues[2].isNotEmpty() -> withStyle(inlineCodeStyle) { append(" ${match.groupValues[2]} ") }
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < text.length) append(text.substring(lastIndex))
}