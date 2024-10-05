package com.sodasensaitions.backend.authentication.token;

import com.sodasensaitions.backend.authentication.user.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Token {

  @Id
  @GeneratedValue
  public Integer id;

  @Column(unique = true)
  public String token;

  public boolean revoked;

  public boolean expired;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  public Account account;

}


