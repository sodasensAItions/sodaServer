package com.sodasensaitions.backend.orders;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sodasensaitions.backend.authentication.user.Account;
import com.sodasensaitions.backend.authentication.user.AccountRepository;
import com.sodasensaitions.backend.config.constants.HttpServletSessionConstants;
import com.sodasensaitions.backend.inventory.Ingredient;
import com.sodasensaitions.backend.inventory.IngredientRepository;
import com.sodasensaitions.backend.orders.pojo.BaseIngredient;
import com.sodasensaitions.backend.orders.pojo.Drink;
import com.sodasensaitions.backend.orders.pojo.FlavorIngredient;
import com.sodasensaitions.backend.orders.pojo.SodaOrder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrdersController {

  private final AccountRepository accountRepository;
  private final OrdersService ordersService;
  private final Gson gson = new Gson();
  private final IngredientRepository ingredientRepository;

  @PostMapping("/create")
  public ResponseEntity<Integer> createOrder(@RequestBody String tmp, @NonNull HttpServletRequest request) {
    // Find the user who is making the order
    String username = (String) request.getSession().getAttribute(HttpServletSessionConstants.PRINCIPAL);
    Optional<Account> usernameOptional = accountRepository.findByUsername(username);
    if (usernameOptional.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    // Parse the soda order from the JSON string
    SodaOrder sodaOrder = sodaOrderFromJsonIncludingChecks(tmp);
    if(sodaOrder == null) {
      return ResponseEntity.badRequest().build();
    }


    // Store the soda order in the database
    Account account = usernameOptional.get();
    SodaOrder savedSodaOrder = ordersService.createOrder(account, sodaOrder);
    if(savedSodaOrder == null) {
      return ResponseEntity.badRequest().build();
    }

    // Return the ID of the newly created order
    return ResponseEntity.ok(savedSodaOrder.getId());
  }

  @GetMapping("/getopenorders")
  public ResponseEntity<String> getOpenOrders(@NonNull HttpServletRequest request) {
    // Find the user who is making the order
    String username = (String) request.getSession().getAttribute(HttpServletSessionConstants.PRINCIPAL);
    Optional<Account> usernameOptional = accountRepository.findByUsername(username);
    if (usernameOptional.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    // Get the user's open orders
    Account account = usernameOptional.get();
    JsonArray jsonArray = sodaOrdersToJson(account.getSodaOrders().stream().filter(sodaOrder -> !sodaOrder.isCompleted())::iterator);
    return ResponseEntity.ok(jsonArray.toString());
  }

  @PostMapping("here/{orderID}")
  public ResponseEntity<String> iamhere(@PathVariable("orderID") Integer orderID, @NonNull HttpServletRequest request){
    //verify that this is my order
    String username = (String) request.getSession().getAttribute(HttpServletSessionConstants.PRINCIPAL);
    Optional<Account> usernameOptional = accountRepository.findByUsername(username);
    if (usernameOptional.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    //if it is my order then complete it
    boolean b = ordersService.completeOrder(usernameOptional.get(), orderID);
    return b ? ResponseEntity.ok("Order completed") : ResponseEntity.badRequest().build();
  }

  @GetMapping("/history")
  public ResponseEntity<String> getHistory(@NonNull HttpServletRequest request) {
    // Find the user who is making the order
    String username = (String) request.getSession().getAttribute(HttpServletSessionConstants.PRINCIPAL);
    Optional<Account> usernameOptional = accountRepository.findByUsername(username);
    if (usernameOptional.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    // Get the user's order history
    Account account = usernameOptional.get();
    JsonArray jsonArray = sodaOrdersToJson(account.getSodaOrders());
    return ResponseEntity.ok(jsonArray.toString());
  }

  @GetMapping("/history/{orderID}")
  public ResponseEntity<String> getOrder(@PathVariable("orderID") Integer orderID, @NonNull HttpServletRequest request) {
    String username = (String) request.getSession().getAttribute(HttpServletSessionConstants.PRINCIPAL);
    Optional<Account> usernameOptional = accountRepository.findByUsername(username);
    if (usernameOptional.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    Optional<SodaOrder> sodaOrderOptional = ordersService.getOrder(orderID);
    return sodaOrderOptional.map(sodaOrder -> ResponseEntity.ok(sodaOrderToJson(sodaOrder).toString())).orElseGet(() -> ResponseEntity.badRequest().build());
  }

  @PostMapping("/save")
  public ResponseEntity<Void> saveDrink(@RequestBody String tmp, @NonNull HttpServletRequest request) {
    String username = (String) request.getSession().getAttribute(HttpServletSessionConstants.PRINCIPAL);
    Optional<Account> usernameOptional = accountRepository.findByUsername(username);
    if (usernameOptional.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }
    Account account = usernameOptional.get();

    SodaOrder sodaOrder = sodaOrderFromJsonIncludingChecks(tmp);
    if(sodaOrder == null) {
      return ResponseEntity.badRequest().build();
    }

    ordersService.saveDrinkToFavorites(account, sodaOrder.getDrinks());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/favorites")
  public ResponseEntity<String> getFavorites(@NonNull HttpServletRequest request) {
    String username = (String) request.getSession().getAttribute(HttpServletSessionConstants.PRINCIPAL);
    Optional<Account> usernameOptional = accountRepository.findByUsername(username);
    if (usernameOptional.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }
    Account account = usernameOptional.get();

    JsonArray jsonArray = drinksToJson(account.getSavedDrinks());
    return ResponseEntity.ok(jsonArray.toString());
  }

  private SodaOrder sodaOrderFromJsonIncludingChecks(String tmp) {
    JsonArray root;
    try {
      root = gson.fromJson(tmp, JsonArray.class);
    } catch (Exception e) {
      return null;
    }

    SodaOrder sodaOrder = sodaOrderFromJson(root);
    if(sodaOrder == null) {
      return null;
    }

    if(sodaOrder.getDrinks().isEmpty()) {
      return null;
    }

    // check if any flavor ingredient has more than 10 pumps
    for(Drink drink : sodaOrder.getDrinks()) {
      for(var flavor : drink.getFlavors()) {
        if(flavor.getQuantity() > 10 || flavor.getQuantity() < 1) {
          return null;
        }
      }
    }

    return sodaOrder;
  }


  private SodaOrder sodaOrderFromJson(JsonArray root) {
    ArrayList<Drink> drinks = new ArrayList<>();

    for (JsonElement element : root) {
      JsonObject drinkJson = element.getAsJsonObject();

      // Extract base ingredient
      JsonElement baseIngredientIdJson = drinkJson.get("baseIngredientId");
      if (baseIngredientIdJson == null) {
        System.out.println("baseIngredientIdJson is null");
        return null;
      }

      int baseIngredientId = baseIngredientIdJson.getAsInt();
      Optional<Ingredient> baseIngredientOptional = ingredientRepository.findById(baseIngredientId);
      if (baseIngredientOptional.isEmpty()) {
        System.out.println("baseIngredientOptional is empty");
        return null;
      }

      Ingredient baseIngredientInInventory = baseIngredientOptional.get();
      BaseIngredient baseIngredient = new BaseIngredient(baseIngredientInInventory.getId(), baseIngredientInInventory.getName());

      // Extract flavor ingredients
      JsonArray flavorIngredientsJson = drinkJson.getAsJsonArray("flavorIngredients");
      ArrayList<FlavorIngredient> flavorIngredients = new ArrayList<>();
      for (JsonElement flavorElement : flavorIngredientsJson) {
        JsonObject flavorJson = flavorElement.getAsJsonObject();

        JsonElement flavorIngredientIdJson = flavorJson.get("flavorIngredientId");
        if(flavorIngredientIdJson == null) {
          System.out.println("flavorIngredientIdJson is null");
          return null;
        }
        int flavorIngredientId = flavorIngredientIdJson.getAsInt();

        JsonElement quantityJson = flavorJson.get("quantity");
        if(quantityJson == null) {
          System.out.println("quantityJson is null");
          return null;
        }
        int quantity = quantityJson.getAsInt();

        Optional<Ingredient> flavorIngredientOptional = ingredientRepository.findById(flavorIngredientId);
        if (flavorIngredientOptional.isEmpty()) {
          System.out.println("flavorIngredientOptional is empty");
          return null;
        }
        Ingredient flavorIngredientInInventory = flavorIngredientOptional.get();
        FlavorIngredient flavorIngredient = new FlavorIngredient( flavorIngredientId, flavorIngredientInInventory.getName(), quantity);
        flavorIngredients.add(flavorIngredient);
      }

      drinks.add(new Drink(baseIngredient, flavorIngredients));
    }

    return new SodaOrder(drinks);
  }


  private JsonArray sodaOrdersToJson(Iterable<SodaOrder> sodaOrders) {
    JsonArray ordersJson = new JsonArray();
    for (SodaOrder sodaOrder : sodaOrders) {
      ordersJson.add(sodaOrderToJson(sodaOrder));
    }

    return ordersJson;
  }

  private JsonObject sodaOrderToJson(SodaOrder sodaOrder) {
    JsonObject orderJson = new JsonObject();
    orderJson.addProperty("id", sodaOrder.getId());
    orderJson.addProperty("completed", sodaOrder.isCompleted());
    orderJson.addProperty("createdAt", sodaOrder.getCreated().toString());

    orderJson.add("drinks", drinksToJson(sodaOrder.getDrinks()));

    return orderJson;
  }

  private JsonArray drinksToJson(Iterable<Drink> drinks) {
    JsonArray drinksJson = new JsonArray();
    for (Drink drink : drinks) {
      JsonObject drinkJson = new JsonObject();
      drinkJson.addProperty("baseIngredientId", drink.getBase().getId());

      JsonArray flavorIngredientsJson = new JsonArray();
      for (FlavorIngredient flavorIngredient : drink.getFlavors()) {
        JsonObject flavorIngredientJson = new JsonObject();
        flavorIngredientJson.addProperty("flavorIngredientId", flavorIngredient.getId());
        flavorIngredientJson.addProperty("quantity", flavorIngredient.getQuantity());

        flavorIngredientsJson.add(flavorIngredientJson);
      }

      drinkJson.add("flavorIngredients", flavorIngredientsJson);
      drinksJson.add(drinkJson);
    }

    return drinksJson;
  }

}