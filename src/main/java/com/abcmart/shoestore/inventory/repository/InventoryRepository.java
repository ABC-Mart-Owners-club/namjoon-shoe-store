package com.abcmart.shoestore.inventory.repository;

import com.abcmart.shoestore.inventory.domain.Inventory;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository {

    Optional<Inventory> findByShoeCode(Long shoeCode);

    List<Inventory> findAllByShoeCodes(List<Long> shoeCodes);

    Inventory save(Inventory inventory);

    List<Inventory> saveAll(List<Inventory> inventories);
}
