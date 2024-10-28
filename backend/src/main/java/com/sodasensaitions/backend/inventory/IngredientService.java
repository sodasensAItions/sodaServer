package com.sodasensaitions.backend.inventory;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class IngredientService {
    private final IngredientRepository ingredientRepository;

    public Ingredient[] getAllAvailable() {
        return ingredientRepository.getAllIngredients().orElse(null);
    }
}
