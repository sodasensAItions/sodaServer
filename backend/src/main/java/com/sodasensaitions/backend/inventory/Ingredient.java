package com.sodasensaitions.backend.inventory;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EqualsAndHashCode
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column
    public String name;

    @Column
    public int ingredientId;

    @Column
    public boolean isBase;

    @Column
    public int quantity;

    public Ingredient(String name, int quantity, int ingredientId, boolean isBase) {
        this.name = name;
        this.quantity = quantity;
        this.ingredientId = ingredientId;
        this.isBase = isBase;
    }
}
