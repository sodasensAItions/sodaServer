package com.sodasensaitions.backend.orders;

import com.sodasensaitions.backend.orders.pojo.SodaOrder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepository extends CrudRepository<SodaOrder, Integer> {
}
