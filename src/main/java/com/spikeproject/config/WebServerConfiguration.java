package com.spikeproject.config;

import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * <h1>定制化webServer</h1>
 * @author samy
 * @date 2020/1/8 16:57
 */
@Configuration
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        ((TomcatServletWebServerFactory)factory)
                .addConnectorCustomizers((TomcatConnectorCustomizer) connector -> {
            Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();

            //设置30秒内没有请求则服务端自动断开keepalive连接
            protocol.setKeepAliveTimeout(30000);
            //当客户端发送超过10000个请求则自动断开keepalive连接
            protocol.setMaxKeepAliveRequests(10000);
                });
    }
}
