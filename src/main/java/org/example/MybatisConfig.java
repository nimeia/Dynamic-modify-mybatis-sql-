package org.example;

import org.apache.ibatis.plugin.Interceptor;
import org.example.auto.AppendAppIdWhenInsertInterceptor;
import org.example.auto.AppendAppIdWhenSelectInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {

    @Bean
    Interceptor interceptor(){
        return new AppendAppIdWhenInsertInterceptor();
    }
    @Bean
    Interceptor appendAppIdWhenSelectInterceptor(){
        return new AppendAppIdWhenSelectInterceptor();
    }

    @Bean
    Interceptor interceptor1(){
        return new StatementHandlerPlugin();
    }


    @Bean
    Interceptor interceptor2(){
        return new ParameterHandlerPlugin();
    }
}
