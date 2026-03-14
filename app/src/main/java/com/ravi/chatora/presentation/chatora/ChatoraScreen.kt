package com.ravi.chatora.presentation.chatora

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ravi.chatora.domain.models.Chatora
import com.ravi.chatora.presentation.chatora.components.FloatingChatoraQueries
import com.ravi.chatora.presentation.chatora.components.VoiceChatDialog
import com.ravi.chatora.presentation.chatora.components.VoiceChatoraViewModel
import com.ravi.chatora.presentation.chatorahistory.ChatoraHistory
import com.ravi.chatora.ui.theme.AppColors
import com.ravi.chatora.utils.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    var messageText by remember { mutableStateOf("") }
    val chatUiState by chatoraViewModel.chatUiState.collectAsStateWithLifecycle()
    val chatoraState by chatoraViewModel.getChatoraResponseState.collectAsState()
    val voiceState by voiceChatoraViewModel.voiceState.collectAsState()

    var lastAnimatedMessageId by rememberSaveable { mutableStateOf<Long?>(null) }
    var shouldStartVoiceChat by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                context.showToast("Permission Denied")
            } else {
                context.showToast("Permission Granted")
                shouldStartVoiceChat = true
            }
        }
    )

    LaunchedEffect(Unit) {
        if (chatUiState.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    BackHandler(
        enabled = pagerState.currentPage != 0
    ) {
        if (pagerState.currentPage != 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(0)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = false
    ) { page ->
        when (page) {
            0 -> {
                Scaffold(
                    topBar = {
                        ChatoraTopBar(
                            modifier = Modifier.windowInsetsPadding(
                                WindowInsets.systemBars.only(
                                    WindowInsetsSides.Top
                                )
                            ),
                            onMenuClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        )
                    },
                    bottomBar = {
                        Column {
//                AnimatedVisibility(visible = messageText == "") {
//                    FloatingChatoraQueries(onQueryClick = { query -> messageText = query })
//                }
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
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        shouldStartVoiceChat = true
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }

                                }
                            )
                        }

                    },
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColors.Background)
                            .padding(paddingValues)
                    ) {

                        if (chatUiState.isEmpty()) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .padding(16.dp), contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "How can I help you today?",
                                    color = AppColors.Primary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 16.dp),
                                reverseLayout = true,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                itemsIndexed(chatUiState.reversed()) { index, message ->
                                    val shouldAnimate =
                                        index == 0 && message.isUser && lastAnimatedMessageId != message.id.toLong()

                                    if (shouldAnimate) {
                                        // Side effect to mark this ID as animated so it doesn't run again
                                        SideEffect {
                                            lastAnimatedMessageId = message.id.toLong()
                                        }
                                    }

                                    ChatBubble(
                                        message,
                                        shouldAnimate = shouldAnimate,
                                        onDeleteClick = {
                                            coroutineScope.launch {
                                                delay(500)
                                                chatoraViewModel.deleterChatora(message.id)
                                            }
                                            context.showToast("Delete")
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
                    onBackClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
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
            onSubmitClick = {
                messageText = voiceState.spokenText
                shouldStartVoiceChat = false
            },
            onCancelClick = {
                voiceChatoraViewModel.stopVoiceChatora()
                shouldStartVoiceChat = false
            }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatoraTopBar(modifier: Modifier, onMenuClick: () -> Unit) {

    val context = LocalContext.current


    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Profile picture
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppColors.PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "C",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                }

                Text(
                    text = "Chatora",
                    color = AppColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 4.sp
                )

            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppColors.Surface.copy(alpha = 0.1f)
        ),
        actions = {
            IconButton(
                modifier = Modifier,
                onClick = {

                    onMenuClick()
                }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "More",
                    tint = AppColors.OnSurface
                )
            }
        }
    )
}


@Composable
fun ChatoraBottomBar(
    isLoading: Boolean,
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onVoiceChatClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) {
        focusManager.clearFocus()
    }

    val context = LocalContext.current
    var hasMicClicked by remember { mutableStateOf(false) }

    LaunchedEffect(hasMicClicked) {
        if (hasMicClicked) {
            delay(1000)
            hasMicClicked = false
        }
    }

    Surface(
        modifier = Modifier.imePadding(),
        color = AppColors.Surface.copy(alpha = 0.1f),
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Surface.copy(alpha = 0.1f))
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Message input field
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Type a message...",
                        color = AppColors.OnSurfaceVariant
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.OutlineVariant,
                    focusedContainerColor = AppColors.Surface,
                    unfocusedContainerColor = AppColors.Surface,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                maxLines = 4,
                trailingIcon = {
                    Row(Modifier.padding(8.dp, 24.dp)) {

                        IconButton(
                            modifier = Modifier
                                .clip(CircleShape)
                                .border(1.dp, Color.LightGray, CircleShape)
                                .background(AppColors.Primary.copy(alpha = 0.1f)),
                            onClick = {
                                hasMicClicked = true
                                onVoiceChatClick()
                            }// TODO: Add Voice Chat Feature
                        ) {
                            if (hasMicClicked) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = AppColors.Primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Recording",
                                    tint = Color.Black
                                )
                            }

                        }
                        Spacer(Modifier.width(8.dp))

                        val sendButtonColor =
                            if (messageText.isNotBlank()) AppColors.Primary else AppColors.Primary.copy(
                                alpha = 0.1f
                            )
                        val borderColor =
                            if (messageText.isNotBlank()) Color.Transparent else Color.LightGray

                        IconButton(
                            modifier = Modifier
                                .clip(CircleShape)
                                .border(1.dp, borderColor, CircleShape)
                                .background(sendButtonColor),
                            onClick = { if (!isLoading) onSendClick() }
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = AppColors.Primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Send",
                                    tint = if (messageText.isNotBlank()) Color.White else Color.Black
                                )
                            }

                        }
                    }

                },
            )

        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BottomBarPreview() {
    ChatoraBottomBar(false, "", {}, {}, {})
}

