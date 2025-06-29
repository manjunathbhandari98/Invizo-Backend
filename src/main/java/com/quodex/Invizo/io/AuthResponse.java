package com.quodex.Invizo.io;
import com.quodex.Invizo.util.Role;
import lombok.*;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String email;
    private Role role;
    private String token;
}
