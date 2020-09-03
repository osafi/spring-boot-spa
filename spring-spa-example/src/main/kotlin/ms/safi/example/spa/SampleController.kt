package ms.safi.example.spa

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @GetMapping("/api/testing")
    fun testing(): Response {
        println("in SampleController")
        return Response(thing = "hello")
    }

    data class Response(val thing: String)
}
