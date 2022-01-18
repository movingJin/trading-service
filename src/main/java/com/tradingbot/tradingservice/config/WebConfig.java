//package com.tradingbot.tradingservice.config;
//
//import io.netty.handler.codec.http.HttpMethod;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.reactive.config.CorsRegistry;
//import org.springframework.web.reactive.config.WebFluxConfigurer;
//
//@Configuration
//public class WebConfig implements WebFluxConfigurer {
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**") //** 하면 전부다 허용
//                .allowedOrigins("*")
//                .allowedMethods(
//                        HttpMethod.POST.name(),
//                        HttpMethod.GET.name(),
//                        HttpMethod.PUT.name(),
//                        HttpMethod.DELETE.name(),
//                        HttpMethod.OPTIONS.name(),
//                        HttpMethod.PATCH.name())
//                .allowCredentials(false)
//                .allowedHeaders("*")
//                .maxAge(3600);
//    }
//}
