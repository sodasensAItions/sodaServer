package com.sodasensaitions.backend.inventory;

import com.sodasensaitions.backend.orders.pojo.Drink;
import com.sodasensaitions.backend.orders.pojo.SodaOrder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    return ingredientList.orElseGet(List::of);
  }

  public boolean isInStock(SodaOrder sodaOrder) {
    List<Drink> drinks = sodaOrder.getDrinks();
    HashMap<Integer, Integer> required = new HashMap<>();

    for (Drink d : drinks) {
      int baseID = d.getBase().getId();
      Integer requiredOrDefault = required.getOrDefault(baseID, 0);
      requiredOrDefault++;
      required.put(baseID, requiredOrDefault);

      var flavors = d.getFlavors();
      for (var f : flavors) {
        requiredOrDefault = required.getOrDefault(f.getId(), 0);
        requiredOrDefault++;
        required.put(f.getId(), requiredOrDefault);
      }
    }

    Set<Integer> keySet = required.keySet();
    for (Integer key : keySet) {
      Optional<Ingredient> ingredientOptional = ingredientRepository.findById(key);
      if (ingredientOptional.isEmpty()) {
        System.out.println("not found");
        return false;
      }
      Ingredient ingredient = ingredientOptional.get();
      if (ingredient.getQuantity() < required.get(key)) {
        return false;
      }
    }

    return true;
  }

  public void reduceAvailableStock(SodaOrder sodaOrder) {
    List<Drink> drinks = sodaOrder.getDrinks();

    for (Drink d : drinks) {
      int baseID = d.getBase().getId();
      Optional<Ingredient> baseIngredientOptional = ingredientRepository.findById(baseID);
      if (baseIngredientOptional.isEmpty()) {
        return;
      }
      Ingredient baseIngredient = baseIngredientOptional.get();
      baseIngredient.setQuantity(baseIngredient.getQuantity() - 1);
      ingredientRepository.save(baseIngredient);

      var flavors = d.getFlavors();
      for (var f : flavors) {
        Optional<Ingredient> flavorIngredientOptional = ingredientRepository.findById(f.getId());
        if (flavorIngredientOptional.isEmpty()) {
          return;
        }
        Ingredient flavorIngredient = flavorIngredientOptional.get();
        flavorIngredient.setQuantity(flavorIngredient.getQuantity() - f.getQuantity());
        ingredientRepository.save(flavorIngredient);
      }
    }
  }
}
