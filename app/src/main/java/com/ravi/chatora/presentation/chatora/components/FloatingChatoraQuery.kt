package com.ravi.chatora.presentation.chatora.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ravi.chatora.ui.theme.AppColors


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FloatingChatoraQueries(
    onQueryClick: (String) -> Unit = {}
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.4f))
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.width(12.dp)) }
            items(dummyQueries) {
                QueryBubble(
                    query = it,
                    onQueryClick = { onQueryClick(it) }
                )
            }
            item { Spacer(Modifier.width(12.dp)) }
        }
    }

}

//@Preview(showBackground = true, showSystemUi = true, backgroundColor = 0xFF003023)
@Composable
fun QueryBubble(query: String = "What is jetpack compose?", onQueryClick: () -> Unit = {}) {
    FloatingActionButton(
        modifier = Modifier,
        onClick = onQueryClick,
        containerColor = Color.Transparent,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(0.dp)
    ) {
        Box(
            Modifier
                .clip(CircleShape)
                .background(AppColors.PrimaryContainer)
                .clickable { onQueryClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(query, color = Color.Black)
        }
    }
}


val dummyQueries = listOf(
    "What is Jetpack compose?",
    "What is coroutines in Kotlin?",
    "How to create a simple app using Jetpack compose?"
)