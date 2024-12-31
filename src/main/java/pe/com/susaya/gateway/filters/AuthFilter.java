package pe.com.susaya.gateway.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Optional;


@Component
public class AuthFilter implements GatewayFilter {

    private final WebClient webClient;
    private final Long EXPIRATION__IN_MINUTES;
    private final String SECRET_KEY;

    private static final String AUTH_VALIDATE_URI = "http://localhost:4040/ms-security/v1/security/auth/validate-permission";

    public AuthFilter(@Value("${security.jwt.expiration-in-minutes}") Long EXPIRATION__IN_MINUTES, @Value("${security.jwt.secret-key}") String SECRET_KEY){
        this.webClient = WebClient.builder().build();
        this.EXPIRATION__IN_MINUTES = EXPIRATION__IN_MINUTES;
        this.SECRET_KEY = SECRET_KEY;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }
        PathContainer pathContainer = exchange.getRequest().getPath();
        String tokenHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Obtener el resto del path a partir del tercer segmento
        String remainingPath = pathContainer.subPath(2).value();
        String httpMethod = exchange.getRequest().getMethod().name();

        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.BAD_REQUEST);
        }
        String jwt = tokenHeader.split(" ")[1];
        String name = Optional.ofNullable(extractUserName(jwt)).orElse("defaultName");


        String requestUri = String.format("%s?url=%s&httpMethod=%s&name=%s", AUTH_VALIDATE_URI, remainingPath, httpMethod, name);



        return this.webClient
                .get()
                .uri(requestUri)
                .retrieve()
                .bodyToMono(Boolean.class)
                .flatMap(isValid -> {
                    if (Boolean.TRUE.equals(isValid)) {
                        return chain.filter(exchange);
                    } else {
                        return onError(exchange, HttpStatus.UNAUTHORIZED);
                    }
                });
    }

    public String extractUserName(String jwt) {
        return extractAllClaims(jwt).getSubject();
    }
    private Claims extractAllClaims(String jwt) {
        return Jwts.parser().verifyWith(generateKey()).build()
                .parseSignedClaims(jwt).getPayload();
    }

    private SecretKey generateKey() {
        byte[] passwordDecoded = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(passwordDecoded);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status){
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
