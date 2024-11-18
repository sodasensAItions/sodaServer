package com.sodasensaitions.backend.inventory;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IngredientService {
    private final IngredientRepository ingredientRepository;

    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    public List<Ingredient> getAvailableIngredients() {
        Optional<List<Ingredient>> ingredientList = ingredientRepository.findByQuantityGreaterThan(1);

        // Returns the list of ingredients, or an empty list if there are no ingredients returned.
        // May want to change the other return in case we don't want an empty list on failures.
        if (ingredientList.isPresent()) {
            return ingredientList.get();
        } else {
            return List.of();
        }
    }

}
