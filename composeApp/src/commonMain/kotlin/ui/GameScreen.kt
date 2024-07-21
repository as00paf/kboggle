package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import viewmodels.GameScreenViewModel

@Composable
@Preview
fun GameScreen(viewModel: GameScreenViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray) // Background color
    ) { // Background color
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Text(
                text = "Game",
                fontSize = 32.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}
