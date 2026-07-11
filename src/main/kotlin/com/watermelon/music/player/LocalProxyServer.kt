package com.watermelon.music.player

import com.sun.net.httpserver.HttpServer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.util.concurrent.Executors

object LocalProxyServer {
    private var server: HttpServer? = null
    private val client = OkHttpClient()
    var port: Int = 8080
        private set

    fun start() {
        if (server != null) return
        
        // Find an open port starting from 8080
        var currentPort = 8080
        while (currentPort < 8100) {
            try {
                server = HttpServer.create(InetSocketAddress("127.0.0.1", currentPort), 0)
                port = currentPort
                break
            } catch (e: Exception) {
                currentPort++
            }
        }
        
        if (server == null) {
            println("🍉 Proxy Server failed to bind to any port.")
            return
        }

        server?.createContext("/stream") { exchange ->
            try {
                val query = exchange.requestURI.query
                val urlParam = query.split("&").firstOrNull { it.startsWith("url=") }?.substring(4)
                
                if (urlParam == null) {
                    exchange.sendResponseHeaders(400, -1)
                    return@createContext
                }

                val targetUrl = java.net.URLDecoder.decode(urlParam, "UTF-8")
                
                // Forward Range header if JavaFX requests it
                val rangeHeader = exchange.requestHeaders.getFirst("Range")
                
                val requestBuilder = Request.Builder()
                    .url(targetUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                
                if (rangeHeader != null) {
                    requestBuilder.header("Range", rangeHeader)
                }

                val response = client.newCall(requestBuilder.build()).execute()
                
                if (!response.isSuccessful) {
                    exchange.sendResponseHeaders(response.code, -1)
                    return@createContext
                }

                val responseHeaders = exchange.responseHeaders
                responseHeaders.add("Content-Type", response.header("Content-Type", "audio/mp4"))
                responseHeaders.add("Accept-Ranges", "bytes")
                
                val contentRange = response.header("Content-Range")
                if (contentRange != null) {
                    responseHeaders.add("Content-Range", contentRange)
                }

                val contentLength = response.body?.contentLength() ?: 0L
                val statusCode = if (rangeHeader != null) 206 else 200
                
                exchange.sendResponseHeaders(statusCode, if (contentLength > 0) contentLength else 0)

                response.body?.byteStream()?.use { input ->
                    exchange.responseBody.use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    exchange.sendResponseHeaders(500, -1)
                } catch (_: Exception) {}
            } finally {
                exchange.close()
            }
        }

        server?.executor = Executors.newFixedThreadPool(4)
        server?.start()
        println("🍉 Proxy Server started on port $port")
    }
}
