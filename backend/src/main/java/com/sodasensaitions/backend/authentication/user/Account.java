package com.sodasensaitions.backend.authentication.user;

import com.google.gson.JsonObject;
import com.sodasensaitions.backend.authentication.token.Token;
import com.sodasensaitions.backend.converter.database.LocalDateTimeConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Account implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false, updatable = false)
  private String username;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @OneToMany(mappedBy = "account")
  private List<Token> tokens;

  @Column(updatable = false)
  @Convert(converter = LocalDateTimeConverter.class)
  @CreationTimestamp
  private LocalDateTime created;

  public Account(String username, String email, String password, String firstName, String lastName) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public JsonObject getAsJsonObject() {
    JsonObject json = new JsonObject();
    json.addProperty("id", id);
    json.addProperty("username", username);
    json.addProperty("email", email);
    json.addProperty("firstName", firstName);
    json.addProperty("lastName", lastName);

    return json;
  }


  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return null;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}