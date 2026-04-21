package github.com.nicolasmaldonadogomez.solochat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.com.nicolasmaldonadogomez.solochat.data.ChatRepository
import github.com.nicolasmaldonadogomez.solochat.data.Message
import github.com.nicolasmaldonadogomez.solochat.data.NoteChat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    fun getLastMessageForChat(chatId: Long): Flow<Message?> {
        return repository.getLastMessageForChat(chatId)
    }

    /**
     * Envía un mensaje en un chat específico.
     * Al ser una app de notas, el "envío" es simplemente guardar la nota.
     */
    fun sendMessage(chat: NoteChat, text: String, imageUrl: String? = null) {
        viewModelScope.launch {
            val message = Message(
                chatId = chat.id,
                text = text,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis()
            )
            repository.sendMessage(message, chat)
        }
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            repository.deleteMessage(message)
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
    fun deleteChat(chat: NoteChat) {
        viewModelScope.launch {
            repository.deleteChat(chat)
            if (_selectedChatId.value == chat.id) {
                _selectedChatId.value = null
            }
        }
    }
}
