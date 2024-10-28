package com.sodasensaitions.backend.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("inventory")
@RequiredArgsConstructor
public class IngredientController {
    private final IngredientService ingredientService;

    @GetMapping("/all")
    public ResponseEntity<Ingredient[]> getAll() {
        return ResponseEntity.ok(ingredientService.getAllAvailable());
    }
}
