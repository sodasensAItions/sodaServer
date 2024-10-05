package com.sodasensaitions.backend.authentication.auth.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangePasswordRequest {

  private final String currentPassword;
  private final String newPassword;
  private final String confirmationPassword;

}
