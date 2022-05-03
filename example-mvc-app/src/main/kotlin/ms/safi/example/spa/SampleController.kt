package ms.safi.example.spa

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController {
    @GetMapping("/api/testing")
    fun testing(): Response {
        return Response(thing = "hello")
    }

    @GetMapping("/api/notfound")
    fun notFound(): ResponseEntity<Void> {
        return ResponseEntity.notFound().build()
    }

    data class Response(val thing: String)
}
