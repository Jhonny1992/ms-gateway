package pe.com.susaya.gateway.beans;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.CrossOrigin;
import pe.com.susaya.gateway.filters.AuthFilter;

@CrossOrigin( origins = "*")
@Configuration
public class GatewayConfig {

    private final AuthFilter authFilter;

    public GatewayConfig(AuthFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    @Profile(value = "eureka-off")
    public RouteLocator routeLocatorEurekaOff(RouteLocatorBuilder locatorBuilder){
        return locatorBuilder
                .routes()
                .route( route ->route
                        .path("/ms-company/v1/company/**")
                        .uri("http://localhost:8082")
                )
                .build();
    }

    @Bean
    @Profile(value = "eureka-on")
    public RouteLocator routeLocatorEurekaOn(RouteLocatorBuilder locatorBuilder){
        return locatorBuilder
                .routes()
                .route( route ->route
                        .path("/ms-company/v1/company/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://ms-company")
                )

                .route( route ->route
                        .path("/ms-security/v1/security/auth/**")
                        .uri("lb://ms-security")
                )
                .build();
    }



}
