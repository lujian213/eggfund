package io.github.lujian213.eggfund.config;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Stream;

@Component
public class HostNameFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(HostNameFilter.class);

    @Value("${whitelist.hosts}")
    List<String> whitelistHosts;

    private List<String> whitelistIps;
    private boolean permitAll = false;
    private static final List<String> localhosts = List.of("127.0.0.1", "0:0:0:0:0:0:0:1");

    @Override
    public void init(FilterConfig filterConfig) {
        if (whitelistHosts.contains("*")) {
            permitAll = true;
            log.info("Permit all hosts");
            return;
        }
        whitelistIps = Stream.concat(
                localhosts.stream(),
                whitelistHosts.stream().map(String::trim).map(this::getIpByHostName)
        ).toList();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String remoteAddr = httpRequest.getRemoteAddr();
        if (isPermitted(httpRequest.getRequestURI(), remoteAddr)) {
            chain.doFilter(request, response);
        } else {
            log.warn("The host {} not in whitelist", remoteAddr);
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden Host");
        }
    }

    boolean isPermitted(String uri, String remoteHost) {
        if (uri.equals("/actuator/shutdown")) {
            if (localhosts.contains(remoteHost)) {
                log.info("Shutdown from localhost");
                return true;
            } else {
                log.warn("Shutdown from {} is not permitted", remoteHost);
                return false;
            }
        }
        return permitAll || whitelistIps.contains(remoteHost);
    }

    private String getIpByHostName(String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Unable to get the IP address for hostname: {}", hostName, e);
            return hostName;
        }
    }

    @Override
    public void destroy() {
        //nothing to destroy
    }
}

