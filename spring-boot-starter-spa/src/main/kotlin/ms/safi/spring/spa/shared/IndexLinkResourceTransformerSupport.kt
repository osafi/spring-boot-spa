package ms.safi.spring.spa.shared

interface IndexLinkResourceTransformerSupport {
    companion object {
        private val URL_REGEX = """(?:src|href)\s*=\s*(?<quote>["'])(?<url>\S*)\k<quote>""".toRegex()
    }

    data class Chunk(val range: IntRange, val isLink: Boolean) {
        val start: Int
            get() = this.range.first
        val end: Int
            get() = this.range.last

        fun getContent(content: String): String {
            return content.substring(range)
        }
    }


    fun parseIntoChunks(indexContent: String): List<Chunk>? {
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

    fun hasScheme(link: String): Boolean {
        val schemeIndex = link.indexOf(':')
        return (schemeIndex > 0 && !link.substring(0, schemeIndex).contains("/")) || link.indexOf("//") == 0
    }
}