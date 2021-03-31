package ms.safi.spring.spa.devserver.proxy.ws

import io.undertow.Handlers.path
import io.undertow.Handlers.websocket
import io.undertow.Undertow
import io.undertow.websockets.core.AbstractReceiveListener
import io.undertow.websockets.core.BufferedTextMessage
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.AbstractWebSocketHandler


@SpringBootTest(
    properties = [
        "spa.devserver.proxy.enabled=true",
        "spa.devserver.proxy.port=9876",
    ],
    webEnvironment = WebEnvironment.RANDOM_PORT
)
internal class DevServerWebSocketProxyTest {
    private lateinit var server: Undertow

    @LocalServerPort
    private var port: Int = 0;

    @BeforeEach
    fun setup() {
        server = Undertow.builder()
            .addHttpListener(9876, "localhost")
            .setHandler(
                path().addPrefixPath("/sockjs-node", websocket { exchange, channel ->
                    channel.receiveSetter.set(object : AbstractReceiveListener() {
                        override fun onFullTextMessage(channel: WebSocketChannel, message: BufferedTextMessage) {
                            println("in server onFullTextMessage: ${message.data}")
                            WebSockets.sendText(message.data, channel, null)
                        }
                    })
                    channel.resumeReceives()
                })
            )
            .build()
        server.start()
    }

    @AfterEach
    fun teardown() {
        server.stop()
    }

    @Test
    fun `does things`() {
        val session = StandardWebSocketClient()
            .doHandshake(
                object : AbstractWebSocketHandler() {},
                "ws://localhost:${port}/sockjs-node"
            ).get()

        session.sendMessage(TextMessage("hi there"))
    }
}