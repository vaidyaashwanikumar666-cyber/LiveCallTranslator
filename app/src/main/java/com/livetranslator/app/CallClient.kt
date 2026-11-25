package com.livetranslator.app

import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** App-to-app call: recognized text relay server se dusre phone tak jata hai. */
class CallClient(private val events: Events) {

    interface Events {
        fun onJoined(peers: List<String>)
        fun onPeerJoined(name: String, lang: Lang)
        fun onPeerLeft(name: String)
        fun onMessage(name: String, text: String, lang: Lang)
        fun onDisconnected(reason: String)
    }

    private val main = Handler(Looper.getMainLooper())
    private val http = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .connectTimeout(90, TimeUnit.SECONDS)   // free server so jaata hai, jaagne me ~1 min
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    private var ws: WebSocket? = null
    private var closedByUser = false

    fun connect(url: String, room: String, name: String, lang: Lang) {
        closedByUser = false
        val req = Request.Builder().url(url).build()
        ws = http.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send(
                    JSONObject().put("type", "join").put("room", room)
                        .put("name", name).put("lang", lang.locale).toString()
                )
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val m = try { JSONObject(text) } catch (e: Exception) { return }
                main.post {
                    when (m.optString("type")) {
                        "joined" -> {
                            val arr = m.optJSONArray("peers")
                            val peers = (0 until (arr?.length() ?: 0)).map { arr!!.getJSONObject(it).optString("name") }
                            events.onJoined(peers)
                        }
                        "peer_joined" -> events.onPeerJoined(m.optString("name"), Languages.byLocale(m.optString("lang")))
                        "peer_left" -> events.onPeerLeft(m.optString("name"))
                        "msg" -> events.onMessage(m.optString("name"), m.optString("text"), Languages.byLocale(m.optString("lang")))
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (!closedByUser) main.post { events.onDisconnected("Connection band ho gaya") }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (!closedByUser) main.post { events.onDisconnected("Server se connect nahi ho paaya: ${t.message}") }
            }
        })
    }

    fun send(text: String) {
        ws?.send(JSONObject().put("type", "msg").put("text", text).toString())
    }

    fun close() {
        closedByUser = true
        ws?.close(1000, "bye")
        ws = null
    }
}
