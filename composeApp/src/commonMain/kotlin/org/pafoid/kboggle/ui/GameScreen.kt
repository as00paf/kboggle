package org.pafoid.kboggle.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.pafoid.kboggle.viewmodels.GameScreenViewModel

@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
fun GameScreen() {
    val viewModel: GameScreenViewModel = koinViewModel()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Background color
    ) { // Background color
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Row with Labels
            LazyVerticalGrid(
                columns= GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {
                item{
                    wordsIndicator(viewModel)
                }
                item{
                    pointsIndicator(viewModel)
                }
            }

            timer(viewModel, Modifier.fillMaxWidth().padding(top = 16.dp))
            letterGrid(viewModel.letters)
            inputTextField(viewModel)
            virtualKeyboard(viewModel)
        }
    }
}

@Composable
fun virtualKeyboard(viewModel: GameScreenViewModel) {

}

@Composable
fun inputTextField(viewModel: GameScreenViewModel, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Card(
            backgroundColor = Color.LightGray,
            border = BorderStroke(1.dp, Color.DarkGray),
            elevation = 4.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text(
                text = "Entrez un mot",
                modifier = Modifier
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun timer(viewModel: GameScreenViewModel, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Card(
            backgroundColor = Color.LightGray,
            border = BorderStroke(1.dp, Color.DarkGray),
            elevation = 4.dp,
            modifier = Modifier.requiredWidth(100.dp)
        ) {
            Text(
                text = "0:00",
                modifier = Modifier
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                fontSize = 24.sp
            )
        }
    }
}

@Composable
fun wordsIndicator(viewModel: GameScreenViewModel, modifier: Modifier = Modifier) {
    Card(
        backgroundColor = Color.LightGray,
        border = BorderStroke(1.dp, Color.DarkGray),
        elevation = 4.dp,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = "0/21 mots",
            modifier = Modifier
                .padding(16.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun pointsIndicator(viewModel: GameScreenViewModel, modifier: Modifier = Modifier) {
    Card(
        backgroundColor = Color.LightGray,
        border = BorderStroke(1.dp, Color.DarkGray),
        elevation = 4.dp,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = "0/211 points",
            modifier = Modifier
                .padding(16.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun letterGrid(letters:List<String>) {
    val size = 4

    Box(contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color.White).padding(16.dp)) {
        Card(
            backgroundColor = Color.LightGray,
            border = BorderStroke(1.dp, Color.DarkGray),
            elevation = 4.dp,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(count = size),
                modifier = Modifier.fillMaxSize().padding(4.dp)
            ) {
                items(letters.size) { index ->
                    val letter = letters[index]
                    letterItem(letter)
                }
            }
        }
    }
}

@Composable
fun letterItem(letter: String, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier.padding(4.dp).aspectRatio(1f).background(Color.White).border(1.dp, Color.DarkGray), contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.uppercase(),
            textAlign = TextAlign.Center,
            fontSize = 40.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold
        )
    }
}
