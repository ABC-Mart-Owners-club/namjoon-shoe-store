package com.abcmart.shoestore.inventory.repository;

import com.abcmart.shoestore.inventory.domain.Inventory;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository {

    List<Inventory> findByShoeCode(Long shoeCode);

    Optional<Inventory> findByShoeCodeAndStockedDate(Long shoeCode, LocalDate stockedDate);

    Map<Long, List<Inventory>> findAllByShoeCodes(List<Long> shoeCodes);

    Inventory save(Inventory inventory);

    List<Inventory> saveAll(List<Inventory> inventories);
}
