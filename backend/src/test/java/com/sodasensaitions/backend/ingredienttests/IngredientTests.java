package com.sodasensaitions.backend.ingredienttests;

import com.google.gson.Gson;
import com.sodasensaitions.backend.authentication.auth.pojo.AuthenticationResponse;
import com.sodasensaitions.backend.authentication.user.Account;
import com.sodasensaitions.backend.inventory.Ingredient;
import com.sodasensaitions.backend.inventory.IngredientRepository;
import com.sodasensaitions.backend.utils.RegisteringUtils;
import org.junit.Assert;
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

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IngredientTests {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private RegisteringUtils registeringUtils;

  @Autowired
  private IngredientRepository ingredientRepository;

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
  public void testGetAllIngredients() {
    Account account = new Account("getAllIngredients", "testGetAllIngredients@usu.edu", "mySecretPassword", "John", "Doe");
    ResponseEntity<AuthenticationResponse> responseEntity = registeringUtils.tryRegistering(account, HttpStatus.OK);
    Ingredient[] ingredientsToSave = {new Ingredient("test1", 10), new Ingredient("test2", 10), new Ingredient("test3", 10)};
    ingredientRepository.saveAll(Arrays.asList(ingredientsToSave));

    //Verify that all the ingredients are returned from the database
    AuthenticationResponse authenticationResponse = responseEntity.getBody();
    Assert.assertNotNull(authenticationResponse);
    String token = authenticationResponse.getAccessToken();
    Assert.assertNotNull(token);

    String url = myURL() + "/inventory/all";
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    HttpEntity<String> httpEntity = new HttpEntity<>(headers);

    ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

    Ingredient[] ingredientResponse = gson.fromJson(response.getBody(), Ingredient[].class);
    Ingredient[] ingredientsFromDb = new Ingredient[ingredientRepository.findAll().size()];
    ingredientRepository.findAll().toArray(ingredientsFromDb);

    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assert.assertArrayEquals(ingredientResponse, ingredientsFromDb);
    System.out.println(response.getBody());
  }


}
