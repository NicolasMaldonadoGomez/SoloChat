# Notas de desarrollo
## Pantallas principales
Actualmente, tu aplicación tiene dos pantallas principales (ubicadas en la carpeta ui/screens):   
**1. HomeScreen:** Es la pantalla de entrada. Muestra la lista de todas tus notas (o "chats") con su nombre, el último mensaje y si están fijadas.  

**2. ChatScreen:** Es la pantalla de detalle. Se abre al tocar una nota y es donde puedes escribir y ver todos los mensajes/notas dentro de esa categoría, con ese estilo visual parecido a WhatsApp.
## Explicación de la Base de Datos (Room)
Tu sistema de datos sigue el patrón recomendado por Google, separando responsabilidades en varios archivos:
### 1. Las Entidades (Los "Qué")
   Son las tablas de tu base de datos.  
   • **NoteChat.kt:** Define la tabla de "Conversaciones". Guarda el título, la fecha de modificación, si está fijada (isPinned), el ID del mensaje fijado y la **URL del icono (iconUrl)**.  
   • **Message.kt:** Define la tabla de "Mensajes". Cada mensaje sabe a qué chat pertenece gracias a un chatId. Guarda el texto, la fecha y opcionalmente una imagen.    
### 2. El DAO - ChatDao.kt (El "Cómo")
   DAO significa Data Access Object. Es la interfaz donde escribes las consultas SQL. Aquí definimos operaciones como insertar mensajes, renombrar chats y **actualizar el icono del chat**. Es el "traductor" entre el código Kotlin y la base de datos SQLite.  
### 3. La Base de Datos - AppDatabase.kt (El "Contenedor")
   Es el corazón del sistema. Define qué tablas existen y la versión del esquema. Actualmente estamos en la **versión 7** tras añadir el soporte para iconos personalizados.
### 4. El Repositorio - ChatRepository.kt (El "Mediador")
   Su trabajo es ser la única fuente de verdad para los datos. Abstrae la base de datos Room. Cuando actualizamos un icono, el repositorio se asegura de que el DAO ejecute la orden correctamente.
### 5. El ViewModel - ChatViewModel.kt (El "Cerebro de la UI")
   Maneja el estado. Cuando eliges una foto, el ViewModel recibe la URI y le pide al repositorio que la guarde. Como los chats se observan como un `Flow`, la pantalla se actualiza sola al instante.

## 📸 Personalización de Iconos
Hemos implementado la capacidad de poner fotos a tus chats usando la librería **Coil**.

### ¿Cómo funciona técnicamente?
1. **Selección:** Usamos el contrato `PickVisualMedia` de Android. Esto abre la galería de forma segura sin pedir permisos de almacenamiento globales.
2. **Almacenamiento:** Solo guardamos la **URI** de la imagen en la tabla `note_chats`. 
   * *Ubicación real:* La foto permanece en la galería del usuario. SoloChat solo guarda la "dirección" para leerla.
3. **Visualización:** Usamos `AsyncImage` de Coil. Esta herramienta carga la imagen de forma eficiente, la recorta en círculo y gestiona la memoria para que la app no se vuelva lenta.
4. **Fallback:** Si un chat no tiene foto, el sistema muestra automáticamente `Icons.Default.AccountCircle`.

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


## Guía Visual de Colores por Pantalla

Aquí tienes el detalle exacto de qué color de tu código (`Theme.kt`) se usa en cada rincón de la aplicación:

### 🏠 HomeScreen
*   **Barra superior (Título)**
    *   Color de fondo: `primaryContainer`
    *   Color de letras título: `onPrimaryContainer`
    *   Ícono de engranaje: `onSurfaceVariant`
*   **Lista de Chats**
    *   Color de fondo: `background`
    *   Color de fuente principal (Nombre de la nota): `onBackground`
    *   Color de fuente segunda línea y hora: `outline`
    *   Color de separador: `outline` (grosor 0.5dp)
*   **Botón nuevo chat (FAB)**
    *   Color de fondo: `primaryContainer`
    *   Color del signo de más (+): `onPrimaryContainer`

### 💬 ChatScreen
*   **Barra superior (Título)**
    *   Color de fondo: `primaryContainer`
    *   Color de letras título: `onPrimaryContainer`
    *   Subtítulo ("en línea"): `primary`
*   **Lugar de mensajes (Fondo)**
    *   Color de fondo: `background`
*   **Mensajes (Burbujas)**
    *   Color de fondo: `primaryContainer` (tus mensajes)
    *   Color de fuente principal: `onPrimaryContainer`
    *   Color de fuente hora: `onPrimaryContainer` (con 60% de opacidad)
*   **Barra inferior y escritura**
    *   Color de fondo base: `surface`
    *   Burbuja para escribir: `surfaceVariant`
    *   Color de fuente principal: `onSurfaceVariant`
    *   Color del icono de clip (adjuntar): `onSurfaceVariant`
*   **Botón Enviar (Círculo)**
    *   Color de Fondo: `primary`
    *   Color del Símbolo de flecha: `onPrimary`

### 🛠️ Diálogos (Renombrar / Opciones)
*   **Color de fondo:** `surface`
*   **Color de Título y Letra:** `onSurface`
*   **Acción de Borrar:** `error` (Rojo)

> **Nota:** Estos colores se adaptan automáticamente cuando cambias el tema en la configuración (WhatsApp, Telegram, Matrix, etc.) porque están vinculados a los nombres del `ColorScheme`.
