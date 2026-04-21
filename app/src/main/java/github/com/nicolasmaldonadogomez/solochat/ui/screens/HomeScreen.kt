package github.com.nicolasmaldonadogomez.solochat.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import github.com.nicolasmaldonadogomez.solochat.R
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
    var isCreationDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showThemesSubMenu by remember { mutableStateOf(false) }
    var showLanguagesSubMenu by remember { mutableStateOf(false) }
    var showFontSizeSubMenu by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? Activity
    val fontSizeScale by themeViewModel.fontSizeState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showSettingsMenu = true }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.config_desc))
                    }
                    
                    // Menú principal de Settings
                    DropdownMenu(
                        expanded = showSettingsMenu,
                        onDismissRequest = { showSettingsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.themes)) },
                            onClick = { 
                                showSettingsMenu = false
                                showThemesSubMenu = true 
                            },
                            trailingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.languages)) },
                            onClick = { 
                                showSettingsMenu = false
                                showLanguagesSubMenu = true 
                            },
                            trailingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.font_size)) },
                            onClick = { 
                                showSettingsMenu = false
                                showFontSizeSubMenu = true 
                            },
                            trailingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.turn_off), color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                activity?.finish()
                            },
                            leadingIcon = { Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }

                    // Submenú de Temas
                    DropdownMenu(
                        expanded = showThemesSubMenu,
                        onDismissRequest = { showThemesSubMenu = false }
                    ) {
                        val currentTheme by themeViewModel.themeState.collectAsState()
                        AppTheme.entries.forEach { theme ->
                            DropdownMenuItem(
                                text = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    themeViewModel.changeTheme(theme)
                                    showThemesSubMenu = false
                                },
                                trailingIcon = {
                                    if (currentTheme == theme) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.back)) },
                            onClick = {
                                showThemesSubMenu = false
                                showSettingsMenu = true
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                        )
                    }

                    // Submenú de Idiomas
                    DropdownMenu(
                        expanded = showLanguagesSubMenu,
                        onDismissRequest = { showLanguagesSubMenu = false }
                    ) {
                        val currentLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.language_en)) },
                            onClick = {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                                showLanguagesSubMenu = false
                            },
                            trailingIcon = {
                                if (currentLocale.startsWith("en")) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.language_es)) },
                            onClick = {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))
                                showLanguagesSubMenu = false
                            },
                            trailingIcon = {
                                if (currentLocale.startsWith("es")) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.back)) },
                            onClick = {
                                showLanguagesSubMenu = false
                                showSettingsMenu = true
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                        )
                    }

                    // Submenú de Tamaño de Letra
                    DropdownMenu(
                        expanded = showFontSizeSubMenu,
                        onDismissRequest = { showFontSizeSubMenu = false }
                    ) {
                        val currentScale by themeViewModel.fontSizeState.collectAsState()
                        val sizes = listOf(
                            0.8f to stringResource(R.string.font_size_very_small),
                            0.9f to stringResource(R.string.font_size_small),
                            1.0f to stringResource(R.string.font_size_normal),
                            1.2f to stringResource(R.string.font_size_large),
                            1.4f to stringResource(R.string.font_size_very_large)
                        )
                        
                        sizes.forEach { (scale, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    themeViewModel.changeFontSize(scale)
                                    showFontSizeSubMenu = false
                                },
                                trailingIcon = {
                                    if (currentScale == scale) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.back)) },
                            onClick = {
                                showFontSizeSubMenu = false
                                showSettingsMenu = true
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                isCreationDialog = true
                showRenameDialog = NoteChat(title = "")
            }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_chat))
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
                    onRename = { 
                        isCreationDialog = false
                        showRenameDialog = chat 
                    },
                    onPin = { viewModel.togglePin(chat) },
                    viewModel = viewModel,
                    fontSizeScale = fontSizeScale
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = (16 * fontSizeScale).dp), thickness = 0.5.dp)
            }
        }
    }

    if (showRenameDialog != null) {
        RenameDialog(
            initialName = showRenameDialog!!.title,
            title = if (isCreationDialog) stringResource(R.string.new_note) else stringResource(R.string.rename),
            onDismiss = { showRenameDialog = null },
            onConfirm = { newName ->
                val finalName = if (newName.isBlank()) {
                    if (isCreationDialog) "Nueva Nota ${chats.size + 1}" else showRenameDialog!!.title
                } else newName

                scope.launch {
                    if (isCreationDialog) {
                        val id = viewModel.createChat(finalName)
                        onChatCreated(NoteChat(id = id, title = finalName))
                    } else {
                        viewModel.renameChat(showRenameDialog!!, finalName)
                    }
                    showRenameDialog = null
                }
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
    viewModel: ChatViewModel,
    fontSizeScale: Float = 1.0f
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
                .padding((16 * fontSizeScale).dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chat.isPinned) {
                        Icon(
                            Icons.Default.PushPin, 
                            contentDescription = stringResource(R.string.pinned_message),
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
                    text = lastMessage?.text ?: stringResource(R.string.placeholder_message),
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
                text = { Text(stringResource(R.string.rename)) },
                onClick = {
                    showMenu = false
                    onRename()
                },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text(if (chat.isPinned) stringResource(R.string.unpin) else stringResource(R.string.pin)) },
                onClick = {
                    showMenu = false
                    onPin()
                },
                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
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
