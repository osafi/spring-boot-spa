package ms.safi.spring.spa.devserver.proxy.http

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

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

    @Test
    fun `request filtered through spring security filter chain before proxied`() {
        mockMvc.get("/secure-route")
            .andExpect {
                status { isForbidden() }
            }

        verify(exactly = 0) {
            httpServletRequestProxy(any(), any())
        }
    }

    @TestConfiguration
    @EnableWebSecurity
    class TestConfig {
        @Bean
        fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
            return http.authorizeRequests { authConfig ->
                authConfig.antMatchers("/secure-route").authenticated()
                authConfig.anyRequest().permitAll()
            }.build()
        }
    }
}
