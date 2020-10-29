package ms.safi.spring.spa.devserver.proxy.ws

import ms.safi.spring.spa.devserver.DevServerConfigurationProperties
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


class DevServerWebSocketProxy(properties: DevServerConfigurationProperties) : AbstractWebSocketHandler() {
    private val devServerBaseUrl = "ws://localhost:${properties.port}"

    private val serverSocketSessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun afterConnectionEstablished(clientSession: WebSocketSession) {
        serverSocketSessions.computeIfAbsent(clientSession.id) {
            StandardWebSocketClient()
                    .doHandshake(
                            object : AbstractWebSocketHandler() {
                                override fun handleMessage(`_`: WebSocketSession, message: WebSocketMessage<*>) {
                                    if (clientSession.isOpen) {
                                        clientSession.sendMessage(message)
                                    }
                                }
                            },
                            "$devServerBaseUrl/sockjs-node"
                    )[1000, TimeUnit.MILLISECONDS]
        }
    }

    override fun handleMessage(clientSession: WebSocketSession, message: WebSocketMessage<*>) {
        serverSocketSessions.getValue(clientSession.id).sendMessage(message)
    }

    override fun afterConnectionClosed(clientSession: WebSocketSession, status: CloseStatus) {
        serverSocketSessions.getValue(clientSession.id).close(status)
        serverSocketSessions.remove(clientSession.id)
    }
}
