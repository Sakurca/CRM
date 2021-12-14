package com.yjxxt.crm.config;

import com.yjxxt.crm.interceptors.NoLoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Bean
    public NoLoginInterceptor noLoginInterceptor(){
        return new NoLoginInterceptor();
    }

    public void addInterceptors(InterceptorRegistry registry){
        //配置拦截器
        //拦截路劲
        registry.addInterceptor(noLoginInterceptor())
                //添加拦截路劲拦截
                .addPathPatterns("/***")
                .excludePathPatterns("/index","/user/login","/js/**","/css/**","/images/**","/lib/**");
    }



}
