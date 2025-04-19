package com.abcmart.shoestore.application.response;

import com.abcmart.shoestore.dto.Shoe;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ShoeSaleCountResponse {

    private final List<SoldShoe> soldShoes;
    private final int totalElements;

    private ShoeSaleCountResponse(List<SoldShoe> soldShoes) {

        this.soldShoes = soldShoes;
        this.totalElements = soldShoes.size();
    }

    public static ShoeSaleCountResponse from(List<SoldShoe> shoes) {

        return new ShoeSaleCountResponse(shoes);
    }

    @Getter
    public static class SoldShoe {

        private Long shoeCode;
        private String shoeName;
        private String color;
        private int size;
        private BigDecimal price;
        private Long saleCount;
        private BigDecimal totalPrice;

        private SoldShoe(Long shoeCode, String shoeName, String color, int size, BigDecimal price, Long saleCount) {

            this.shoeCode = shoeCode;
            this.shoeName = shoeName;
            this.color = color;
            this.size = size;
            this.price = price;
            this.saleCount = saleCount;
            this.totalPrice = price.multiply(BigDecimal.valueOf(saleCount));
        }

        public static SoldShoe of(Shoe shoe, Long saleCount) {

            return new SoldShoe(
                shoe.getShoeCode(),
                shoe.getShoeName(),
                shoe.getColor(),
                shoe.getSize(),
                shoe.getPrice(),
                saleCount
            );
        }

        public SoldShoe updateSaleCountAndTotalPrice(Long saleCount) {

            this.saleCount += saleCount;
            this.totalPrice = this.totalPrice.add(this.price.multiply(BigDecimal.valueOf(saleCount)));
            return this;
        }
    }
}
