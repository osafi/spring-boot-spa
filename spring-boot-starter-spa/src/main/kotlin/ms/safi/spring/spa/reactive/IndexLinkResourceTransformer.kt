package ms.safi.spring.spa.reactive

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


class IndexLinkResourceTransformer : ResourceTransformerSupport() {
    companion object {
        private val URL_REGEX = """(?:src|href)\s*=\s*(?<quote>["'])(?<url>\S*)\k<quote>""".toRegex()

        private data class Chunk(val range: IntRange, val isLink: Boolean) {
            val start: Int
                get() = this.range.first
            val end: Int
                get() = this.range.last

            fun getContent(content: String): String {
                return content.substring(range)
            }
        }
    }

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

    private fun parseIntoChunks(indexContent: String): List<Chunk>? {
        val linkChunks = URL_REGEX.findAll(indexContent)
            .map { Chunk(range = it.groups["url"]!!.range, isLink = true) }
            .toList()

        if (linkChunks.isEmpty()) {
            return null
        }

        val chunks: MutableList<Chunk> = mutableListOf()
        var position = 0

        for (link in linkChunks) {
            chunks.add(Chunk(range = IntRange(position, link.start - 1), isLink = false))
            chunks.add(link)
            position = link.end + 1
        }
        if (position < indexContent.length) {
            chunks.add(Chunk(range = IntRange(position, indexContent.length - 1), isLink = false))
        }

        return chunks
    }

    private fun hasScheme(link: String): Boolean {
        val schemeIndex = link.indexOf(':')
        return (schemeIndex > 0 && !link.substring(0, schemeIndex).contains("/")) || link.indexOf("//") == 0
    }
}
