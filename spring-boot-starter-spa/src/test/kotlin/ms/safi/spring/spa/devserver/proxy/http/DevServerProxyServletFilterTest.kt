package ms.safi.spring.spa.devserver.proxy.http

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@Disabled("Using RequestMappingHandlerMapping in a filter no longer works as it did before, will need to revisit this: https://github.com/spring-projects/spring-boot/issues/28874")
@SpringBootTest(properties = ["spa.devserver.proxy.enabled=true"])
@AutoConfigureMockMvc
internal class DevServerProxyServletFilterTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var httpServletRequestProxy: HttpServletRequestProxy

    @Test
    fun `request not proxied when it can be handled by a controller`() {
        every {
            httpServletRequestProxy(any(), any())
        } returns Unit

        mockMvc.get("/test")
            .andExpect {
                status { is2xxSuccessful() }
                content { json("""{"foo":"bar"}""") }
            }

        verify(exactly = 0) {
            httpServletRequestProxy(any(), any())
        }
    }

    @Test
    fun `request not proxied when it can be handled by actuator`() {
        every {
            httpServletRequestProxy(any(), any())
        } returns Unit

        mockMvc.get("/actuator/health")
            .andDo { print() }
            .andExpect {
                status { is2xxSuccessful() }
                content { json("""{"status":"UP"}""") }
            }

        verify(exactly = 0) {
            httpServletRequestProxy(any(), any())
        }
    }

    @Test
    fun `request proxied when it cannot be handled by a handler other than resourceHandlerMapping`() {
        every {
            httpServletRequestProxy(any(), any())
        } returns Unit

        mockMvc.get("/unknown")
            .andExpect {
                status { is2xxSuccessful() }
            }

        verify(exactly = 1) {
            httpServletRequestProxy(any(), any())
        }
    }
}
