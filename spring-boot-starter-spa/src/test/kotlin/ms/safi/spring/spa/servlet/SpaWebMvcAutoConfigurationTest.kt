package ms.safi.spring.spa.servlet

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import org.springframework.context.ApplicationContext
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
import org.springframework.web.servlet.resource.*
import java.util.concurrent.TimeUnit


internal class SpaWebMvcAutoConfigurationTest {

    private val contextRunner = WebApplicationContextRunner().withConfiguration(
        AutoConfigurations.of(
            SpaWebMvcAutoConfiguration::class.java,
            WebMvcAutoConfiguration::class.java,
            DispatcherServletAutoConfiguration::class.java,
            HttpMessageConvertersAutoConfiguration::class.java,
            PropertyPlaceholderAutoConfiguration::class.java,
        )
    )

    @Test
    fun `overrides the ALL resource handler mapping to use IndexFallbackPathResourceResolver`() {
        this.contextRunner.run {
            assertThat(getMappingResourceResolvers(it, "/**")).hasSize(2)
            assertThat(getMappingResourceResolvers(it, "/**")).extractingResultOf("getClass").containsExactly(
                CachingResourceResolver::class.java,
                IndexFallbackPathResourceResolver::class.java,
            )
        }
    }

    @Test
    fun `with resource chain caching disabled`() {
        this.contextRunner
            .withPropertyValues("spring.web.resources.chain.cache=false")
            .run {
                assertThat(getMappingResourceResolvers(it, "/**")).hasSize(1)
                assertThat(getMappingResourceResolvers(it, "/**")).extractingResultOf("getClass").containsExactly(
                    IndexFallbackPathResourceResolver::class.java,
                )
            }
    }

    @Test
    fun `with custom static path pattern for the resource handler`() {
        this.contextRunner
            .withPropertyValues("spring.mvc.static-path-pattern=/static/**")
            .run {
                assertThat(getResourceHandlerMapping(it).handlerMap).hasSize(2)
                assertThat(getMappingLocations(it, "/static/**")).hasSize(4) // should be 5 - need to add Servlet context
            }
    }

    @Test
    fun `with custom resource locations`() {
        this.contextRunner
            .withPropertyValues("spring.web.resources.static-locations=classpath:/foo/,classpath:/bar/")
            .run {
                assertThat(getMappingLocations(it, "/**")).containsExactly(
                    ClassPathResource("/foo/"),
                    ClassPathResource("/bar/"),
                )
            }
    }

    @Test
    fun `with custom cache period`() {
        this.contextRunner
            .withPropertyValues("spring.web.resources.cache.period=5")
            .run {
                assertThat(getHandlerMapping(it, "/**").cacheSeconds).isEqualTo(5)
                assertThat(getHandlerMapping(it, "/**").cacheControl).isNull()
            }
    }

    @Test
    fun `with custom cache control`() {
        this.contextRunner
            .withPropertyValues(
                "spring.web.resources.cache.cachecontrol.max-age=5",
                "spring.web.resources.cache.cachecontrol.proxy-revalidate=true",
            )
            .run {
                assertThat(getHandlerMapping(it, "/**").cacheSeconds).isEqualTo(-1)
                assertThat(getHandlerMapping(it, "/**").cacheControl)
                    .usingRecursiveComparison()
                    .isEqualTo(CacheControl.maxAge(5, TimeUnit.SECONDS).proxyRevalidate())
            }
    }

    @Test
    fun `with use last-modified disabled`() {
        this.contextRunner
            .withPropertyValues("spring.web.resources.cache.use-last-modified=false")
            .run {
                assertThat(getHandlerMapping(it, "/**").isUseLastModified).isFalse
            }
    }

    @Test
    fun `with fixed strategy enabled`() {
        this.contextRunner
            .withPropertyValues(
                "spring.web.resources.chain.strategy.fixed.enabled=true",
                "spring.web.resources.chain.strategy.fixed.version=test",
                "spring.web.resources.chain.strategy.fixed.paths=/**/*.js",
            )
            .run {
                val allResourceResolver = getMappingResourceResolvers(it, "/**")
                assertThat(allResourceResolver).hasSize(3)
                assertThat(allResourceResolver).extractingResultOf("getClass").containsExactly(
                    CachingResourceResolver::class.java,
                    VersionResourceResolver::class.java,
                    IndexFallbackPathResourceResolver::class.java,
                )

                assertThat(getMappingResourceTransformers(it, "/**")).extractingResultOf("getClass").containsExactly(
                    CachingResourceTransformer::class.java,
                    CssLinkResourceTransformer::class.java,
                    IndexLinkResourceTransformer::class.java,
                )

                val versionResourceResolver = allResourceResolver[1] as VersionResourceResolver
                assertThat(versionResourceResolver.strategyMap).hasSize(2)
                assertThat(versionResourceResolver.strategyMap["/**/*.js"]).isInstanceOf(FixedVersionStrategy::class.java)
            }
    }

