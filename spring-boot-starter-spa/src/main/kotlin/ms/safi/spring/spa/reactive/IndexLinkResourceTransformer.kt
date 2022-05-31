package ms.safi.spring.spa.reactive

import ms.safi.spring.spa.shared.IndexLinkResourceTransformerSupport
import org.springframework.core.io.Resource
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.util.StreamUtils
import org.springframework.web.reactive.resource.ResourceTransformerChain
import org.springframework.web.reactive.resource.ResourceTransformerSupport
import org.springframework.web.reactive.resource.TransformedResource
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.StringWriter
import java.nio.charset.StandardCharsets.UTF_8

class IndexLinkResourceTransformer : ResourceTransformerSupport(), IndexLinkResourceTransformerSupport {

    override fun transform(
        exchange: ServerWebExchange,
        originalResource: Resource,
        transformerChain: ResourceTransformerChain
    ): Mono<Resource> {
        return transformerChain.transform(exchange, originalResource).flatMap { resource ->
            if ("index.html" != resource.filename) {
                return@flatMap Mono.just(resource)
            }

            val bufferFactory = exchange.response.bufferFactory()
            val dataBuffers = DataBufferUtils.read(resource, bufferFactory, StreamUtils.BUFFER_SIZE)

            DataBufferUtils.join(dataBuffers)
                .flatMap { dataBuffer ->
                    val charBuffer = UTF_8.decode(dataBuffer.asByteBuffer())
                    DataBufferUtils.release(dataBuffer)
                    val indexContent = charBuffer.toString()
                    transformContent(indexContent, resource, transformerChain, exchange)
                }
        }
    }

    private fun transformContent(
        indexContent: String,
        resource: Resource,
        chain: ResourceTransformerChain,
        exchange: ServerWebExchange
    ): Mono<Resource> {
        val chunks = parseIntoChunks(indexContent) ?: return Mono.just(resource)

        return Flux.fromIterable(chunks)
            .concatMap { chunk ->
                val contentChunk = chunk.getContent(indexContent)
                if (chunk.isLink && !hasScheme(contentChunk)) {
                    val link = toAbsolutePath(contentChunk, exchange)
                    resolveUrlPath(link, exchange, resource, chain).defaultIfEmpty(contentChunk)
                } else {
                    Mono.just(contentChunk)
                }
            }
            .reduce(StringWriter()) { writer, chunkContent -> writer.write(chunkContent); writer }
            .map { TransformedResource(resource, it.toString().toByteArray(UTF_8)) }
    }
}
