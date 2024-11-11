package com.sodasensaitions.backend.payment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentPOJO {
  long amount; // Amount in cents
  String currency;
}
