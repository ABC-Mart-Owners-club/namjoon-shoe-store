package com.abcmart.shoestore.inventory.repository;

import com.abcmart.shoestore.inventory.domain.Inventory;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository {

    Optional<Inventory> findByShoeCode(Long shoeCode);

}
