package ms.safi.spring.spa

import ms.safi.spring.spa.reactive.SpaWebFluxAutoConfiguration
import ms.safi.spring.spa.servlet.SpaWebMvcAutoConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.test.context.FilteredClassLoader
import org.springframework.boot.test.context.assertj.ApplicationContextAssertProvider
import org.springframework.boot.test.context.runner.AbstractApplicationContextRunner
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

internal class SpaAutoConfigurationTest {

    @Test
    fun `does not setup SPA configurations when not a web application`() {
        ApplicationContextRunner()
            .withSpaAutoconfigurations()
            .run {
                assertThat(it).doesNotHaveBean(SpaWebMvcAutoConfiguration::class.java)
                assertThat(it).doesNotHaveBean(SpaWebFluxAutoConfiguration::class.java)
            }
    }

    @Test
    fun `configures SpaWebMvcConfigurer when servlet web application`() {
        WebApplicationContextRunner()
            .withSpaAutoconfigurations()
            .run {
                assertThat(it).hasSingleBean(SpaWebMvcAutoConfiguration::class.java)
                assertThat(it).doesNotHaveBean(SpaWebFluxAutoConfiguration::class.java)
            }
    }

    @Test
    fun `SpaWebMvcConfigurer is conditional on having WebMvcConfigurer on the classpath`() {
        WebApplicationContextRunner()
            .withSpaAutoconfigurations()
            .withClassLoader(FilteredClassLoader(WebMvcConfigurer::class.java))
            .run {
                assertThat(it).doesNotHaveBean(SpaWebMvcAutoConfiguration::class.java)
            }
    }

    @Test
    fun `configures SpaWebFluxConfigurer when reactive web application`() {
        ReactiveWebApplicationContextRunner()
            .withSpaAutoconfigurations()
            .run {
                assertThat(it).hasSingleBean(SpaWebFluxAutoConfiguration::class.java)
                assertThat(it).doesNotHaveBean(SpaWebMvcAutoConfiguration::class.java)
            }
    }

    @Test
    fun `SpaWebFluxConfigurer is conditional on having WebFluxConfigurer on the classpath`() {
        ReactiveWebApplicationContextRunner()
            .withSpaAutoconfigurations()
            .withClassLoader(FilteredClassLoader(WebFluxConfigurer::class.java))
            .run {
                assertThat(it).doesNotHaveBean(SpaWebFluxAutoConfiguration::class.java)
            }
    }

}

private fun <SELF : AbstractApplicationContextRunner<SELF, C, A>, C : ConfigurableApplicationContext, A : ApplicationContextAssertProvider<C>> AbstractApplicationContextRunner<SELF, C, A>.withSpaAutoconfigurations(): AbstractApplicationContextRunner<SELF, C, A> {
    return this
        .withConfiguration(AutoConfigurations.of(SpaWebFluxAutoConfiguration::class.java, SpaWebMvcAutoConfiguration::class.java))
        .withBean(WebProperties::class.java)
}
