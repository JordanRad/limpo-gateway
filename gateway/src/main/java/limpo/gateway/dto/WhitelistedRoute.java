package limpo.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
@AllArgsConstructor
public class WhitelistedRoute {

    private String urlMatcher;

    private String method;
}