    @Test
    fun `with content strategy enabled`() {
        this.contextRunner
            .withPropertyValues(
                "spring.web.resources.chain.strategy.content.enabled:true",
                "spring.web.resources.chain.strategy.content.paths:/**,/*.png",
            )
            .run {
                val allResourceResolver = getMappingResourceResolvers(it, "/**")
                assertThat(allResourceResolver).hasSize(3)
                assertThat(allResourceResolver).extractingResultOf("getClass").containsExactly(
                    CachingResourceResolver::class.java,
                    VersionResourceResolver::class.java,
                    IndexFallbackPathResourceResolver::class.java,
                )

                assertThat(getMappingResourceTransformers(it, "/**")).extractingResultOf("getClass").containsExactly(
                    CachingResourceTransformer::class.java,
                    CssLinkResourceTransformer::class.java,
                    IndexLinkResourceTransformer::class.java,
                )

                val versionResourceResolver = allResourceResolver[1] as VersionResourceResolver
                assertThat(versionResourceResolver.strategyMap).hasSize(2)
                assertThat(versionResourceResolver.strategyMap["/*.png"]).isInstanceOf(ContentVersionStrategy::class.java)
                assertThat(versionResourceResolver.strategyMap["/**"]).isInstanceOf(ContentVersionStrategy::class.java)
            }
    }

    @Test
    fun `with customized resource chain`() {
        this.contextRunner
            .withPropertyValues(
                "spring.web.resources.chain.cache=false",
                "spring.web.resources.chain.strategy.content.enabled=true",
                "spring.web.resources.chain.strategy.content.paths=/**,/*.png",
                "spring.web.resources.chain.strategy.fixed.enabled=true",
                "spring.web.resources.chain.strategy.fixed.version=test",
                "spring.web.resources.chain.strategy.fixed.paths=/**/*.js",
                "spring.web.resources.chain.compressed=true",
            )
            .run {
                val allResourceResolver = getMappingResourceResolvers(it, "/**")
                assertThat(allResourceResolver).hasSize(3)
                assertThat(allResourceResolver).extractingResultOf("getClass").containsExactly(
                    EncodedResourceResolver::class.java,
                    VersionResourceResolver::class.java,
                    IndexFallbackPathResourceResolver::class.java,
                )

                assertThat(getMappingResourceTransformers(it, "/**")).extractingResultOf("getClass").containsExactly(
                    CssLinkResourceTransformer::class.java,
                    IndexLinkResourceTransformer::class.java,
                )

                val versionResourceResolver = allResourceResolver[1] as VersionResourceResolver
                assertThat(versionResourceResolver.strategyMap).hasSize(4)
                assertThat(versionResourceResolver.strategyMap["/*.png"]).isInstanceOf(ContentVersionStrategy::class.java)
                assertThat(versionResourceResolver.strategyMap["/**"]).isInstanceOf(ContentVersionStrategy::class.java)
                assertThat(versionResourceResolver.strategyMap["/**/*.js"]).isInstanceOf(FixedVersionStrategy::class.java)
                assertThat(versionResourceResolver.strategyMap["/test/**/*.js"]).isInstanceOf(FixedVersionStrategy::class.java)
            }
    }

    private fun getResourceHandlerMapping(context: ApplicationContext): SimpleUrlHandlerMapping {
        return context.getBean("resourceHandlerMapping", SimpleUrlHandlerMapping::class.java)
    }

    private fun getHandlerMapping(context: ApplicationContext, mapping: String): ResourceHttpRequestHandler {
        return getResourceHandlerMapping(context).handlerMap[mapping] as ResourceHttpRequestHandler
    }

    private fun getMappingResourceResolvers(context: ApplicationContext, mapping: String): List<ResourceResolver> {
        return getHandlerMapping(context, mapping).resourceResolvers
    }

    private fun getMappingResourceTransformers(context: ApplicationContext, mapping: String): List<ResourceTransformer> {
        return getHandlerMapping(context, mapping).resourceTransformers
    }

    private fun getMappingLocations(context: ApplicationContext, mapping: String): List<Resource> {
        return getHandlerMapping(context, mapping).locations
    }
}