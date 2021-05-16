package cn.com.kunghsu.desktop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.annotation.PostConstruct;

@Configuration
public class AppConfiguration extends WebMvcConfigurerAdapter {

    @Value("${desktop.websocket.enter.skipPassword:false}")
    private boolean isSkipPassword;

    @Value("${desktop.websocket.enter.skip-password:false}")
    private boolean isSkipPassword2;

    @PostConstruct
    public void init(){
        System.out.println();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CommonInterceptor()).addPathPatterns("/**");
        super.addInterceptors(registry);
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
