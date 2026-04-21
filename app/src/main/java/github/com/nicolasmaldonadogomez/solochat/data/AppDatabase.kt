package github.com.nicolasmaldonadogomez.solochat.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * La base de datos principal de la aplicación.
 * Define las entidades y la versión del esquema.
 */
@Database(
    entities = [NoteChat::class, Message::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Proporciona acceso al DAO.
     */
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Patrón Singleton para evitar múltiples instancias de la base de datos 
         * abiertas al mismo tiempo, lo cual es costoso y propenso a errores.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "solo_chat_database"
                )
                // Estrategia de migración simple para desarrollo. 
                // En producción, se deben definir migraciones reales.
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
