package top.endiexz.DiskOnline.Configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import top.endiexz.DiskOnline.Interceptor.JwtInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    private CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();

            // 允许任何来源
            config.addAllowedOriginPattern("*");  // 使用 addAllowedOriginPattern() 允许所有来源

            config.setAllowCredentials(true); // 允许发送 cookie 或自定义 token
            config.addAllowedHeader("*");
            config.addAllowedMethod("*");
            config.setExposedHeaders(List.of("token")); // 可以暴露给前端的头

            return config;
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(                
    "/login",
                "/signin",
                "/publickey",
                "/verifynode",
                "/joincentral");
    }
}
