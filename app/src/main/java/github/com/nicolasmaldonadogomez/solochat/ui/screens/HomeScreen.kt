package github.com.nicolasmaldonadogomez.solochat.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import github.com.nicolasmaldonadogomez.solochat.data.NoteChat
import github.com.nicolasmaldonadogomez.solochat.ui.components.RenameDialog
import github.com.nicolasmaldonadogomez.solochat.ui.theme.AppTheme
import github.com.nicolasmaldonadogomez.solochat.ui.viewmodel.ChatViewModel
import github.com.nicolasmaldonadogomez.solochat.ui.viewmodel.theme.ThemeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ChatViewModel,
    themeViewModel: ThemeViewModel,
    onChatClick: (NoteChat) -> Unit,
    onChatCreated: (NoteChat) -> Unit
) {
    val chats by viewModel.allChats.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showRenameDialog by remember { mutableStateOf<NoteChat?>(null) }
    var showThemeMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SoloChat", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showThemeMenu = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                    DropdownMenu(
                        expanded = showThemeMenu,
                        onDismissRequest = { showThemeMenu = false }
                    ) {
                        AppTheme.entries.filter { it != AppTheme.MATRIX || true }.forEach { theme ->
                            DropdownMenuItem(
                                text = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    themeViewModel.changeTheme(theme)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                scope.launch {
                    val newTitle = "Nueva Nota ${chats.size + 1}"
                    val id = viewModel.createChat(newTitle)
                    onChatCreated(NoteChat(id = id, title = newTitle))
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Chat")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(chats, key = { it.id }) { chat ->
                ChatItem(
                    chat = chat, 
                    onClick = { onChatClick(chat) },
                    onDelete = { viewModel.deleteChat(chat) },
                    onRename = { showRenameDialog = chat },
                    onPin = { viewModel.togglePin(chat) },
                    viewModel = viewModel
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
        }
    }

    if (showRenameDialog != null) {
        RenameDialog(
            initialName = showRenameDialog!!.title,
            onDismiss = { showRenameDialog = null },
            onConfirm = { newName ->
                viewModel.renameChat(showRenameDialog!!, newName)
                showRenameDialog = null
            }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatItem(
    chat: NoteChat, 
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onPin: () -> Unit,
    viewModel: ChatViewModel
) {
    var showMenu by remember { mutableStateOf(false) }
    val lastMessage by viewModel.getLastMessageForChat(chat.id).collectAsState(initial = null)

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chat.isPinned) {
                        Icon(
                            Icons.Default.PushPin, 
                            contentDescription = "Fijado",
                            modifier = Modifier.size(16.dp).padding(end = 4.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = chat.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = lastMessage?.text ?: "Toca para escribir...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
            
            Text(
                text = formatTimestamp(chat.lastModified),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Cambiar nombre") },
                onClick = {
                    showMenu = false
                    onRename()
                },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text(if (chat.isPinned) "Desfijar" else "Fijar") },
                onClick = {
                    showMenu = false
                    onPin()
                },
                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Borrar", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    showMenu = false
                    onDelete()
                },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
