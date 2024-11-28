package com.sodasensaitions.backend.orders.pojo;

import com.sodasensaitions.backend.authentication.user.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SodaOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<Drink> drinks;

  @ManyToOne
  private Account account;

  private boolean completed;

  @CreationTimestamp
  private LocalDateTime created;

  public SodaOrder(List<Drink> drinks) {
    this.drinks = drinks;
  }
}
