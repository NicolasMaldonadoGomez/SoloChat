package github.com.nicolasmaldonadogomez.solochat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import github.com.nicolasmaldonadogomez.solochat.data.AppDatabase
import github.com.nicolasmaldonadogomez.solochat.data.ChatRepository
import github.com.nicolasmaldonadogomez.solochat.data.NoteChat
import github.com.nicolasmaldonadogomez.solochat.ui.screens.ChatScreen
import github.com.nicolasmaldonadogomez.solochat.ui.screens.HomeScreen
import github.com.nicolasmaldonadogomez.solochat.ui.theme.SoloChatTheme
import github.com.nicolasmaldonadogomez.solochat.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicialización manual simple (en apps reales se usa Hilt o Koin)
        val database = AppDatabase.getDatabase(this)
        val repository = ChatRepository(database.chatDao())
        val viewModel = ChatViewModel(repository)

        enableEdgeToEdge()
        setContent {
            SoloChatTheme {
                var currentChat by remember { mutableStateOf<NoteChat?>(null) }

                if (currentChat == null) {
                    HomeScreen(
                        viewModel = viewModel,
                        onChatClick = { chat ->
                            viewModel.selectChat(chat.id)
                            currentChat = chat
                        },
                        onChatCreated = { chat ->
                            viewModel.selectChat(chat.id)
                            currentChat = chat
                        }
                    )
                } else {
                    ChatScreen(
                        chat = currentChat!!,
                        viewModel = viewModel,
                        onBack = { currentChat = null },
                        onChatUpdated = { updatedChat ->
                            currentChat = updatedChat
                        }
                    )
                }
            }
        }
    }
}
