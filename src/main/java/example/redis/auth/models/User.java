package example.redis.auth.models;


import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String username;
    private String email;
    private String passwordHash;
}
