package ms.safi.spring.spa

import ms.safi.spring.spa.reactive.SpaWebFluxConfigurer
import ms.safi.spring.spa.servlet.SpaWebMvcConfigurer
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(WebMvcAutoConfiguration::class, WebFluxAutoConfiguration::class)
@ConditionalOnWebApplication
class SpaAutoconfiguration {

    @ConditionalOnClass(WebMvcConfigurer::class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @Configuration
    class MvcConfig {
        @Bean
        fun spaWebMvcConfigurer(webProperties: WebProperties) = SpaWebMvcConfigurer(webProperties)
    }

    @ConditionalOnClass(WebFluxConfigurer::class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @Configuration
    class ReactiveConfig {
        @Bean
        fun spaWebFluxConfigurer(webProperties: WebProperties) = SpaWebFluxConfigurer(webProperties)
    }
}