package cuong.com.apigateway;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.time.LocalDateTime;

@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String time = LocalDateTime.now().toString();
        String method = exchange.getRequest().getMethod().name();
        String uri = exchange.getRequest().getURI().toString();

        System.out.println("[GATEWAY LOG] Time: " + time + " | Method: " + method + " | URI: " + uri);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
