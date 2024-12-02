package com.sodasensaitions.backend.orders.pojo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class BaseIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int idInInventory;
    private String name;

    public BaseIngredient(int idInInventory, String name){
        this.idInInventory = idInInventory;
        this.name = name;
    }

    public int getId() {
        return idInInventory;
    }
}
