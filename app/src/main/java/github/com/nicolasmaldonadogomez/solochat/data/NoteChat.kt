package github.com.nicolasmaldonadogomez.solochat.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa una conversación o "chat" de notas.
 * Similar a una entrada en la lista principal de WhatsApp.
 */
@Entity(
    tableName = "note_chats",
    indices = [Index(value = ["pinnedMessageId"])]
)
data class NoteChat(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val lastModified: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val pinnedMessageId: Long? = null
)
