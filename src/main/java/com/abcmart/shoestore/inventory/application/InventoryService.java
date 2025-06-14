package com.abcmart.shoestore.inventory.application;

import com.abcmart.shoestore.inventory.domain.Inventory;
import com.abcmart.shoestore.inventory.dto.AvailableDeductionStock;
import com.abcmart.shoestore.inventory.repository.InventoryRepository;
import com.abcmart.shoestore.order.application.request.CreateOrderRequest.CreateOrderDetailRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<Inventory> findInventoriesByShoeCode(Long shoeCode){

        return inventoryRepository.findByShoeCode(shoeCode);
    }

    @Transactional(readOnly = true)
    public List<Inventory> findInventoriesByRequestOrElseThrow(Long shoeCode, Long requestedQuantity) {

        List<Inventory> inventories = inventoryRepository.findByShoeCode(shoeCode);
        return validateAvailableUseStock(inventories, requestedQuantity);
    }

    @Transactional
    public Map<Long, List<AvailableDeductionStock>> deductStockAndFindAvailableOrElseThrow(
        List<CreateOrderDetailRequest> orderDetails
    ) {

        List<Long> shoeCodes = orderDetails.stream().map(CreateOrderDetailRequest::getShoeCode).toList();
        Map<Long, List<Inventory>> inventoriesMap = inventoryRepository.findAllByShoeCodes(shoeCodes);

        Map<Long, List<AvailableDeductionStock>> deductionResultMap = new HashMap<>();
        for (CreateOrderDetailRequest orderDetail : orderDetails) {

            List<Inventory> inventories = inventoriesMap.get(orderDetail.getShoeCode());
            if (Objects.isNull(inventories)) {
                throw Inventory.insufficientStockException();
            }

            validateAvailableUseStock(inventories, orderDetail.getCount());
            deductionResultMap.put(
                orderDetail.getShoeCode(),
                deductStockAndSave(inventories, orderDetail.getCount())
            );
        }

        return deductionResultMap;
    }

    private static List<Inventory> validateAvailableUseStock(List<Inventory> inventories, Long requestedQuantity) {

        Long totalStockCount = inventories.stream().map(Inventory::getStock).reduce(0L, Long::sum);
        if (requestedQuantity > totalStockCount) throw Inventory.insufficientStockException();
        return inventories;
    }

    public List<AvailableDeductionStock> deductStockAndSave(List<Inventory> inventories, Long requestedQuantity) {

        List<Inventory> usedInventories = new ArrayList<>();
        List<AvailableDeductionStock> result = new ArrayList<>();

        for (Inventory inventory : inventories) {

            long deducted = inventory.deductStock(requestedQuantity);
            if (deducted > 0) {
                usedInventories.add(inventory);
                result.add(AvailableDeductionStock.of(inventory, deducted));
                requestedQuantity -= deducted;
            }
        }

        if (requestedQuantity > 0) throw Inventory.insufficientStockException();

        inventoryRepository.saveAll(usedInventories);
        return result;
    }
}
