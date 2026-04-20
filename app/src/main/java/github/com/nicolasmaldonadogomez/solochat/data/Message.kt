package github.com.nicolasmaldonadogomez.solochat.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa un mensaje individual dentro de un chat.
 * 
 * @param chatId Relación con la entidad NoteChat.
 * @param text Contenido del mensaje (soporta Markdown).
 * @param imageUrl URL opcional para imágenes adjuntas.
 * @param timestamp Fecha de creación para ordenar la conversación.
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = NoteChat::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE // Si se borra el chat, se borran sus mensajes
        )
    ],
    indices = [Index(value = ["chatId"])] // Índice para mejorar la velocidad de búsqueda por chat
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chatId: Long,
    val text: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
