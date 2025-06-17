package io.githib.maikotrindade.appfortesting.ui.screen.sendmessage.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.githib.maikotrindade.appfortesting.model.User

@Composable
fun ChatScreen(user: User) {
    val viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(user))
    val messageText = viewModel.messageText
    val messages = viewModel.messages

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = user.username,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .defaultMinSize(minWidth = 48.dp)
                            .background(
                                if (msg.isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else Color.LightGray.copy(alpha = 0.25f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(12.dp)
                    ) {
                        Text(msg.text)
                    }
                }
            }
        }
        // Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = viewModel::onMessageTextChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = viewModel::sendMessage,
                enabled = messageText.isNotBlank()
            ) {
                Text("Send")
            }
        }
    }
}
