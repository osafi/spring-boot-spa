package ms.safi.spring.spa.servlet

import ms.safi.spring.spa.shared.IndexLinkResourceTransformerSupport
import org.springframework.core.io.Resource
import org.springframework.util.FileCopyUtils
import org.springframework.web.servlet.resource.ResourceTransformerChain
import org.springframework.web.servlet.resource.ResourceTransformerSupport
import org.springframework.web.servlet.resource.TransformedResource
import java.nio.charset.StandardCharsets.UTF_8
import javax.servlet.http.HttpServletRequest

class IndexLinkResourceTransformer : ResourceTransformerSupport(), IndexLinkResourceTransformerSupport {
    override fun transform(
        request: HttpServletRequest,
        originalResource: Resource,
        transformerChain: ResourceTransformerChain
    ): Resource {
        val resource = transformerChain.transform(request, originalResource)

        if ("index.html" != resource.filename) {
            return resource
        }

        val indexContent = String(FileCopyUtils.copyToByteArray(resource.inputStream), UTF_8)

        val chunks = parseIntoChunks(indexContent) ?: return resource

        val transformed = chunks.map { chunk ->
            val contentChunk = chunk.getContent(indexContent)
            return@map if (chunk.isLink && !hasScheme(contentChunk)) {
                val link = toAbsolutePath(contentChunk, request)
                resolveUrlPath(link, request, resource, transformerChain) ?: contentChunk
            } else {
                contentChunk
            }
        }.joinToString("")

        return TransformedResource(originalResource, transformed.toByteArray(UTF_8))
    }
}
