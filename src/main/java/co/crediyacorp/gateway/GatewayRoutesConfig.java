package co.crediyacorp.gateway;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${services.auth.uri}")
    private String authUri;

    @Value("${services.auth.public-paths}")
    private String[] authPublicPaths;

    @Value("${services.auth.private-paths}")
    private String[] authPrivatePaths;

    @Value("${services.solicitudes.uri}")
    private String solicitudesUri;

    @Value("${services.solicitudes.paths}")
    private String[] solicitudesPaths;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-public-service", r -> r.path(authPublicPaths)
                        .filters(f -> f.filter(new JwtAuthenticationFilter(false, jwtSecret)))
                        .uri(authUri))

                .route("auth-private-service", r -> r.path(authPrivatePaths)
                        .filters(f -> f.filter(new JwtAuthenticationFilter(true, jwtSecret)))
                        .uri(authUri))

                .route("solicitudes-service", r -> r.path(solicitudesPaths)
                        .filters(f -> f.filter(new JwtAuthenticationFilter(true, jwtSecret)))
                        .uri(solicitudesUri))

                .build();
    }
}


