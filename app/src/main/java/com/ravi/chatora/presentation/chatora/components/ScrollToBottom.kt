package com.ravi.chatora.presentation.chatora.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ravi.chatora.ui.theme.AppColors


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScrollToBottom(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(AppColors.PrimaryContainer)
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.ArrowDownward,
            contentDescription = "Scroll to bottom",
            tint = AppColors.Primary
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
annotation class PreviewBg

@PreviewBg
@Composable
fun Pr(modifier: Modifier = Modifier) {
    Box(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ){
        ScrollToBottom {  }
    }
}