# Notas de desarrollo
## pantallas principales
Actualmente, tu aplicación tiene dos pantallas principales (ubicadas en la carpeta ui/screens):   
**1. HomeScreen:** Es la pantalla de entrada. Muestra la lista de todas tus notas (o "chats") con su nombre, el último mensaje y si están fijadas.  

**2. ChatScreen:** Es la pantalla de detalle. Se abre al tocar una nota y es donde puedes escribir y ver todos los mensajes/notas dentro de esa categoría, con ese estilo visual parecido a WhatsApp.
## Explicación de la Base de Datos (Room)
Tu sistema de datos sigue el patrón recomendado por Google, separando responsabilidades en varios archivos:
### 1. Las Entidades (Los "Qué")
   Son las tablas de tu base de datos.  
   • **NoteChat.kt:** Define la tabla de "Conversaciones". Guarda el título, la fecha de modificación, si está fijada (isPinned) y su ID único.  
   • **Message.kt:** Define la tabla de "Mensajes". Cada mensaje sabe a qué chat pertenece gracias a un chatId. Guarda el texto, la fecha y opcionalmente una imagen.    
### 2. El DAO - ChatDao.kt (El "Cómo")
   DAO significa Data Access Object. Es la interfaz donde escribes las consultas SQL. Aquí es donde definimos que queremos las notas ordenadas primero por las fijadas y luego por fecha. Es el "traductor" entre el código Kotlin y la base de datos SQLite.  
### 3. La Base de Datos - AppDatabase.kt (El "Contenedor")
   Es el corazón del sistema. Define qué tablas existen y la versión del esquema. Incluye la configuración fallbackToDestructiveMigration(), que es lo que nos permitió actualizar la base de datos a la versión 2 sin errores complejos al añadir el campo de "Fijar".
### 4. El Repositorio - ChatRepository.kt (El "Mediador")
   Su trabajo es ser la única fuente de verdad para los datos. El resto de la app no le pregunta directamente a la base de datos, le pregunta al Repositorio. Esto facilita mucho si en el futuro decides guardar los datos en la nube (Firebase, por ejemplo), ya que solo tendrías que cambiar este archivo.
### 5. El ViewModel - ChatViewModel.kt (El "Cerebro de la UI")
   No es parte de la base de datos técnicamente, pero es quien pide los datos al Repositorio y los prepara para que Compose los pinte en pantalla. Maneja el "estado" (qué chat está seleccionado, qué mensajes se muestran ahora mismo).
   
## El tema
Claro, aquí tienes el contenido estructurado y formateado en Markdown con tablas para una lectura clara.

***

## 🎨 Diseño de Componentes y Atributos de Color

### 🏠 HomeScree & ChatScree (Estructura General)

| Elemento UI | Atributo de Color (MaterialTheme.colorScheme) |
| :--- | :--- |
| **Texto del Título** | `onPrimaryContainer` (automático por `TopAppBar`) |
| **Fondo de la Pantalla** | `background` |
| **Iconos / Botones de Acción** | `primary` |
| **Botón Flotante (FAB)** | `primary` (fondo) y `onPrimary` (icono) |

### 💬 ChatScree (Detalles del Chat)

| Elemento UI | Atributo de Color |
| :--- | :--- |
| **Fondo del Chat** | `background` |
| **Burbuja de mi Mensaje** | `primaryContainer` |
| **Texto de mi Mensaje** | `onPrimaryContainer` |
| **Hora del Mensaje** | `onPrimaryContainer` (con transparencia) |
| **Texto "en línea"** | `primary` |

### 🌐 Valores por Tema (Configuración Actual)

| Atributo / Tema | Dark | Matrix | Telegram | WhatsApp |
| :--- | :--- | :--- | :--- | :--- |
| **`primaryContainer` (Barra / Burbuja)** | `DarkBlueBubbles` (Azul oscuro) | `MatrixDarkGreen` (Verde oscuro) | `TelegramBubbleBlue` (Azul claro) | `WhatsAppLightGreen` (Verde claro) |
| **`onPrimaryContainer` (Texto Burbuja)** | `Color.White` | `MatrixGreen` (Verde neón) | `Color.Black` | `Color.Black` |
| **`background` (Fondo Chat)** | `DarkGreyBackground` (Gris) | `Color.Black` (Negro) | `TelegramBlue` (Azul Lochmara) | `WhatsAppBackground` (Arena) |

***

### 🖼️ ¿Cómo añadir imágenes de fondo?
Si decides añadir imágenes reales a la carpeta `res/drawable`, puedes usar este patrón en `ChatScreen.kt`:

1. **Definir el recurso:** Crear una variable que elija el ID del drawable según el tema.
2. **Componente Image:** Usar `Image(...)` con `contentScale = ContentScale.Crop` dentro del `Box` principal del chat.
3. **Opacidad:** Puedes usar `alpha = 0.5f` en la imagen si quieres que el fondo sea más sutil y no distraiga de los mensajes.

***

**Nota Técnica:** En el código, el color del título de la barra superior se maneja internamente por el componente `TopAppBar` usando `onPrimaryContainer` cuando el fondo es `primaryContainer`, asegurando que siempre sea legible.