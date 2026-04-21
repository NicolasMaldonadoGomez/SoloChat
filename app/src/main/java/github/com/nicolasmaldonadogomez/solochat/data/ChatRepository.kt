package github.com.nicolasmaldonadogomez.solochat.data

import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que centraliza el acceso a los datos.
 * Abstrae la fuente de datos (Room en este caso) del resto de la aplicación.
 */
class ChatRepository(private val chatDao: ChatDao) {

    // Chats
    val allChats: Flow<List<NoteChat>> = chatDao.getAllChats()

    suspend fun insertChat(chat: NoteChat): Long {
        return chatDao.insertChat(chat)
    }

    suspend fun updateChat(chat: NoteChat) {
        chatDao.updateChat(chat)
    }

    suspend fun deleteChat(chat: NoteChat) {
        chatDao.deleteChat(chat)
    }

    // Mensajes
    fun getMessagesForChat(chatId: Long): Flow<List<Message>> {
        return chatDao.getMessagesForChat(chatId)
    }

    fun getLastMessageForChat(chatId: Long): Flow<Message?> {
        return chatDao.getLastMessageForChat(chatId)
    }

    suspend fun renameChat(chat: NoteChat, newTitle: String) {
        chatDao.updateChat(chat.copy(title = newTitle))
    }

    suspend fun togglePin(chat: NoteChat) {
        chatDao.updateChat(chat.copy(isPinned = !chat.isPinned))
    }

    suspend fun updateChatIcon(chatId: Long, iconUrl: String?) {
        chatDao.updateChatIcon(chatId, iconUrl)
    }

    /**
     * Inserta un mensaje y actualiza el timestamp del chat asociado 
     * para que este suba al principio de la lista (estilo WhatsApp).
     */
    suspend fun sendMessage(message: Message, chat: NoteChat) {
        chatDao.insertMessage(message)
        // Actualizamos el chat con el timestamp del nuevo mensaje
        val updatedChat = chat.copy(lastModified = message.timestamp)
        chatDao.updateChat(updatedChat)
    }

    suspend fun deleteMessage(message: Message) {
        chatDao.deleteMessage(message)
    }

    suspend fun updateMessage(message: Message) {
        chatDao.updateMessage(message)
    }

    suspend fun isImageUsedInAnyMessage(imageUrl: String): Boolean {
        return chatDao.isImageUsedInMessages(imageUrl)
    }

    suspend fun isImageUsedInAnyChat(iconUrl: String): Boolean {
        return chatDao.isImageUsedInChats(iconUrl)
    }
}
