package com.sodasensaitions.backend.orders;

import com.sodasensaitions.backend.authentication.user.Account;
import com.sodasensaitions.backend.authentication.user.AccountRepository;
import com.sodasensaitions.backend.inventory.IngredientService;
import com.sodasensaitions.backend.orders.pojo.DrinkRepository;
import com.sodasensaitions.backend.orders.pojo.SodaOrder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class OrdersService {

  private final AccountRepository accountRepository;
  private final OrdersRepository ordersRepository;
  private final DrinkRepository drinkRepository;
  private final IngredientService ingredientService;

  @Transactional
  public SodaOrder createOrder(Account account, SodaOrder sodaOrder) {
    //find out whether we are out of stock
    if (!ingredientService.isInStock(sodaOrder)) {
      return null;
    }


    // Save each Drink entity
    drinkRepository.saveAll(sodaOrder.getDrinks());

    // Save the SodaOrder entity
    sodaOrder.setAccount(account);
    SodaOrder savedOrder = ordersRepository.save(sodaOrder);

    // Update the Account entity
    account.addOrder(savedOrder);
    accountRepository.save(account);

    // Update the Ingredient entity
    ingredientService.reduceAvailableStock(sodaOrder);

    return savedOrder;
  }

  public boolean completeOrder(Account account, Integer orderID) {
    // verify that the order belongs to the account
    SodaOrder sodaOrder = ordersRepository.findById(orderID).orElse(null);
    if (sodaOrder == null) {
      return false;
    }

    if (!sodaOrder.getAccount().equals(account)) {
      return false;
    }

    sodaOrder.setCompleted(true);

    ordersRepository.save(sodaOrder);
    return true;
  }

  public Optional<SodaOrder> getOrder(Integer orderID) {
    return ordersRepository.findById(orderID);
  }
}