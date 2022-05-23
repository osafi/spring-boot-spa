package ms.safi.spring.spa.servlet

import ms.safi.spring.spa.SpaIntegrationTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment

@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.main.web-application-type=servlet"
    ]
)
internal class SpaWebMvcIntegrationTest : SpaIntegrationTest()