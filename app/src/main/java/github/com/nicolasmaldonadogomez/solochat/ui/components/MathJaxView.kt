package github.com.nicolasmaldonadogomez.solochat.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MathJaxView(
    latex: String,
    modifier: Modifier = Modifier,
    textColor: String = "white",
    isInteractive: Boolean = false,
    onTap: (() -> Unit)? = null,
    onDoubleTap: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                setBackgroundColor(0) 
                
                isVerticalScrollBarEnabled = isInteractive
                isHorizontalScrollBarEnabled = true
                settings.setSupportZoom(isInteractive)
                settings.displayZoomControls = false

                // Puente de comunicación JS -> Kotlin
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onSingleTap() { post { onTap?.invoke() } }
                    @JavascriptInterface
                    fun onDoubleTap() { post { onDoubleTap?.invoke() } }
                    @JavascriptInterface
                    fun onLongPress() { post { onLongPress?.invoke() } }
                }, "AndroidInterface")
            }
        },
        update = { webView ->
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=yes">
                    <style>
                        body {
                            color: $textColor;
                            background-color: transparent;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            margin: 0;
                            padding: 10px;
                            width: 100%;
                            height: 100%;
                            overflow-x: auto;
                            overflow-y: ${if (isInteractive) "auto" else "hidden"};
                            -webkit-user-select: none;
                            user-select: none;
                            -webkit-tap-highlight-color: transparent;
                        }
                        .math-content { font-size: 18px; text-align: center; min-width: 100%; }
                    </style>
                    <script>
                        window.MathJax = {
                            tex: { inlineMath: [['$', '$']], displayMath: [['$$', '$$']] },
                            options: { renderActions: { addMenu: [] } }, // Desactivar menú MathJax
                            chtml: { scale: 1.0, displayAlign: 'center' }
                        };

                        let lastTap = 0;
                        let tapTimeout;
                        let longPressTimeout;
                        let isDragging = false;
                        let startX, startY;

                        document.addEventListener('touchstart', (e) => {
                            isDragging = false;
                            startX = e.touches[0].clientX;
                            startY = e.touches[0].clientY;
                            longPressTimeout = setTimeout(() => {
                                if (!isDragging) AndroidInterface.onLongPress();
                            }, 600);
                        }, false);

                        document.addEventListener('touchmove', (e) => {
                            let diffX = Math.abs(e.touches[0].clientX - startX);
                            let diffY = Math.abs(e.touches[0].clientY - startY);
                            // Si se mueve más de 10px, es un arrastre (scroll), no un clic/longpress
                            if (diffX > 10 || diffY > 10) {
                                isDragging = true;
                                clearTimeout(longPressTimeout);
                            }
                        }, false);

                        document.addEventListener('touchend', (e) => {
                            clearTimeout(longPressTimeout);
                            if (isDragging) return;

                            let now = Date.now();
                            if (now - lastTap < 300) {
                                clearTimeout(tapTimeout);
                                AndroidInterface.onDoubleTap();
                                lastTap = 0;
                            } else {
                                lastTap = now;
                                tapTimeout = setTimeout(() => {
                                    AndroidInterface.onSingleTap();
                                }, 300);
                            }
                        }, false);
                    </script>
                    <script id="MathJax-script" async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"></script>
                </head>
                <body><div class="math-content">$latex</div></body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        }
    )
}
