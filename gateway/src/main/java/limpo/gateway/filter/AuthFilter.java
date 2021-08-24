package limpo.gateway.filter;

import limpo.gateway.dto.ProtectedRoute;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
public class AuthFilter implements GatewayFilter {

    /**
     * This method constructs list of protected routes
     * @return protectedRoutes List<ProtectedRoute>
     */
    private List<ProtectedRoute> getProtectedRoutes() {
        List<ProtectedRoute> protectedRouteList = new ArrayList<>();
        protectedRouteList.add(new ProtectedRoute("/order-service/clients", HttpMethod.GET,new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/clients", HttpMethod.DELETE,new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/orders", HttpMethod.GET,new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/orders", HttpMethod.PUT,new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/orders", HttpMethod.DELETE,new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/limpoUnits", HttpMethod.POST,new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/limpoUnits", HttpMethod.PUT,new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/limpoUnits", HttpMethod.DELETE,new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/consumables", HttpMethod.GET,new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/consumables", HttpMethod.POST,new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/consumables", HttpMethod.PUT,new String[]{"ROLE_ADMIN"}));
        protectedRouteList.add(new ProtectedRoute("/order-service/consumables", HttpMethod.DELETE,new String[]{"ROLE_ADMIN"}));
        return protectedRouteList;
    }

    /**
     * Check if a given url is a protected route (Authentication)
     * @param route - Route to check (URL)
     * @return boolean
     */
    private boolean isRouteProtected(String url, HttpMethod method) {

        for (ProtectedRoute route : getProtectedRoutes()) {
            if (url.contains(route.getUrlMatcher()) && route.getMethod().equals(method)) {
                return true;
            }
        }
        return false;

    }

    /**
     * Check if authenticated route requires a particular role (Authorisation)
     */
    private boolean authorise(String role){
        return true;
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

        System.out.printf("\n[%s]: %s",requestMethod,requestUrl);

        if (isRouteProtected(requestUrl,requestMethod)){
            System.out.print(" -> protected");
        }
        else{
            System.out.print(" -> unprotected");
        }


        return chain.filter(exchange);
    }
}
