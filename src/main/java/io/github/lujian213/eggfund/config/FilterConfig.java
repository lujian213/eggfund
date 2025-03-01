package io.github.lujian213.eggfund.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<HostNameFilter> hostnameFilterRegistrationBean(HostNameFilter hostnameFilter) {
        FilterRegistrationBean<HostNameFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(hostnameFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}