package com.sodasensaitions.backend.paymenttests;

import com.sodasensaitions.backend.authentication.user.Account;
import com.sodasensaitions.backend.authentication.user.AccountRepository;
import com.sodasensaitions.backend.payment.PaymentPOJO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentTests {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private RegisteringUtils registeringUtils;


  @Lazy
  @TestConfiguration
  static class PaymentTestConfiguration {
    @Bean
    public RegisteringUtils registeringUtils(@Value("${local.server.port}") int port, @Autowired TestRestTemplate restTemplate) {
      return new RegisteringUtils(port, restTemplate);
    }
  }

  private String myURL() {
    return "http://localhost:" + port;
  }

  @Test
  public void testPayment() {
    Account account = new Account("testPayment", "testPayment@usu.edu", "mySecretPassword", "John", "Doe");
    registeringUtils.tryRegistering(account, HttpStatus.OK);

    PaymentPOJO usd = new PaymentPOJO(1000, "usd");
    ResponseEntity<String> responseEntity = restTemplate.postForEntity(myURL() + "/payments/create-payment-intent", usd, String.class);

    Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

}
