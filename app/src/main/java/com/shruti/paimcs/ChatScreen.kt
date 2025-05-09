package com.shruti.paimcs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shruti.paimcs.ChatMessage
import com.shruti.paimcs.ChatViewModel

@Composable
fun ChatScreen(chatViewModel: ChatViewModel = viewModel()) {
    val messages by chatViewModel.chatMessages.collectAsState()
    var userInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        chatViewModel.initGemma()
    }

    Box(modifier = Modifier.fillMaxSize().padding(all = 10.dp)){
        Image(painter = painterResource(R.drawable.background), contentDescription = "background image", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = true,
        ) {
            items(messages.reversed()) { msg ->
                MessageBubble(msg)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row {
            TextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask your AI mentor...") }
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                chatViewModel.sendMessage(userInput)
                userInput = ""
            }) {
                Text("Send")
            }
        }
    }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = message.text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = if (message.isUser) TextAlign.End else TextAlign.Start,
            modifier = Modifier
                .padding(8.dp)
                .background(
                    if (message.isUser) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer
                )
                .padding(12.dp)
        )
    }
}