package github.com.nicolasmaldonadogomez.solochat.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun MathJaxView(
    latex: String,
    modifier: Modifier = Modifier,
    textColor: String = "white",
    isInteractive: Boolean = false,
    onTap: (() -> Unit)? = null
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                setBackgroundColor(0) 
                
                if (isInteractive) {
                    isVerticalScrollBarEnabled = true
                    isHorizontalScrollBarEnabled = true
                    settings.setSupportZoom(true)
                    settings.displayZoomControls = false
                    
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun performClick() {
                            post { onTap?.invoke() }
                        }
                    }, "AndroidInterface")
                } else {
                    // En el chat: Totalmente inerte
                    isClickable = false
                    isFocusable = false
                    isEnabled = false
                    // Importante: No consumir toques para que pasen a Compose
                    setOnTouchListener { _, _ -> false }
                }
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
                            ${if (isInteractive) "overflow: auto;" else "overflow: hidden;"}
                            -webkit-user-select: none;
                            user-select: none;
                        }
                        .math-content { font-size: 18px; text-align: center; min-width: 100%; }
                    </style>
                    <script>
                        window.MathJax = {
                            tex: { 
                                inlineMath: [['$', '$']], 
                                displayMath: [['$$', '$$']] 
                            },
                            options: {
                                renderActions: {
                                    addMenu: [] // DESHABILITA EL MENÚ DE MATHJAX
                                }
                            },
                            chtml: { scale: 1.0, displayAlign: 'center' }
                        };

                        if ($isInteractive) {
                            document.documentElement.onclick = function() {
                                AndroidInterface.performClick();
                            };
                        }
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
