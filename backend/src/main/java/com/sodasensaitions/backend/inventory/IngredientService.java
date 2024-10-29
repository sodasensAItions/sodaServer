package com.sodasensaitions.backend.inventory;

import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IngredientService {
    private final IngredientRepository ingredientRepository;

    public List<Ingredient> getAllAvailable() {
        return ingredientRepository.findAll();
    }

}
