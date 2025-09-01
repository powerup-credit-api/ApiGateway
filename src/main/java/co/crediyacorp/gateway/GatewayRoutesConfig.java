package co.crediyacorp.gateway;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route("auth-public-service", r -> r.path("/api/v1/login")
                        .filters(f -> f.filter(new JwtAuthenticationFilter(false, jwtSecret)))
                        .uri("http://localhost:8082"))

                .route("auth-private-service", r -> r.path("/api/v1/usuarios", "/api/v1/validar")
                        .filters(f -> f.filter(new JwtAuthenticationFilter(true, jwtSecret)))
                        .uri("http://localhost:8082"))

                .route("solicitudes-service", r -> r.path("/api/v1/solicitud")
                        .filters(f -> f.filter(new JwtAuthenticationFilter(true, jwtSecret)))
                        .uri("http://localhost:8081"))

                .build();
    }
}
