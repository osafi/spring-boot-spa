package ms.safi.spring.spa.servlet

import org.springframework.core.io.Resource
import org.springframework.util.FileCopyUtils
import org.springframework.web.servlet.resource.ResourceTransformerChain
import org.springframework.web.servlet.resource.ResourceTransformerSupport
import org.springframework.web.servlet.resource.TransformedResource
import java.nio.charset.StandardCharsets.UTF_8
import javax.servlet.http.HttpServletRequest

class IndexLinkResourceTransformer : ResourceTransformerSupport() {
    companion object {
        private val URL_PATTERN = """(?:src|href)\s*=\s*(?<quote>["'])(?<url>\S*)\k<quote>""".toRegex()
    }

    override fun transform(
        request: HttpServletRequest,
        originalResource: Resource,
        transformerChain: ResourceTransformerChain
    ): Resource {
        val resource = transformerChain.transform(request, originalResource)

        if ("index.html" != resource.filename) {
            return resource
        }

        val content = String(FileCopyUtils.copyToByteArray(resource.inputStream), UTF_8)

        val transformed = content.replace(URL_PATTERN) { match ->
            val matchValue = match.value
            val matchRange = match.range
            val (url, urlRange) = match.groups["url"]!!

            if (hasScheme(url)) {
                return@replace matchValue
            }

            val absolutePath = toAbsolutePath(url, request)
            val newLink = resolveUrlPath(absolutePath, request, resource, transformerChain)

            newLink?.let { matchValue.replaceRange(urlRange.shiftLeft(matchRange.first), newLink) }
                ?: matchValue
        }

        return TransformedResource(originalResource, transformed.toByteArray(UTF_8))
    }

    private fun hasScheme(link: String): Boolean {
        val schemeIndex = link.indexOf(':')
        return (schemeIndex > 0 && !link.substring(0, schemeIndex).contains("/")) || link.indexOf("//") == 0
    }

    private fun IntRange.shiftLeft(amount: Int) = IntRange(this.first - amount, this.last - amount)
}
