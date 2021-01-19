package ms.safi.spring.spa.devserver.proxy.http

import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest(properties = ["spa.devserver.proxy.enabled=true"])
@AutoConfigureMockMvc
internal class DevServerProxyServletFilterTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var httpServletRequestProxy: HttpServletRequestProxy

    @Test
    fun `todo`() {
    }
}
