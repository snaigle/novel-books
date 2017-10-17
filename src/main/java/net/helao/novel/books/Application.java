package net.helao.novel.books;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAuthorizeMode;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.SpringBootWebSecurityConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wangcheng<wangcheng@mucang.cn>
 * @date 2017/3/20.
 */
@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(Application.class, args);
    }

    @Configuration
    public static class MyConfig {
        @Bean
        public TaskScheduler taskScheduler() {
            return new ThreadPoolTaskScheduler();
        }
    }

    @Configuration
    protected static class MyWebSecurityAdapter extends WebSecurityConfigurerAdapter {

        private SecurityProperties security;

        protected MyWebSecurityAdapter(SecurityProperties security) {
            this.security = security;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            if (this.security.isRequireSsl()) {
                http.requiresChannel().anyRequest().requiresSecure();
            }
            if (!this.security.isEnableCsrf()) {
                http.csrf().disable();
            }
            // No cookies for application endpoints by default
            http.sessionManagement().sessionCreationPolicy(this.security.getSessions());
            SpringBootWebSecurityConfiguration.configureHeaders(http.headers(),
                    this.security.getHeaders());
            String[] paths = getSecureApplicationPaths();
            if (paths.length > 0) {
                String[] roles = this.security.getUser().getRole().toArray(new String[0]);
                SecurityAuthorizeMode mode = this.security.getBasic().getAuthorizeMode();
                if (mode == null || mode == SecurityAuthorizeMode.ROLE) {
                    http.authorizeRequests().antMatchers(getSecureApplicationPaths()).hasAnyRole(roles);
                } else if (mode == SecurityAuthorizeMode.AUTHENTICATED) {
                    http.authorizeRequests().antMatchers(getSecureApplicationPaths()).authenticated();
                }
            }
            http.authorizeRequests().anyRequest().anonymous();
        }

        private String[] getSecureApplicationPaths() {
            List<String> list = new ArrayList<String>();
            for (String path : this.security.getBasic().getPath()) {
                path = (path == null ? "" : path.trim());
                if (path.equals("/**")) {
                    return new String[]{path};
                }
                if (!path.equals("")) {
                    list.add(path);
                }
            }
            return list.toArray(new String[list.size()]);
        }

    }

}
