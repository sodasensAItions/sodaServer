package com.sodasensaitions.backend.payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/payments")
public class PaymentController {

  // Inject the Stripe API key from application.properties
  @Value("${stripe.apiKey}")
  private String stripeApiKey;

  // Initialize Stripe API key once when the controller is created
  @PostConstruct
  public void init() {
    Stripe.apiKey = stripeApiKey;
  }

  @PostMapping("/create-payment-intent")
  public ResponseEntity<String> createPaymentIntent(@RequestBody PaymentPOJO data) {

    try {
      // Retrieve amount and currency from the request body
      long amount = data.getAmount();
      String currency = data.getCurrency();

      if (currency == null) {
        return ResponseEntity.badRequest().body("Amount and currency are required.");
      }

      // Ensure amount is a positive value
      if (amount <= 0) {
        return ResponseEntity.badRequest().body("Amount must be greater than zero.");
      }

      // Create PaymentIntent parameters
      PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
          .setAmount(amount)
          .setCurrency(currency)
          .build();

      // Create a PaymentIntent with Stripe
      PaymentIntent intent = PaymentIntent.create(params);

      // Return the client secret to the client
      return ResponseEntity.ok(intent.getClientSecret());

    } catch (StripeException e) {
      // Handle Stripe API errors
      return ResponseEntity.badRequest().build();
    }
  }
}