package net.helao.novel.books

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.SecurityAuthorizeMode
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.autoconfigure.security.SpringBootWebSecurityConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import java.io.IOException
import java.util.*

/**
 * @author snail
 * @date 2017/3/20.
 */
@SpringBootApplication
@EnableScheduling
open class Application {

    @Configuration
    open class MyConfig {
        @Bean
        open fun taskScheduler(): TaskScheduler {
            return ThreadPoolTaskScheduler()
        }
    }

    @Configuration
    open protected class MyWebSecurityAdapter protected constructor(private val security: SecurityProperties) : WebSecurityConfigurerAdapter() {

        private val secureApplicationPaths: Array<String>
            get() {
                val list = ArrayList<String>()
                for (path1 in this.security.basic.path) {
                    val path = path1?.trim { it <= ' ' } ?: ""
                    if (path == "/**") {
                        return arrayOf(path)
                    }
                    if (path != "") {
                        list.add(path)
                    }
                }
                return list.toTypedArray()
            }

        @Throws(Exception::class)
        override fun configure(http: HttpSecurity) {
            if (this.security.isRequireSsl) {
                http.requiresChannel().anyRequest().requiresSecure()
            }
            if (!this.security.isEnableCsrf) {
                http.csrf().disable()
            }
            http.formLogin()
            // No cookies for application endpoints by default
            http.sessionManagement().sessionCreationPolicy(this.security.sessions)
            SpringBootWebSecurityConfiguration.configureHeaders(http.headers(),
                    this.security.headers)
            val paths = secureApplicationPaths
            if (paths.size > 0) {
                val roles = this.security.user.role.toTypedArray()
                val mode = this.security.basic.authorizeMode
                if (mode == null || mode == SecurityAuthorizeMode.ROLE) {
                    http.authorizeRequests().antMatchers(*secureApplicationPaths).hasAnyRole(*roles)
                } else if (mode == SecurityAuthorizeMode.AUTHENTICATED) {
                    http.authorizeRequests().antMatchers(*secureApplicationPaths).authenticated()
                }
            }
            http.authorizeRequests().anyRequest().anonymous()
        }

    }
}

@Throws(IOException::class)
fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
