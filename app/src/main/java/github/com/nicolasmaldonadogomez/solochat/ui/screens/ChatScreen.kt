package github.com.nicolasmaldonadogomez.solochat.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.com.nicolasmaldonadogomez.solochat.data.Message
import github.com.nicolasmaldonadogomez.solochat.data.NoteChat
import github.com.nicolasmaldonadogomez.solochat.ui.components.RenameDialog
import github.com.nicolasmaldonadogomez.solochat.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chat: NoteChat,
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    onChatUpdated: (NoteChat) -> Unit
) {
    val messages by viewModel.selectedChatMessages.collectAsState()
    var textState by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf(false) }
    var messageToEdit by remember { mutableStateOf<Message?>(null) }
    var messageToOptions by remember { mutableStateOf<Message?>(null) }

    val pinnedMessage = messages.find { it.id == chat.pinnedMessageId }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column(modifier = Modifier.clickable { showRenameDialog = true }) {
                            Text(chat.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("en línea", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                if (pinnedMessage != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Mensaje fijado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(pinnedMessage.text, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                            IconButton(onClick = { 
                                viewModel.pinMessage(chat, null)
                                onChatUpdated(chat.copy(pinnedMessageId = null))
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Desfijar")
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            ChatBottomBar(
                text = if (messageToEdit != null) "" else textState,
                onTextChange = { textState = it },
                onSend = {
                    if (textState.isNotBlank()) {
                        viewModel.sendMessage(chat, textState)
                        textState = ""
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        onLongClick = { messageToOptions = message }
                    )
                }
            }
        }
    }

    if (showRenameDialog) {
        RenameDialog(
            initialName = chat.title,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                viewModel.renameChat(chat, newName)
                onChatUpdated(chat.copy(title = newName))
                showRenameDialog = false
            }
        )
    }

    if (messageToOptions != null) {
        AlertDialog(
            onDismissRequest = { messageToOptions = null },
            title = { Text("Opciones de mensaje") },
            text = { Text("¿Qué deseas hacer con este mensaje?") },
            confirmButton = {
                TextButton(onClick = { 
                    messageToEdit = messageToOptions
                    textState = messageToOptions?.text ?: ""
                    messageToOptions = null 
                }) {
                    Text("Editar")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { 
                        viewModel.deleteMessage(messageToOptions!!)
                        messageToOptions = null 
                    }) {
                        Text("Borrar", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { 
                        val newPinnedId = if (chat.pinnedMessageId == messageToOptions?.id) null else messageToOptions?.id
                        viewModel.pinMessage(chat, newPinnedId)
                        onChatUpdated(chat.copy(pinnedMessageId = newPinnedId))
                        messageToOptions = null 
                    }) {
                        Text(if (chat.pinnedMessageId == messageToOptions?.id) "Desfijar" else "Fijar")
                    }
                }
            }
        )
    }

    if (messageToEdit != null) {
        EditMessageDialog(
            initialText = messageToEdit!!.text,
            onDismiss = { messageToEdit = null },
            onConfirm = { newText ->
                viewModel.editMessage(messageToEdit!!, newText)
                messageToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(message: Message, onLongClick: () -> Unit) {
    val isMine = true 

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isMine) 12.dp else 0.dp,
                bottomEnd = if (isMine) 0.dp else 12.dp
            ),
            tonalElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .combinedClickable(
                    onClick = { /* Opcional: ver detalles */ },
                    onLongClick = onLongClick
                )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = message.text, 
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun EditMessageDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar mensaje") },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ChatBottomBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.imePadding()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* TODO: Adjuntar imagen */ }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Adjuntar")
                }
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = { Text("Escribe un mensaje") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FloatingActionButton(
                onClick = onSend,
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
