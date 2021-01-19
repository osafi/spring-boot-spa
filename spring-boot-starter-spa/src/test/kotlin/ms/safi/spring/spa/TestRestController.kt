package ms.safi.spring.spa

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
internal class TestRestController {
    @GetMapping("/test")
    fun testGet() = object {
        val foo = "bar"
    }
}
