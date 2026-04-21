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
import androidx.compose.ui.res.stringResource
import github.com.nicolasmaldonadogomez.solochat.R
import github.com.nicolasmaldonadogomez.solochat.data.Message
import github.com.nicolasmaldonadogomez.solochat.data.NoteChat
import github.com.nicolasmaldonadogomez.solochat.ui.components.RenameDialog
import github.com.nicolasmaldonadogomez.solochat.ui.viewmodel.ChatViewModel
import github.com.nicolasmaldonadogomez.solochat.ui.viewmodel.theme.ThemeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chat: NoteChat,
    viewModel: ChatViewModel,
    themeViewModel: ThemeViewModel,
    onBack: () -> Unit,
    onChatUpdated: (NoteChat) -> Unit
) {
    val messages by viewModel.selectedChatMessages.collectAsState()
    val fontSizeScale by themeViewModel.fontSizeState.collectAsState()
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
                            Text(stringResource(R.string.online), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
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
                                Text(stringResource(R.string.pinned_message), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(pinnedMessage.text, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                            IconButton(onClick = { 
                                viewModel.pinMessage(chat, null)
                                onChatUpdated(chat.copy(pinnedMessageId = null))
                            }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.unpin_desc))
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
                },
                fontSizeScale = fontSizeScale
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
                contentPadding = PaddingValues((12 * fontSizeScale).dp),
                verticalArrangement = Arrangement.spacedBy((8 * fontSizeScale).dp)
            ) {
                items(messages.size, key = { index -> messages[index].id }) { index ->
                    val message = messages[index]
                    val prevMessage = if (index > 0) messages[index - 1] else null
                    
                    val showYearHeader = shouldShowYearHeader(message, prevMessage)
                    val showDateHeader = shouldShowDateHeader(message, prevMessage)

                    if (showYearHeader) {
                        YearHeader(message.timestamp, fontSizeScale)
                    }
                    if (showDateHeader) {
                        DateHeader(message.timestamp, fontSizeScale)
                    }

                    MessageBubble(
                        message = message,
                        onLongClick = { messageToOptions = message },
                        fontSizeScale = fontSizeScale
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
            title = { Text(stringResource(R.string.message_options)) },
            text = { Text(stringResource(R.string.message_options_desc)) },
            confirmButton = {
                TextButton(onClick = { 
                    messageToEdit = messageToOptions
                    textState = messageToOptions?.text ?: ""
                    messageToOptions = null 
                }) {
                    Text(stringResource(R.string.edit))
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { 
                        viewModel.deleteMessage(messageToOptions!!)
                        messageToOptions = null 
                    }) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { 
                        val newPinnedId = if (chat.pinnedMessageId == messageToOptions?.id) null else messageToOptions?.id
                        viewModel.pinMessage(chat, newPinnedId)
                        onChatUpdated(chat.copy(pinnedMessageId = newPinnedId))
                        messageToOptions = null 
                    }) {
                        Text(if (chat.pinnedMessageId == messageToOptions?.id) stringResource(R.string.unpin) else stringResource(R.string.pin))
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
fun MessageBubble(message: Message, onLongClick: () -> Unit, fontSizeScale: Float = 1.0f) {
    val isMine = true 

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(
                topStart = (12 * fontSizeScale).dp,
                topEnd = (12 * fontSizeScale).dp,
                bottomStart = if (isMine) (12 * fontSizeScale).dp else 0.dp,
                bottomEnd = if (isMine) 0.dp else (12 * fontSizeScale).dp
            ),
            tonalElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = (300 * fontSizeScale).dp)
                .combinedClickable(
                    onClick = { /* Opcional: ver detalles */ },
                    onLongClick = onLongClick
                )
        ) {
            Column(modifier = Modifier.padding((8 * fontSizeScale).dp)) {
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
        title = { Text(stringResource(R.string.edit_message)) },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ChatBottomBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    fontSizeScale: Float = 1.0f
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.imePadding()
    ) {
        Row(
            modifier = Modifier
                .padding((8 * fontSizeScale).dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape((24 * fontSizeScale).dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = (12 * fontSizeScale).dp, vertical = (4 * fontSizeScale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* TODO: Adjuntar imagen */ }) {
                    Icon(Icons.Default.AttachFile, contentDescription = stringResource(R.string.attach_file))
                }
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = { Text(stringResource(R.string.type_message)) },
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
            
            Spacer(modifier = Modifier.width((8 * fontSizeScale).dp))
            
            FloatingActionButton(
                onClick = onSend,
                shape = RoundedCornerShape((24 * fontSizeScale).dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size((48 * fontSizeScale).dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.send))
            }
        }
    }
}

@Composable
fun YearHeader(timestamp: Long, fontSizeScale: Float = 1.0f) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = (16 * fontSizeScale).dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(timestamp)),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun DateHeader(timestamp: Long, fontSizeScale: Float = 1.0f) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = (8 * fontSizeScale).dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
            shape = RoundedCornerShape((8 * fontSizeScale).dp)
        ) {
            Text(
                text = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.getDefault()).format(Date(timestamp))
                    .replaceFirstChar { it.uppercase() },
                modifier = Modifier.padding(horizontal = (12 * fontSizeScale).dp, vertical = (4 * fontSizeScale).dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

fun shouldShowYearHeader(message: Message, prevMessage: Message?): Boolean {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val messageYear = Calendar.getInstance().apply { timeInMillis = message.timestamp }.get(Calendar.YEAR)
    
    if (messageYear == currentYear) return false
    
    if (prevMessage == null) return true
    
    val prevYear = Calendar.getInstance().apply { timeInMillis = prevMessage.timestamp }.get(Calendar.YEAR)
    return messageYear != prevYear
}

fun shouldShowDateHeader(message: Message, prevMessage: Message?): Boolean {
    if (prevMessage == null) return true
    
    val cal1 = Calendar.getInstance().apply { timeInMillis = message.timestamp }
    val cal2 = Calendar.getInstance().apply { timeInMillis = prevMessage.timestamp }
    
    return cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR) ||
           cal1.get(Calendar.DAY_OF_YEAR) != cal2.get(Calendar.DAY_OF_YEAR)
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
