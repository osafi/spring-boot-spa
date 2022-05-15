package ms.safi.spring.spa.reactive

import ms.safi.spring.spa.SpaIntegrationTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.*

@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.main.web-application-type=reactive"
    ]
)
internal class SpaWebFluxIntegrationTest : SpaIntegrationTest()
