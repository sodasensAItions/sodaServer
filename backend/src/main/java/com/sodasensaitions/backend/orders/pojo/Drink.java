package com.sodasensaitions.backend.orders.pojo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Drink {

  @Id
  @GeneratedValue
  public Integer id;

  @ManyToOne(cascade = CascadeType.ALL)
  private BaseIngredient base;

  @Column
  @ManyToMany(cascade = CascadeType.ALL)
  private List<FlavorIngredient> flavors;

  @ManyToOne
  private SodaOrder sodaOrder;

  public Drink(BaseIngredient base, List<FlavorIngredient> flavors) {
    this.base = base;
    this.flavors = flavors;
  }

}
