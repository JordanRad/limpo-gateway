package limpo.gateway.filter;

import io.jsonwebtoken.Claims;
import limpo.gateway.dto.ProtectedRoute;
import limpo.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
public class AuthFilter implements GatewayFilter {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * This method constructs list of protected routes
     *
     * @return protectedRoutes List<ProtectedRoute>
     */
    private List<ProtectedRoute> getProtectedRoutes() {
        List<ProtectedRoute> protectedRouteList = new ArrayList<>();
        protectedRouteList.add(new ProtectedRoute("/order-service/clients", HttpMethod.GET, new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/clients", HttpMethod.DELETE, new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/orders", HttpMethod.GET, new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/orders", HttpMethod.PUT, new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/orders", HttpMethod.DELETE, new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/limpoUnits", HttpMethod.POST, new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/limpoUnits", HttpMethod.PUT, new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/limpoUnits", HttpMethod.DELETE, new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/consumables", HttpMethod.GET, new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/consumables", HttpMethod.POST, new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/consumables", HttpMethod.PUT, new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/consumables", HttpMethod.DELETE, new String[]{"ROLE_ADMIN"}));
        return protectedRouteList;
    }

    /**
     * This method returns protected route
     * @param url request url
     * @param method request method
     * @return Protected route or null if the route is unprotected
     */
    private ProtectedRoute getProtectedRoute(String url, HttpMethod method) {
        for (ProtectedRoute route : getProtectedRoutes()) {
            if (url.contains(route.getUrlMatcher()) && route.getMethod().equals(method)) {
                return route;
            }
        }
        return null;
    }


    /**
     * Check if the token is valid
     * @param token Bearer token from HTTP Header
     * @return true if the JWT is valid
     */
    private boolean isJwtValid(String token) {
        try {
            String email = jwtUtil.extractEmail(token);
            if (jwtUtil.validateToken(token, email)) {
                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

    /**
     * Process the Web request and (optionally) delegate to the next {@code WebFilter}
     * through the given {@link GatewayFilterChain}.
     *
     * @param exchange the current server exchange
     * @param chain    provides a way to delegate to the next filter
     * @return {@code Mono<Void>} to indicate when request processing is complete
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String requestUrl = request.getURI().toString();
        HttpMethod requestMethod = request.getMethod();

        System.out.printf("\n[%s]: %s", requestMethod, requestUrl);

        ProtectedRoute routeToCheck = getProtectedRoute(requestUrl, requestMethod);

        // Check if the route is filtered as protected
        if (routeToCheck != null) {
            HttpHeaders headers = request.getHeaders();
            List<String> authorization = headers.get("Authorization");

            // Check if the HTTP Request includes the Authorization header
            if (authorization == null) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }


            String token = authorization.get(0).substring(7);
            String userRole = jwtUtil.extractAllClaims(token).get("role").toString();

            boolean hasRequiredRole = Arrays.asList(routeToCheck.getRequiredRoles()).contains(userRole);

            // Check if the user has the required role
            // to access the protected route
            if(!hasRequiredRole){
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return response.setComplete();
            }

            Claims claims = jwtUtil.getClaims(token);
            exchange.getRequest().mutate().header("id", String.valueOf(claims.get("id"))).build();
        }

        return chain.filter(exchange);
    }
}
