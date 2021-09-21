package limpo.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import limpo.gateway.dto.ProtectedRoute;
import limpo.gateway.service.JwtUserDetailsService;
import limpo.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Component
public class AuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtUserDetailsService service;

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



//    /**
//     * Process the Web request and (optionally) delegate to the next {@code WebFilter}
//     * through the given {@link GatewayFilterChain}.
//     *
//     * @param exchange the current server exchange
//     * @param chain    provides a way to delegate to the next filter
//     * @return {@code Mono<Void>} to indicate when request processing is complete
//     */
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        ServerHttpRequest request = exchange.getRequest();
//
//        String requestUrl = request.getURI().toString();
//        HttpMethod requestMethod = request.getMethod();
//
//        System.out.printf("\n[%s]: %s", requestMethod, requestUrl);
//
//        ProtectedRoute routeToCheck = getProtectedRoute(requestUrl, requestMethod);
//
//        // Check if the route is filtered as protected
//        if (routeToCheck != null) {
//            HttpHeaders headers = request.getHeaders();
//            List<String> authorization = headers.get("Authorization");
//
//            // Check if the HTTP Request includes the Authorization header
//            if (authorization == null) {
//                ServerHttpResponse response = exchange.getResponse();
//                response.setStatusCode(HttpStatus.UNAUTHORIZED);
//                return response.setComplete();
//            }
//
//
//            String token = authorization.get(0).substring(7);
//
//            // TODO: Validate token
//
//            String userRole ="";
//
//            try {
//                userRole = jwtUtil.extractAllClaims(token).get("role").toString();
//            }catch (ExpiredJwtException e){
//                System.out.println("\nToken is expired");
//                System.out.println(headers.get("refreshToken"));
//                // Send request to refresh the token here
//            }
//
//
//            boolean hasRequiredRole = Arrays.asList(routeToCheck.getRequiredRoles()).contains(userRole);
//
//            // Check if the user has the required role
//            // to access the protected route
//            if(!hasRequiredRole){
//                ServerHttpResponse response = exchange.getResponse();
//                response.setStatusCode(HttpStatus.FORBIDDEN);
//                return response.setComplete();
//            }
//
//            Claims claims = jwtUtil.getClaims(token);
//            exchange.getRequest().mutate().header("id", String.valueOf(claims.get("id"))).build();
//        }
//
//        return chain.filter(exchange);
//    }

    /**
     * Same contract as for {@code doFilter}, but guaranteed to be
     * just invoked once per request within a single request thread.
     * See {@link #shouldNotFilterAsyncDispatch()} for details.
     * <p>Provides HttpServletRequest and HttpServletResponse arguments instead of the
     * default ServletRequest and ServletResponse ones.
     *
     * @param request
     * @param response
     * @param filterChain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        String token = null;
        String email = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            email = jwtUtil.extractEmail(token);
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = service.loadUserByUsername(email);

            if (jwtUtil.validateToken(token, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }


        filterChain.doFilter(request, response);
    }
}
