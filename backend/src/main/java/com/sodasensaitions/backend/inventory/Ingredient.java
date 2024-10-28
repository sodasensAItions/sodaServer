package com.sodasensaitions.backend.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
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
public class Ingredient {
    @Id
    @GeneratedValue
    public Integer id;

    @Column(unique = true)
    public String name;

    @Column
    public int quantity;
}
