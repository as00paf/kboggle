package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kboggle.composeapp.generated.resources.Res
import kboggle.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import viewmodels.LoginScreenViewModel

@Composable
@Preview
fun LoginScreen(viewModel: LoginScreenViewModel) {
    var username by rememberSaveable { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray) // Background color
    ) { // Background color
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Kolossal Boggle",
                fontSize = 32.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Image(painterResource(Res.drawable.compose_multiplatform), null, modifier = Modifier.weight(1f))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                },
                label = { Text("Nom d'utilisateur") },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.DarkGray,
                    unfocusedBorderColor = Color.DarkGray,
                    textColor = Color.DarkGray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!viewModel.isUserNameValid(username)) {
                        dialogMessage = "Vous devez entrer un nom d'utilisateur"
                        return@Button
                    }
                    viewModel.joinGame(username)
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Entrer", color = Color.White)
            }
        }
    }

    if (dialogMessage.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { dialogMessage = "" },
            title = { Text(text = "Alert") },
            text = { Text(text = dialogMessage) },
            confirmButton = {
                Button(onClick = { dialogMessage = "" }) {
                    Text(text = "OK")
                }
            }
        )
    }
}
