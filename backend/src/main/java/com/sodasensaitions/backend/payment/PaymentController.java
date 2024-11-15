package com.sodasensaitions.backend.payment;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/payments")
public class PaymentController {

  @Value("${stripe.apiKey}")
  private String stripeApiKey;

  @PostConstruct
  public void init() {
    Stripe.apiKey = stripeApiKey;
  }

  @PostMapping("/create-payment-intent")
  public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody PaymentPOJO data) {
    try {
      long amount = data.getAmount();
      String currency = data.getCurrency();

      if (currency == null) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Amount and currency are required.");
        return ResponseEntity.badRequest().body(errorResponse);
      }

      if (amount <= 0) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Amount must be greater than zero.");
        return ResponseEntity.badRequest().body(errorResponse);
      }

      PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
          .setAmount(amount)
          .setCurrency(currency)
          .build();

      PaymentIntent intent = PaymentIntent.create(params);

      // Return client secret in a JSON response
      Map<String, String> responseData = new HashMap<>();
      responseData.put("clientSecret", intent.getClientSecret());
      return ResponseEntity.ok(responseData);

    } catch (StripeException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", "Failed to create payment intent.");
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }
}
