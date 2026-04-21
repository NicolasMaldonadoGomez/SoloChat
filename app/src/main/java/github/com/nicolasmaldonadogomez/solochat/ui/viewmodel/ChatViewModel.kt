package github.com.nicolasmaldonadogomez.solochat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.com.nicolasmaldonadogomez.solochat.data.ChatRepository
import github.com.nicolasmaldonadogomez.solochat.data.Message
import github.com.nicolasmaldonadogomez.solochat.data.NoteChat
import github.com.nicolasmaldonadogomez.solochat.utils.ImageStorageUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context
import java.util.Calendar

/**
 * ViewModel que gestiona la lógica de UI para la app de notas tipo WhatsApp.
 */
class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    // Estado para la lista de chats en la pantalla principal
    val allChats: StateFlow<List<NoteChat>> = repository.allChats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ID del chat seleccionado actualmente
    private val _selectedChatId = MutableStateFlow<Long?>(null)

    /**
     * Estado para los mensajes del chat seleccionado.
     * Se actualiza automáticamente cuando cambia el chat seleccionado.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedChatMessages: StateFlow<List<Message>> = _selectedChatId
        .flatMapLatest { chatId ->
            if (chatId == null) flowOf(emptyList())
            else repository.getMessagesForChat(chatId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Selecciona un chat para ver sus mensajes.
     */
    fun selectChat(chatId: Long) {
        _selectedChatId.value = chatId
    }

    /**
     * Crea un nuevo chat de notas y devuelve su ID.
     */
    suspend fun createChat(title: String): Long {
        return repository.insertChat(NoteChat(title = title))
    }

    fun renameChat(chat: NoteChat, newTitle: String) {
        viewModelScope.launch {
            repository.renameChat(chat, newTitle)
        }
    }

    fun togglePin(chat: NoteChat) {
        viewModelScope.launch {
            repository.togglePin(chat)
        }
    }

    fun updateChatIcon(context: Context, chat: NoteChat, iconUri: android.net.Uri?) {
        viewModelScope.launch {
            val oldIcon = chat.iconUrl
            val newPath = iconUri?.let { ImageStorageUtils.saveImageToInternalStorage(context, it) }
            
            repository.updateChatIcon(chat.id, newPath)
            
            // Limpieza: borrar el icono viejo si ya no se usa en ningún chat ni mensaje
            if (oldIcon != null && oldIcon != newPath) {
                cleanupOrphanedImage(context, oldIcon)
            }
        }
    }

    fun getLastMessageForChat(chatId: Long): Flow<Message?> {
        return repository.getLastMessageForChat(chatId)
    }

    /**
     * Envía un mensaje en un chat específico.
     * Al ser una app de notas, el "envío" es simplemente guardar la nota.
     */
    fun sendMessage(chat: NoteChat, text: String, imageUrl: String? = null, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val message = Message(
                chatId = chat.id,
                text = text,
                imageUrl = imageUrl,
                timestamp = timestamp
            )
            repository.sendMessage(message, chat)
        }
    }

    fun createTestData() {
        viewModelScope.launch {
            val chatId = createChat("Prueba de Fechas")
            val chat = NoteChat(id = chatId, title = "Prueba de Fechas")
            
            val calendar = Calendar.getInstance()

            // Mensajes 2022
            calendar.set(2022, Calendar.JANUARY, 15, 10, 30)
            sendMessage(chat, "Hola, este es un mensaje de 2022", timestamp = calendar.timeInMillis)
            calendar.set(2022, Calendar.JANUARY, 15, 10, 35)
            sendMessage(chat, "Otro mensaje del mismo día en 2022", timestamp = calendar.timeInMillis)
            
            calendar.set(2022, Calendar.MAY, 20, 15, 0)
            sendMessage(chat, "Mensaje de mayo 2022", timestamp = calendar.timeInMillis)

            // Mensajes 2023
            calendar.set(2023, Calendar.AUGUST, 10, 9, 0)
            sendMessage(chat, "Ya estamos en 2023", timestamp = calendar.timeInMillis)
            
            // Mensajes 2024 (Actual)
            calendar.set(2024, Calendar.JANUARY, 1, 0, 1)
            sendMessage(chat, "¡Feliz 2024!", timestamp = calendar.timeInMillis)
            
            // Mensajes 2025 (Futuro)
            calendar.set(2025, Calendar.DECEMBER, 25, 12, 0)
            sendMessage(chat, "Mensaje desde el futuro: Navidad 2025", timestamp = calendar.timeInMillis)
        }
    }

    fun deleteMessage(context: Context, message: Message) {
        viewModelScope.launch {
            val imagePath = message.imageUrl
            repository.deleteMessage(message)
            
            // Limpieza: intentar borrar la imagen si era un mensaje con foto
            if (imagePath != null) {
                cleanupOrphanedImage(context, imagePath)
            }
        }
    }

    private suspend fun cleanupOrphanedImage(context: Context, path: String) {
        ImageStorageUtils.deleteImageIfOrphaned(context, path) { imagePath ->
            // Verificamos si alguien más está usando esta imagen en la DB
            // Corrutina bloqueante controlada para el callback
            val used = kotlinx.coroutines.runBlocking {
                val isUsedInChats = repository.isImageUsedInAnyChat(imagePath)
                val isUsedInMessages = repository.isImageUsedInAnyMessage(imagePath)
                isUsedInChats || isUsedInMessages
            }
            used
        }
    }

    fun editMessage(message: Message, newText: String) {
        viewModelScope.launch {
            repository.updateMessage(message.copy(text = newText))
        }
    }

    fun pinMessage(chat: NoteChat, messageId: Long?) {
        viewModelScope.launch {
            val updatedChat = chat.copy(pinnedMessageId = messageId)
            repository.updateChat(updatedChat)
            // Emitimos el cambio manualmente si es necesario, 
            // pero el Flow de allChats lo hará automáticamente.
        }
    }

    /**
     * Borra un chat y todos sus mensajes (gracias a ForeignKey CASCADE).
     */
    fun deleteChat(context: Context, chat: NoteChat) {
        viewModelScope.launch {
            // Guardamos las referencias a imágenes antes de borrar
            val iconPath = chat.iconUrl
            val messagesWithImages = repository.getMessagesForChat(chat.id).first().mapNotNull { it.imageUrl }
            
            repository.deleteChat(chat)
            
            if (_selectedChatId.value == chat.id) {
                _selectedChatId.value = null
            }

            // Limpieza del icono
            if (iconPath != null) {
                cleanupOrphanedImage(context, iconPath)
            }
            // Limpieza de imágenes de mensajes
            messagesWithImages.forEach { path ->
                cleanupOrphanedImage(context, path)
            }
        }
    }
}
