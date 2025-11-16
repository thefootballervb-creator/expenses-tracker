package com.fullStack.expenseTracker.dto.reponses;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class JwtResponseDto {
    private String token;
    @Default
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
}
