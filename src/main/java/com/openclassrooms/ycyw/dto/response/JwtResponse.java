package com.openclassrooms.ycyw.dto.response;

import lombok.*;

/**
 * DTO used for login success response
 * @author Wilhelm Zwertvaegher
 */

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class JwtResponse {
  private String token;

  private String type = "anonymous";

  private String username;

  private String role;
}