@Composable
fun ChatBubble(
    message: Chatora,
    shouldAnimate: Boolean,
    onDeleteClick: () -> Unit = {}
) {

    var hasDeleteConfirmed by remember { mutableStateOf(false) }
    var hasSwiped by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()


    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it != SwipeToDismissBoxValue.Settled && !hasSwiped) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                hasSwiped = true
            }
            true
        },
        positionalThreshold = { distance -> distance * 0.6f }
    )

    val alphaAnim =
        remember { Animatable(if (shouldAnimate && message.isUser) 0f else 1f) }
    val scaleAnim =
        remember { Animatable(if (shouldAnimate && message.isUser) 0.5f else 1f) }
    val offsetYAnim =
        remember { Animatable(if (shouldAnimate && message.isUser) 300f else 0f) }
    val offsetXAnim =
        remember { Animatable(if (shouldAnimate && message.isUser) 100f else 0f) }

    LaunchedEffect(Unit) {
        if (message.isUser && shouldAnimate) {
            launch {
                alphaAnim.animateTo(1f, animationSpec = tween(300))
            }
            launch {
                // OvershootInterpolator gives it a nice "pop" effect
                scaleAnim.animateTo(
                    1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            }
            launch {
                // Slide up to 0
                offsetYAnim.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessLow))
            }
            launch {
                // Slide left to 0 (since user msg is on right, we start a bit to the right)
                offsetXAnim.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessLow))
            }
        }
    }

    LaunchedEffect(Unit) {
        hasSwiped = false
        swipeState.reset()
    }
    Box(
        modifier = Modifier
            .graphicsLayer {
                alpha = alphaAnim.value
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
                translationY = offsetYAnim.value
                translationX = offsetXAnim.value
            }
    ) {
        AnimatedVisibility(
            visible = !hasDeleteConfirmed,
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 500),
                shrinkTowards = Alignment.Top
            )
        ) {
            SwipeToDismissBox(
                modifier = Modifier.fillMaxWidth(),
                state = swipeState,
                backgroundContent = {
                    if (hasSwiped) {
                        SwipeBackground(
                            modifier = Modifier.fillMaxSize(),
                            onCancelClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    hasSwiped = false
                                    swipeState.reset()
                                }
                            },
                            onDeleteClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    delay(500)
                                    onDeleteClick()
                                    hasDeleteConfirmed = true
                                }
                            }
                        )
                    }


                },
                enableDismissFromEndToStart = message.isUser,
                enableDismissFromStartToEnd = !message.isUser
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = if (message.isUser) 16.dp else 8.dp, end = 16.dp),
                    horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!message.isUser) {
                        Icon(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AppColors.Primary.copy(alpha = 0.1f)),
                            imageVector = Icons.Default.AcUnit,
                            contentDescription = "",
                            tint = AppColors.Primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isUser) 16.dp else 4.dp,
                            bottomEnd = if (message.isUser) 4.dp else 16.dp
                        ),
                        color = if (message.isUser) AppColors.Primary else AppColors.SurfaceContainerHigh,
                        shadowElevation = 1.dp,
                        modifier = if (message.isUser) Modifier.widthIn(max = 280.dp) else Modifier.weight(
                            1f
                        )
                    ) {
                        SelectionContainer {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {

                                MarkdownText(message)

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = message.timeStamp,
                                    fontSize = 11.sp,
                                    color = if (message.isUser) {
                                        AppColors.OnPrimary.copy(alpha = 0.7f)
                                    } else {
                                        AppColors.OnSurfaceVariant
                                    },
                                    modifier = Modifier.align(Alignment.End)
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
fun MarkdownText(
    message: Chatora,
    modifier: Modifier = Modifier
) {

    val markdownText = message.message

    // Define the styles you want to apply
    val headingStyle = SpanStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
    val boldStyle = SpanStyle(
        fontWeight = FontWeight.Bold,
        color = AppColors.Primary // Or any color you like
    )
    val bulletParagraphStyle = ParagraphStyle(
        textIndent = TextIndent(restLine = 28.sp)
    )

    // The core logic: build the AnnotatedString
    val annotatedString = buildAnnotatedString {
        // Split the text by lines to process each one
        val lines = markdownText.lines()

        lines.forEach { line ->
            when {
                // Rule 1: Headline
                line.startsWith("## ") || line.startsWith("### ") -> {
                    withStyle(style = headingStyle) {
                        // Append the text after "## "
                        append(line.substring(3) + "\n\n")
                    }
                }

                // Rule 2: Bullet Point
                line.startsWith("* ") -> {
                    withStyle(style = bulletParagraphStyle) {
                        append("  •  ") // Append bullet character
                        // Process the rest of the line for other styles (like bold)

                        processInlineStyles(line.substring(2), boldStyle)
                        append("\n") // Add a newline after the bullet point
                    }
                }

                // Rule 3: Regular Paragraph
                else -> {
                    // Process the line for any inline styles (like bold)
                    processInlineStyles(line, boldStyle)
                    append("\n") // Treat as a paragraph break
                }
            }
        }
    }



    Text(
        modifier = Modifier,
        text = annotatedString,
        fontSize = 15.sp,
        color = if (message.isUser) AppColors.OnPrimary else AppColors.OnSurface,
        lineHeight = 20.sp,
    )


}


private fun AnnotatedString.Builder.processInlineStyles(
    text: String,
    boldStyle: SpanStyle
) {
    // Regex to find text wrapped in double asterisks, e.g., **text**
    val boldRegex = """\*\*(.*?)\*\*""".toRegex()

    var lastIndex = 0
    boldRegex.findAll(text).forEach { matchResult ->
        // 1. Append the text before the bold part
        append(text.substring(lastIndex, matchResult.range.first))

        // 2. Append the bold text with style
        withStyle(style = boldStyle) {
            append(matchResult.groupValues[1]) // groupValues[1] is the text inside **...**
        }

        // 3. Update the last processed index
        lastIndex = matchResult.range.last + 1
    }

    // 4. Append any remaining text after the last bold part
    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}


@Composable
fun SwipeBackground(
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {

    Row(
        modifier = modifier
            .background(Color.LightGray.copy(alpha = 0.1f))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .background(Color.Red.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    modifier = Modifier.size(48.dp),
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .background(Color.Green.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onCancelClick) {
                Icon(
                    modifier = Modifier.size(48.dp),
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.White
                )
            }
        }

    }
}

@Composable
fun ss(modifier: Modifier = Modifier) {

}