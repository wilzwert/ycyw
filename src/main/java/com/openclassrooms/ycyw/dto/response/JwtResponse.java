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

  private String type = "Bearer";

  private String username;
}
