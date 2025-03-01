package io.github.lujian213.eggfund.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
public class WebProxyConfig {
    @Value("${webproxy.type}")
    private String proxyType;
    @Value("${webproxy.host}")
    private String proxyHost;
    @Value("${webproxy.port}")
    private int proxyPort;

    @Bean
    public Proxy createProxy() {
        if (proxyType == null || proxyType.isEmpty()) {
            return Proxy.NO_PROXY;
        }
        return new Proxy(Proxy.Type.valueOf(proxyType), new InetSocketAddress(proxyHost, proxyPort));
    }
}