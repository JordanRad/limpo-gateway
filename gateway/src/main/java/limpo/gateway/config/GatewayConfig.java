package limpo.gateway.config;

import limpo.gateway.filter.AuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    AuthFilter authFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth", r -> r.path("/api/v1/auth-service/**").filters(f->f.filter(authFilter)).uri("lb://AUTH-SERVICE"))
                .route("orders", r -> r.path("/api/v1/order-service/**").filters(f->f.filter(authFilter)).uri("lb://ORDER-SERVICE"))
                .route("consumables", r -> r.path("/api/v1/consumable-service/**").filters(f->f.filter(authFilter)).uri("lb://CONSUMABLE-SERVICE"))
                .build();
    }
}
