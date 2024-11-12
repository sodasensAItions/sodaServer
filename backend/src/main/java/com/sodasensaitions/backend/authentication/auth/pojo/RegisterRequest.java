package com.sodasensaitions.backend.authentication.auth.pojo;

import com.google.gson.JsonObject;
import com.sodasensaitions.backend.authentication.user.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
  private String firstname;
  private String lastname;
  private String username;
  private String email;
  private String password;

  public RegisterRequest(Account account) {
    this.firstname = account.getFirstName();
    this.lastname = account.getLastName();
    this.username = account.getUsername();
    this.email = account.getEmail();
    this.password = account.getPassword();
  }

  public JsonObject getAsJsonObject() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("firstname", firstname);
    jsonObject.addProperty("lastname", lastname);
    jsonObject.addProperty("username", username);
    jsonObject.addProperty("email", email);
    jsonObject.addProperty("password", password);
    return jsonObject;
  }

}
