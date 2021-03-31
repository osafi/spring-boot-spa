package ms.safi.spring.spa.devserver.proxy.ws

import ms.safi.spring.spa.devserver.proxy.DevServerProxyConfigurationProperties
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


class DevServerWebSocketProxy(properties: DevServerProxyConfigurationProperties) : AbstractWebSocketHandler() {
    private val devServerWsUrl = "ws://localhost:${properties.port}/sockjs-node"

    private val serverSocketSessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun afterConnectionEstablished(clientSession: WebSocketSession) {
        serverSocketSessions.computeIfAbsent(clientSession.id) {
            StandardWebSocketClient()
                .doHandshake(MessageForwardingSocketHandler(clientSession), devServerWsUrl)[1000, TimeUnit.MILLISECONDS]
        }
    }

    inner class MessageForwardingSocketHandler(private val clientSession: WebSocketSession) :
        AbstractWebSocketHandler() {
        override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
            if (clientSession.isOpen) {
                clientSession.sendMessage(message)
            }
        }
    }

    override fun handleMessage(clientSession: WebSocketSession, message: WebSocketMessage<*>) {
        serverSocketSessions.getValue(clientSession.id).sendMessage(message)
    }

    override fun afterConnectionClosed(clientSession: WebSocketSession, status: CloseStatus) {
        serverSocketSessions.remove(clientSession.id)?.use { it.close(status) }
    }
}
