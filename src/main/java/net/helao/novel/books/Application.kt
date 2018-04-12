package net.helao.novel.books

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author snail
 * @date 2017/3/20.
 */
@SpringBootApplication
class Application {

    @Bean
    fun userDetailsService(): UserDetailsService {
        val manager: InMemoryUserDetailsManager = InMemoryUserDetailsManager()
        manager.createUser(User.withUsername("admin")
                .password("123456")
                .roles("USER")
                .passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder()::encode)
                .build())
        return manager
    }

    @Bean
    fun bean3(): Bean33 {
        return Bean33()
    }

    @Bean
    @ConditionalOnMissingBean(Bean33::class)
    fun bean1(): Bean33 {
        return Bean33()
    }

    @Bean
    @ConditionalOnBean(Bean33::class)
    fun bean2(): Bean44 {
        return Bean44()
    }

    class Bean33
    class Bean44
}

@RestController
class MyController(val ctx: ApplicationContext) {

    @GetMapping("/abc")
    fun abc() {
        println(ctx.getBeansOfType(Application.Bean33::class.java))
        println(ctx.getBeansOfType(Application.Bean44::class.java))
    }
}

@Configuration
class MySecurityAdapter : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http!!.authorizeRequests()
                .antMatchers("/admin/**").authenticated()
                .anyRequest().permitAll()
                .and().formLogin().loginPage("/login")
                .loginProcessingUrl("/login")
                .and().cors()
                .and().oauth2Login()
    }

}

fun main(args: Array<String>) {
    runApplication<Application>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
}

