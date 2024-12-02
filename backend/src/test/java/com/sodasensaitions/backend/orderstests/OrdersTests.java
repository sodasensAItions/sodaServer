package com.sodasensaitions.backend.orderstests;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sodasensaitions.backend.authentication.auth.pojo.AuthenticationResponse;
import com.sodasensaitions.backend.authentication.user.Account;
import com.sodasensaitions.backend.authentication.user.AccountRepository;
import com.sodasensaitions.backend.inventory.Ingredient;
import com.sodasensaitions.backend.inventory.IngredientRepository;
import com.sodasensaitions.backend.orders.OrdersRepository;
import com.sodasensaitions.backend.orders.pojo.BaseIngredient;
import com.sodasensaitions.backend.orders.pojo.Drink;
import com.sodasensaitions.backend.orders.pojo.FlavorIngredient;
import com.sodasensaitions.backend.orders.pojo.SodaOrder;
import com.sodasensaitions.backend.utils.RegisteringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrdersTests {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private RegisteringUtils registeringUtils;

  @Autowired
  private IngredientRepository ingredientRepository;

  @Autowired
  private OrdersRepository ordersRepository;

  @Autowired
  private AccountRepository accountRepository;

  @BeforeEach
  public void setup() {
    // clear all ingredients
    ingredientRepository.deleteAll();
    // insert 10 base ingredients
    for (int i = 0; i < 10; i++) {
      Ingredient ingredient = new Ingredient("base" + System.currentTimeMillis(), 100, i, true);
      ingredientRepository.save(ingredient);
    }

    // insert 10 flavor ingredients
    for (int i = 0; i < 10; i++) {
      Ingredient ingredient = new Ingredient("flavor" + System.currentTimeMillis(), 100, i + 10, false);
      ingredientRepository.save(ingredient);
    }
  }

  private final Gson gson = new Gson();

  private String myURL() {
    return "http://localhost:" + port;
  }


  @Lazy
  @TestConfiguration
  static class CustomerAccountsTestConfiguration {
    @Bean
    public RegisteringUtils registeringUtils(@Value("${local.server.port}") int port, @Autowired TestRestTemplate restTemplate) {
      return new RegisteringUtils(port, restTemplate);
    }
  }

  @Test
  public void testCreateOrder() {
    Account account = new Account("testCreateOrder", "testCreateOrder@usu.edu", "password", "testCreateOrder", "testCreateOrder");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = generateRandomDrinks(1);
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.OK);

    Assert.assertNotNull(newOrderID);
    // verify that the order was created in database
    ordersRepository.findById(newOrderID).ifPresentOrElse(
        x -> {
          Assert.assertEquals(newOrderID, x.getId());
          Assert.assertEquals(sodaOrder.getDrinks().size(), x.getDrinks().size());
        },
        () -> Assert.fail("Order not found in database")
    );

    // verify that the order belongs to the account
    Account accountFromDB = accountRepository.findByUsername(account.getUsername()).orElseThrow();
    Assert.assertTrue(accountFromDB.getSodaOrders().stream().anyMatch(x -> x.getId().equals(newOrderID)));
  }

  @Test
  public void testCreateOrderWithInvalidAccount() {
    Account account = new Account("testCreateOrderWithInvalidAccount", "testCreateOrderWithInvalidAccount@usu.edu", "password", "testCreateOrderWithInvalidAccount", "testCreateOrderWithInvalidAccount");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    registeringUtils.logout(authenticationResponse);

    ArrayList<Drink> drinks = generateRandomDrinks(1);
    SodaOrder sodaOrder = new SodaOrder(drinks);
    tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.FORBIDDEN);
  }

  @Test
  public void testCreateOrderWithMultipleDrinks() {
    Account account = new Account("testCreateOrderWithMultipleDrinks", "testCreateOrderWithMultipleDrinks@usu.edu", "password", "testCreateOrderWithMultipleDrinks", "testCreateOrderWithMultipleDrinks");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = generateRandomDrinks(3);
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.OK);

    Assert.assertNotNull(newOrderID);
    System.out.println(newOrderID);
  }

  @Test
  public void testCreateOrderWithNoFlavors() {
    Account account = new Account("testCreateOrderWithNoFlavors", "testCreateOrderWithNoFlavors@usu.edu", "password", "testCreateOrderWithNoFlavors", "testCreateOrderWithNoFlavors");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = new ArrayList<>();
    Ingredient baseIngredient = ingredientRepository.findAll().stream()
        .filter(Ingredient::isBase)
        .findFirst()
        .orElseThrow();

    drinks.add(new Drink(new BaseIngredient(baseIngredient.getId(), baseIngredient.getName()), new ArrayList<>()));
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.OK);

    Assert.assertNotNull(newOrderID);
    System.out.println(newOrderID);
  }

  @Test
  public void testCreateOrderWithNoBase() {
    Account account = new Account("testCreateOrderWithNoBase", "testCreateOrderWithNoBase@usu.edu", "password", "testCreateOrderWithNoBase", "testCreateOrderWithNoBase");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = new ArrayList<>();
    Ingredient flavorIngredient = ingredientRepository.findAll().stream()
        .filter(x -> !x.isBase())
        .findFirst()
        .orElseThrow();

    ArrayList<FlavorIngredient> flavorIngredients = new ArrayList<>();
    flavorIngredients.add(new FlavorIngredient(flavorIngredient.getId(), flavorIngredient.getName(), 1));
    drinks.add(new Drink(new BaseIngredient(-1, "No Base"), flavorIngredients));
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.BAD_REQUEST);

    Assert.assertNull(newOrderID);
  }

  @Test
  public void testCreateOrderWithNoIngredients() {
    Account account = new Account("testCreateOrderWithNoIngredients", "testCreateOrderWithNoIngredients@usu.edu", "password", "testCreateOrderWithNoIngredients", "testCreateOrderWithNoIngredients");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = new ArrayList<>();
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.BAD_REQUEST);

    Assert.assertNull(newOrderID);
  }

  @Test
  public void testCreateOrderWithInvalidBase() {
    Account account = new Account("testCreateOrderWithInvalidBase", "testCreateOrderWithInvalidBase", "password", "testCreateOrderWithInvalidBase", "testCreateOrderWithInvalidBase");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = new ArrayList<>();
    Ingredient flavorIngredient = ingredientRepository.findAll().stream()
        .filter(x -> !x.isBase())
        .findFirst()
        .orElseThrow();

    ArrayList<FlavorIngredient> flavorIngredients = new ArrayList<>();
    flavorIngredients.add(new FlavorIngredient(flavorIngredient.getId(), flavorIngredient.getName(), 1));
    drinks.add(new Drink(new BaseIngredient(-1, "No Base"), flavorIngredients));
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;

    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.BAD_REQUEST);
    Assert.assertNull(newOrderID);
  }

  @Test
  public void testCreateOrderWithInvalidFlavor() {
    Account account = new Account("testCreateOrderWithInvalidFlavor", "testCreateOrderWithInvalidFlavor@usu.edu", "password", "testCreateOrderWithInvalidFlavor", "testCreateOrderWithInvalidFlavor");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = new ArrayList<>();
    Ingredient baseIngredient = ingredientRepository.findAll().stream()
        .filter(Ingredient::isBase)
        .findFirst()
        .orElseThrow();

    ArrayList<FlavorIngredient> flavorIngredients = new ArrayList<>();
    flavorIngredients.add(new FlavorIngredient(-1, "No Flavor", 1));
    drinks.add(new Drink(new BaseIngredient(baseIngredient.getId(), baseIngredient.getName()), flavorIngredients));
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.BAD_REQUEST);

    Assert.assertNull(newOrderID);
  }

  @Test
  public void testCreateOrderWithInvalidFlavorQuantity() {
    Account account = new Account("testCreateOrderWithInvalidFlavorQuantity", "testCreateOrderWithInvalidFlavorQuantity@usu.edu", "password", "testCreateOrderWithInvalidFlavorQuantity", "testCreateOrderWithInvalidFlavorQuantity");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = new ArrayList<>();
    Ingredient baseIngredient = ingredientRepository.findAll().stream()
        .filter(Ingredient::isBase)
        .findFirst()
        .orElseThrow();

    Ingredient flavorIngredient = ingredientRepository.findAll().stream()
        .filter(x -> !x.isBase())
        .findFirst()
        .orElseThrow();

    ArrayList<FlavorIngredient> flavorIngredients = new ArrayList<>();
    flavorIngredients.add(new FlavorIngredient(flavorIngredient.getId(), flavorIngredient.getName(), 100));
    drinks.add(new Drink(new BaseIngredient(baseIngredient.getId(), baseIngredient.getName()), flavorIngredients));
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.BAD_REQUEST);
  }

  @Test
  public void testCreateOrderWithNegativeFlavorQuantity() {
    Account account = new Account("testCreateOrderWithNegativeFlavorQuantity", "testCreateOrderWithNegativeFlavorQuantity@usu.edu", "password", "testCreateOrderWithNegativeFlavorQuantity", "testCreateOrderWithNegativeFlavorQuantity");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = new ArrayList<>();
    Ingredient baseIngredient = ingredientRepository.findAll().stream()
        .filter(Ingredient::isBase)
        .findFirst()
        .orElseThrow();

    Ingredient flavorIngredient = ingredientRepository.findAll().stream()
        .filter(x -> !x.isBase())
        .findFirst()
        .orElseThrow();

    ArrayList<FlavorIngredient> flavorIngredients = new ArrayList<>();
    flavorIngredients.add(new FlavorIngredient(flavorIngredient.getId(), flavorIngredient.getName(), -1));
    drinks.add(new Drink(new BaseIngredient(baseIngredient.getId(), baseIngredient.getName()), flavorIngredients));
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.BAD_REQUEST);
  }


  @Test
  public void testCreateOrderWithZeroFlavorQuantity() {
    Account account = new Account("testCreateOrderWithZeroFlavorQuantity", "testCreateOrderWithZeroFlavorQuantity@usu.edu", "password", "testCreateOrderWithZeroFlavorQuantity", "testCreateOrderWithZeroFlavorQuantity");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = new ArrayList<>();
    Ingredient baseIngredient = ingredientRepository.findAll().stream()
        .filter(Ingredient::isBase)
        .findFirst()
        .orElseThrow();

    Ingredient flavorIngredient = ingredientRepository.findAll().stream()
        .filter(x -> !x.isBase())
        .findFirst()
        .orElseThrow();

    ArrayList<FlavorIngredient> flavorIngredients = new ArrayList<>();
    flavorIngredients.add(new FlavorIngredient(flavorIngredient.getId(), flavorIngredient.getName(), 0));
    drinks.add(new Drink(new BaseIngredient(baseIngredient.getId(), baseIngredient.getName()), flavorIngredients));
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.BAD_REQUEST);
  }


  //create an order with out-of-stock elements
  @Test
  public void testCreateOrderWithOutOfStockElements() {
    Account account = new Account("testCreateOrderWithOutOfStockElements", "testCreateOrderWithOutOfStockElements@usu.edu", "password", "testCreateOrderWithOutOfStockElements", "testCreateOrderWithOutOfStockElements");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = generateRandomDrinks(1);

    // set the quantity of the first base ingredient to 0 so that the order will have an out-of-stock element

    var baseIngredient = drinks.getFirst().getBase();
    Optional<Ingredient> ingredientOptional = ingredientRepository.findById(baseIngredient.getId());
    if (ingredientOptional.isEmpty()) {
      Assert.fail("Base ingredient not found");
    }
    Ingredient ingredient = ingredientOptional.get();
    int quantity = ingredient.getQuantity();
    ingredient.setQuantity(0);
    ingredientRepository.save(ingredient);

    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.BAD_REQUEST);

    ingredient.setQuantity(quantity);
  }

  @Test
  public void testIsQuantityReduced(){
    Account account = new Account("testIsQuantityReduced", "testIsQuantityReduced@usu.edu", "password", "testIsQuantityReduced", "testIsQuantityReduced");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = generateRandomDrinks(1);

    HashMap<Integer, Integer> reducedBy = new HashMap<>();

    for(Drink d : drinks){
      Integer id = d.getBase().getId();
      Integer getOrDefault = reducedBy.getOrDefault(id, 0);
      getOrDefault++;
      reducedBy.put(id, getOrDefault);

      for(FlavorIngredient f : d.getFlavors()){
        id = f.getId();
        getOrDefault = reducedBy.getOrDefault(id, 0);
        reducedBy.put(id, getOrDefault + f.getQuantity());
      }
    }

    // store current quantities for comparison
    HashMap<Integer, Integer> currentQuantities = new HashMap<>();
    for (Map.Entry<Integer, Integer> entry : reducedBy.entrySet()) {
      Optional<Ingredient> ingredientOptional = ingredientRepository.findById(entry.getKey());
      if (ingredientOptional.isEmpty()) {
        Assert.fail("Ingredient not found");
      }
      Ingredient ingredient = ingredientOptional.get();
      currentQuantities.put(ingredient.getId(), ingredient.getQuantity());
    }

    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;

    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.OK);
    Assert.assertNotNull(newOrderID);

    // verify that stock was reduced in database
    for (Map.Entry<Integer, Integer> entry : reducedBy.entrySet()) {
      Optional<Ingredient> ingredientOptional = ingredientRepository.findById(entry.getKey());
      if (ingredientOptional.isEmpty()) {
        Assert.fail("Ingredient not found");
      }
      Ingredient ingredient = ingredientOptional.get();
      Assert.assertEquals(currentQuantities.get(ingredient.getId()) - entry.getValue(), ingredient.getQuantity());
    }
  }

  @Test
  public void testCompleteOrder() {
    Account account = new Account("testCompleteOrder", "testCompleteOrder@usu.edu", "password", "testCompleteOrder", "testCompleteOrder");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = generateRandomDrinks(1);
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.OK);

    Assert.assertNotNull(newOrderID);
    tryCompletingOrder(authenticationResponse, newOrderID, HttpStatus.OK);
  }

  @Test
  public void testCompleteOrderWithInvalidAccount() {
    Account account = new Account("testCompleteOrderWithInvalidAccount", "testCompleteOrderWithInvalidAccount@usu.edu", "password", "testCompleteOrderWithInvalidAccount", "testCompleteOrderWithInvalidAccount");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = generateRandomDrinks(1);
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.OK);

    Assert.assertNotNull(newOrderID);
    tryCompletingOrder(authenticationResponse, newOrderID, HttpStatus.OK);

    // logout and try to complete the order again
    registeringUtils.logout(authenticationResponse);

    tryCompletingOrder(authenticationResponse, newOrderID, HttpStatus.FORBIDDEN);
  }

  @Test
  public void testCompleteOrderWithInvalidOrder() {
    Account account = new Account("testCompleteOrderWithInvalidOrder", "testCompleteOrderWithInvalidOrder@usu.edu", "password", "testCompleteOrderWithInvalidOrder", "testCompleteOrderWithInvalidOrder");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = generateRandomDrinks(1);
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.OK);

    Assert.assertNotNull(newOrderID);
    tryCompletingOrder(authenticationResponse, newOrderID, HttpStatus.OK);

    // try to complete an order that does not exist
    tryCompletingOrder(authenticationResponse, newOrderID + 1, HttpStatus.BAD_REQUEST);
  }

  @Test
  public void testCompleteOrderWithCompletedOrder() {
    Account account = new Account("testCompleteOrderWithCompletedOrder", "testCompleteOrderWithCompletedOrder@usu.edu", "password", "testCompleteOrderWithCompletedOrder", "testCompleteOrderWithCompletedOrder");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = generateRandomDrinks(1);
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.OK);

    Assert.assertNotNull(newOrderID);
    tryCompletingOrder(authenticationResponse, newOrderID, HttpStatus.OK);

    // try to complete an order that has already been completed
    tryCompletingOrder(authenticationResponse, newOrderID, HttpStatus.OK);
  }

  @Test
  public void testGetOrderHistory() {
    Account account = new Account("testGetOrderHistory", "testGetOrderHistory@usu.edu", "password", "testGetOrderHistory", "testGetOrderHistory");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = generateRandomDrinks(1);
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.OK);

    Assert.assertNotNull(newOrderID);
    tryCompletingOrder(authenticationResponse, newOrderID, HttpStatus.OK);

    String orderHistory = tryGettingOrderHistory(authenticationResponse, HttpStatus.OK);
    System.out.println(orderHistory);

    JsonArray jsonArray = gson.fromJson(orderHistory, JsonArray.class);
    Assert.assertEquals(1, jsonArray.size());
  }

  @Test
  public void testGettingOrderHistoryWithInvalidAccount() {
    Account account = new Account("testGettingOrderHistoryWithInvalidAccount", "testGettingOrderHistoryWithInvalidAccount@usu.edu", "password", "testGettingOrderHistoryWithInvalidAccount", "testGettingOrderHistoryWithInvalidAccount");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = generateRandomDrinks(1);
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.OK);

    Assert.assertNotNull(newOrderID);
    tryCompletingOrder(authenticationResponse, newOrderID, HttpStatus.OK);

    // logout and try to get order history
    registeringUtils.logout(authenticationResponse);

    tryGettingOrderHistory(authenticationResponse, HttpStatus.FORBIDDEN);
  }

  @Test
  public void testGetOrderHistoryWithNoOrders() {
    Account account = new Account("testGetOrderHistoryWithNoOrders", "testGetOrderHistoryWithNoOrders@usu.edu", "password", "testGetOrderHistoryWithNoOrders", "testGetOrderHistoryWithNoOrders");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    AuthenticationResponse body = authenticationResponseResponseEntity.getBody();
    assert body != null;
    String orderHistory = tryGettingOrderHistory(body, HttpStatus.OK);
    System.out.println(orderHistory);
  }

  @Test
  public void testGetOrderHistoryWithSpecificOrder() {
    Account account = new Account("testGetOrderHistoryWithSpecificOrder", "testGetOrderHistoryWithSpecificOrder@usu.edu", "password", "testGetOrderHistoryWithSpecificOrder", "testGetOrderHistoryWithSpecificOrder");
    ResponseEntity<AuthenticationResponse> authenticationResponseResponseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);

    ArrayList<Drink> drinks = generateRandomDrinks(1);
    SodaOrder sodaOrder = new SodaOrder(drinks);
    AuthenticationResponse authenticationResponse = authenticationResponseResponseEntity.getBody();
    assert authenticationResponse != null;
    Integer newOrderID = tryCreatingOrder(authenticationResponse, sodaOrder, HttpStatus.OK);

    Assert.assertNotNull(newOrderID);
    tryCompletingOrder(authenticationResponse, newOrderID, HttpStatus.OK);

    String orderHistory = tryGettingOrderHistory(authenticationResponse, HttpStatus.OK, newOrderID);
    System.out.println(orderHistory);
  }

  private Integer tryCreatingOrder(AuthenticationResponse authenticationResponse, SodaOrder sodaOrder, HttpStatus expectedStatus) {
    String url = myURL() + "/orders/create";
    String orderJson = orderToJson(sodaOrder);

    // do not set interceptors for this request because method will be called for a different acccount every time
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(authenticationResponse.getAccessToken());
    HttpEntity<String> httpEntity = new HttpEntity<>(orderJson, httpHeaders);
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<Integer> response = this.restTemplate.exchange(url, HttpMethod.POST, httpEntity, Integer.class);

    Assert.assertEquals(expectedStatus, response.getStatusCode());
    return response.getBody();
  }

  private String orderToJson(SodaOrder sodaOrder) {
    JsonArray root = new JsonArray();
    for (Drink drink : sodaOrder.getDrinks()) {
      JsonObject drinkJson = new JsonObject();
      drinkJson.addProperty("baseIngredientId", drink.getBase().getId());

      JsonArray flavorIngredients = new JsonArray();
      for (var flavorIngredient : drink.getFlavors()) {
        JsonObject flavorIngredientJson = new JsonObject();
        flavorIngredientJson.addProperty("flavorIngredientId", flavorIngredient.getId());
        flavorIngredientJson.addProperty("quantity", flavorIngredient.getQuantity());
        flavorIngredients.add(flavorIngredientJson);
      }
      drinkJson.add("flavorIngredients", flavorIngredients);
      root.add(drinkJson);
    }
    return root.toString();
  }

  private ArrayList<Drink> generateRandomDrinks(int length) {
    ArrayList<Drink> drinks = new ArrayList<>();
    Random random = new Random();

    List<Ingredient> baseIngredients = new ArrayList<>(ingredientRepository.findAll().stream()
        .filter(Ingredient::isBase)
        .toList());
    if (baseIngredients.isEmpty()) {
      Assert.fail("No base ingredient found");
    }
    Collections.shuffle(baseIngredients);

    List<Ingredient> flavorIngredients = new ArrayList<>(ingredientRepository.findAll().stream()
        .filter(x -> !x.isBase())
        .toList());
    if (flavorIngredients.isEmpty()) {
      Assert.fail("No flavor ingredient found");
    }
    Collections.shuffle(flavorIngredients);

    for (int drinkCnt = 0; drinkCnt < length; drinkCnt++) {
      Ingredient baseIngredientInInventory = baseIngredients.get(random.nextInt(baseIngredients.size()));
      BaseIngredient baseIngredient = new BaseIngredient(baseIngredientInInventory.getId(), baseIngredientInInventory.getName());

      ArrayList<FlavorIngredient> selectedFlavorIngredients = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        Ingredient flavorIngredientInInventory = flavorIngredients.get(random.nextInt(flavorIngredients.size()));
        FlavorIngredient flavorIngredient = new FlavorIngredient(flavorIngredientInInventory.getId(), flavorIngredientInInventory.getName(), random.nextInt(5) + 1);
        selectedFlavorIngredients.add(flavorIngredient);
      }

      drinks.add(new Drink(baseIngredient, selectedFlavorIngredients));
    }

    return drinks;
  }

  private void tryCompletingOrder(AuthenticationResponse authenticationResponse, Integer orderID, HttpStatus expectedStatus) {
    String url = myURL() + "/orders/here/" + orderID;

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(authenticationResponse.getAccessToken());
    HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

    ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

    Assert.assertEquals(expectedStatus, response.getStatusCode());
  }

  private String tryGettingOrderHistory(AuthenticationResponse authenticationResponse, HttpStatus expectedStatus) {
    String url = myURL() + "/orders/history";

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(authenticationResponse.getAccessToken());
    HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

    ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

    Assert.assertEquals(expectedStatus, response.getStatusCode());
    return response.getBody();
  }

  private String tryGettingOrderHistory(AuthenticationResponse authenticationResponse, HttpStatus expectedStatus, Integer orderID) {
    String url = myURL() + "/orders/history/" + orderID;

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(authenticationResponse.getAccessToken());
    HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

    ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

    Assert.assertEquals(expectedStatus, response.getStatusCode());
    return response.getBody();
  }

}
