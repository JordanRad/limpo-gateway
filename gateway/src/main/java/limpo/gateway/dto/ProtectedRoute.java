package limpo.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
@AllArgsConstructor
public class ProtectedRoute {
    private String urlMatcher;

    private HttpMethod method;

    private String[] requiredRoles;
}
