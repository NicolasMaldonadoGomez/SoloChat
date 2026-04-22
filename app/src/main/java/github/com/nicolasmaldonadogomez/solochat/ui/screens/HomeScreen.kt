package github.com.nicolasmaldonadogomez.solochat.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import github.com.nicolasmaldonadogomez.solochat.R
import github.com.nicolasmaldonadogomez.solochat.data.NoteChat
import github.com.nicolasmaldonadogomez.solochat.ui.components.RenameDialog
import github.com.nicolasmaldonadogomez.solochat.ui.viewmodel.ChatViewModel
import github.com.nicolasmaldonadogomez.solochat.ui.viewmodel.theme.ThemeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: ChatViewModel,
    themeViewModel: ThemeViewModel,
    onChatClick: (NoteChat) -> Unit,
    onChatCreated: (NoteChat) -> Unit
) {
    val chats by viewModel.allChats.collectAsState(initial = emptyList())
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    var initialScrollDone by remember { mutableStateOf(false) }
    LaunchedEffect(chats) {
        if (chats.isNotEmpty() && !initialScrollDone) {
            listState.scrollToItem(0)
            initialScrollDone = true
        }
    }
    
    var showRenameDialog by remember { mutableStateOf<NoteChat?>(null) }
    var chatToChangeIcon by remember { mutableStateOf<NoteChat?>(null) }
    var previewImageUrl by remember { mutableStateOf<Any?>(null) }
    var isCreationDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showThemesSubMenu by remember { mutableStateOf(false) }
    var showLanguagesSubMenu by remember { mutableStateOf(false) }
    var showFontSizeSubMenu by remember { mutableStateOf(false) }
    
    val activity = LocalContext.current as? Activity
    val fontSizeScale by themeViewModel.fontSizeState.collectAsState()

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                chatToChangeIcon?.let { chat ->
                    viewModel.updateChatIcon(context, chat, it)
                }
            }
            chatToChangeIcon = null
        }
    )

    // Visor de Imagen a pantalla completa
    previewImageUrl?.let { model ->
        Dialog(
            onDismissRequest = { previewImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { previewImageUrl = null }
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = model,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showSettingsMenu = true }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.config_desc))
                    }
                    
                    DropdownMenu(
                        expanded = showSettingsMenu,
                        onDismissRequest = { 
                            showSettingsMenu = false
                            showThemesSubMenu = false
                            showLanguagesSubMenu = false
                            showFontSizeSubMenu = false
                        }
                    ) {
                        // Menú de Temas
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.themes))
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            },
                            onClick = { showThemesSubMenu = !showThemesSubMenu }
                        )
                        if (showThemesSubMenu) {
                            github.com.nicolasmaldonadogomez.solochat.ui.theme.AppTheme.values().forEach { theme ->
                                DropdownMenuItem(
                                    text = { Text("  • ${theme.name}", style = MaterialTheme.typography.bodySmall) },
                                    onClick = {
                                        themeViewModel.changeTheme(theme)
                                        showSettingsMenu = false
                                    }
                                )
                            }
                        }

                        // Menú de Idiomas
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.languages))
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            },
                            onClick = { showLanguagesSubMenu = !showLanguagesSubMenu }
                        )
                        if (showLanguagesSubMenu) {
                            listOf("es" to "Español", "en" to "English").forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = { Text("  • $name", style = MaterialTheme.typography.bodySmall) },
                                    onClick = {
                                        val appLocale = androidx.core.os.LocaleListCompat.forLanguageTags(code)
                                        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocale)
                                        showSettingsMenu = false
                                    }
                                )
                            }
                        }

                        // Tamaño de Fuente
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.font_size))
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.FormatSize, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            },
                            onClick = { showFontSizeSubMenu = !showFontSizeSubMenu }
                        )
                        if (showFontSizeSubMenu) {
                            listOf(0.8f to "Pequeño", 1.0f to "Normal", 1.2f to "Grande").forEach { (scale, name) ->
                                DropdownMenuItem(
                                    text = { Text("  • $name", style = MaterialTheme.typography.bodySmall) },
                                    onClick = {
                                        themeViewModel.changeFontSize(scale)
                                        showSettingsMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreationDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.app_name))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            state = listState
        ) {
            items(chats, key = { it.id }) { chat ->
                ChatItem(
                    chat = chat,
                    onClick = { onChatClick(chat) },
                    onDelete = { viewModel.deleteChat(context, chat) },
                    onRename = { showRenameDialog = chat },
                    onPin = { viewModel.togglePin(chat) },
                    onChangeIcon = { 
                        chatToChangeIcon = chat
                        photoPickerLauncher.launch("image/*")
                    },
                    viewModel = viewModel,
                    fontSizeScale = fontSizeScale,
                    onPreviewIcon = { url -> previewImageUrl = url }
                )
            }
        }

        // Diálogos
        if (isCreationDialog) {
            RenameDialog(
                initialName = "",
                onDismiss = { isCreationDialog = false },
                onConfirm = { title ->
                    scope.launch {
                        viewModel.createChat(title)
                    }
                    isCreationDialog = false
                },
                title = stringResource(R.string.new_chat)
            )
        }

        showRenameDialog?.let { chat ->
            RenameDialog(
                initialName = chat.title,
                onDismiss = { showRenameDialog = null },
                onConfirm = { newTitle ->
                    viewModel.renameChat(chat, newTitle)
                    showRenameDialog = null
                },
                title = stringResource(R.string.rename)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItem(
    chat: NoteChat, 
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onPin: () -> Unit,
    onChangeIcon: () -> Unit,
    viewModel: ChatViewModel,
    fontSizeScale: Float = 1.0f,
    onPreviewIcon: (Any) -> Unit
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del Chat con Doble Comportamiento
            Box(
                modifier = Modifier
                    .size((50 * fontSizeScale).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .combinedClickable(
                        onClick = { 
                            onPreviewIcon(chat.iconUrl ?: R.drawable.cuaderno_ico)
                        },
                        onDoubleClick = { onChangeIcon() },
                        onLongClick = { onChangeIcon() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!chat.iconUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = chat.iconUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.cuaderno_ico),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Unspecified
                    )
                }
            }

            Spacer(modifier = Modifier.width((16 * fontSizeScale).dp))

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
                text = { Text(stringResource(R.string.change_icon)) },
                onClick = {
                    showMenu = false
                    onChangeIcon()
                },
                leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) }
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

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
