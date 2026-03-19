package com.ravi.chatora.presentation.chatorahistory

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravi.chatora.domain.models.ChatoraHistory
import com.ravi.chatora.ui.theme.AppColors

// ─── Design tokens ─────────────────────────────────────────────────────────
private val CornerMd = 12.dp
private val CornerLg = 16.dp
private val SpaceSm = 8.dp
private val SpaceMd = 12.dp
private val SpaceLg = 16.dp

// ─── Screen ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatoraHistory(
    history: List<ChatoraHistory>,
    onHistoryClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            HistoryTopBar(onBackClick = onBackClick)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpaceLg, vertical = SpaceSm),
                placeholder = {
                    Text(
                        "Search conversations…",
                        color = AppColors.OnSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = AppColors.OnSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(CornerLg),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = AppColors.OutlineVariant.copy(alpha = 0.4f),
                    focusedContainerColor = AppColors.SurfaceContainer,
                    unfocusedContainerColor = AppColors.SurfaceContainer,
                    focusedTextColor = AppColors.OnSurface,
                    unfocusedTextColor = AppColors.OnSurface,
                    cursorColor = AppColors.Primary
                ),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )

            // Section label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpaceLg, vertical = SpaceSm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Recent",
                    color = AppColors.OnSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "20 conversations",
                    color = AppColors.OnSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }

            if (history.isEmpty()) {
                // Demo items
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = SpaceMd, vertical = SpaceSmallBottom),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(demoHistoryItems) { index, item ->
                        HistoryItemCard(
                            index = index,
                            title = item.first,
                            preview = item.second,
                            timeLabel = item.third,
                            onClick = { onHistoryClick(index) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = SpaceMd, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(history) { index, item ->
                        HistoryItemCard(
                            index = index,
                            title =  "Untitled conversation",
                            preview =  "preview",
                            timeLabel = "12/12/2012",
                            onClick = { onHistoryClick(index) }
                        )
                    }
                }
            }
        }
    }
}

private val SpaceSmallBottom = 8.dp

private val demoHistoryItems = listOf(
    Triple("How do coroutines work in Kotlin?", "Coroutines are a Kotlin feature that allows you to write async code…", "2m ago"),
    Triple("Redesign my onboarding flow", "Here are 5 ideas for improving your onboarding experience…", "1h ago"),
    Triple("Fix my LazyColumn performance issue", "The issue is likely that you're creating new lambdas on every recompose…", "3h ago"),
    Triple("Write unit tests for my ViewModel", "Here's a test for your ChatoraViewModel using MockK…", "Yesterday"),
    Triple("Explain MVVM vs MVI", "MVVM (Model-View-ViewModel) separates UI state from business logic…", "Yesterday"),
    Triple("Generate SQL for a chat app schema", "Here's a schema that handles users, conversations, and messages…", "2 days ago"),
    Triple("Translate this to Spanish", "Aquí está la traducción: Hola, ¿cómo puedo ayudarte hoy?", "3 days ago"),
    Triple("Best practices for Jetpack Compose", "Some key principles: state hoisting, side-effect handling, slot APIs…", "1 week ago"),
    Triple("Write a cover letter for Android dev role", "Dear Hiring Manager, I am excited to apply for the Android Developer…", "1 week ago"),
    Triple("Explain Retrofit interceptors", "Interceptors in Retrofit are OkHttp interceptors that allow you to…", "2 weeks ago"),
)

// ─── History Item ─────────────────────────────────────────────────────────

@Composable
fun HistoryItemCard(
    modifier: Modifier = Modifier,
    index: Int,
    title: String,
    preview: String,
    timeLabel: String,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300, delayMillis = index * 30),
        label = "alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(CornerMd))
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(CornerMd)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpaceMd, vertical = SpaceMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpaceMd)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AppColors.SurfaceContainerHigh, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.Primary.copy(alpha = 0.7f)
                )
            }

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = AppColors.OnSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = preview,
                    color = AppColors.OnSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            // Time
            Text(
                text = timeLabel,
                color = AppColors.OnSurfaceVariant.copy(alpha = 0.45f),
                fontSize = 11.sp
            )
        }
    }
}

fun String.lengthOfFifty(): String = if (this.length > 50) this.substring(0..50) else this

// ─── History Top Bar ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTopBar(onBackClick: () -> Unit) {
    Surface(
        color = AppColors.Background,
        shadowElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = AppColors.OnSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Conversations",
                    color = AppColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear history",
                        tint = AppColors.OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            HorizontalDivider(
                color = AppColors.OutlineVariant.copy(alpha = 0.4f),
                thickness = 0.5.dp
            )
        }
    }
}