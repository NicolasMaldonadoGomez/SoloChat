package github.com.nicolasmaldonadogomez.solochat.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para las operaciones de la base de datos.
 * Define las consultas SQL de forma declarativa.
 */
@Dao
interface ChatDao {

    // --- Operaciones de Chat ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: NoteChat): Long

    @Query("SELECT * FROM note_chats ORDER BY isPinned DESC, lastModified DESC")
    fun getAllChats(): Flow<List<NoteChat>>

    @Query("UPDATE note_chats SET iconUrl = :iconUrl WHERE id = :chatId")
    suspend fun updateChatIcon(chatId: Long, iconUrl: String?)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT 1")
    fun getLastMessageForChat(chatId: Long): Flow<Message?>

    @Update
    suspend fun updateChat(chat: NoteChat)

    @Delete
    suspend fun deleteChat(chat: NoteChat)

    // --- Operaciones de Mensajes ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long

    /**
     * Obtiene todos los mensajes de un chat específico.
     * Se usa Flow para recibir actualizaciones en tiempo real (estilo WhatsApp).
     */
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: Long): Flow<List<Message>>

    @Update
    suspend fun updateMessage(message: Message)

    @Delete
    suspend fun deleteMessage(message: Message)

    // --- Operaciones Combinadas ---

    /**
     * Al insertar un mensaje, solemos querer actualizar el timestamp del chat 
     * para que suba al principio de la lista.
     */
    @Transaction
    suspend fun insertMessageAndUpdateChatTimestamp(message: Message) {
        insertMessage(message)
        // Podríamos obtener el chat y actualizar su timestamp aquí
        // o manejarlo desde el repositorio/ViewModel.
    }
}
