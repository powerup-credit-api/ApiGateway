package co.crediyacorp.gateway;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;



public class JwtAuthenticationFilter implements GatewayFilter, Ordered {

    private final boolean requireAuth;
    private final String jwtSecret;

    public JwtAuthenticationFilter(boolean requireAuth, String jwtSecret) {
        this.requireAuth = requireAuth;
        this.jwtSecret = jwtSecret;
    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return validateToken(exchange)
                .flatMap(chain::filter)
                .onErrorResume(JWTVerificationException.class, e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private Mono<ServerWebExchange> validateToken(ServerWebExchange exchange) {
        if (!requireAuth) {
            return Mono.just(exchange);
        }

        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7))
                .flatMap(this::verifyToken)
                .map(decodedJWT -> exchange.mutate()
                        .request(r -> r
                                .header("X-USER-SUBJECT", decodedJWT.getSubject())
                                .header("X-USER-ROLE", decodedJWT.getClaim("role").asString()))
                        .build()
                )
                .switchIfEmpty(Mono.defer(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return Mono.error(new JWTVerificationException("Token missing or invalid"));
                }));
    }

    private Mono<DecodedJWT> verifyToken(String token) {
        return Mono.fromSupplier(() -> {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        });
    }


    @Override
    public int getOrder() {
        return -1;
    }
}
