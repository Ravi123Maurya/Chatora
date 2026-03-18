package com.ravi.chatora.presentation.chatorahistory

import android.webkit.WebHistoryItem
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ravi.chatora.domain.models.Chatora
import com.ravi.chatora.domain.models.ChatoraHistory
import com.ravi.chatora.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatoraHistory(
    history: List<ChatoraHistory>,
    onHistoryClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chatora History") },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier,
                        onClick = {
                            onBackClick()
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
    ) { paddingValues ->
        LazyColumn(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White),

        ) {
            items(20 ){
                HistoryItem(
                    text = "",
                    i = it,
                    onClick = { onHistoryClick(it) }
                )
            }
        }
    }

}


@Composable
fun HistoryItem(
    modifier: Modifier = Modifier,
    i: Int,
    text: String,
    onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ){
        Text(
            text = "I am history $i",
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
    }
}

fun String.lengthOfFifty(): String{
    return if (this.length > 50) this.substring(0..50) else this
}


