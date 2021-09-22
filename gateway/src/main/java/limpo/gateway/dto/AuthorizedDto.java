package limpo.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorizedDto {
    private String email;

    private String token;

    private String role;

    private String refreshToken;
}