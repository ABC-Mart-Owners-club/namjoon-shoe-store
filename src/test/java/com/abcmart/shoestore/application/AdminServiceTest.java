package com.abcmart.shoestore.application;

import static com.abcmart.shoestore.testutil.InventoryTestDomainFactory.createInventory;
import static com.abcmart.shoestore.testutil.OrderTestDomainFactory.createOrderDetail;
import static com.abcmart.shoestore.testutil.PaymentTestDomainFactory.createCreditCard;
import static com.abcmart.shoestore.testutil.ShoeTestDomainFactory.createShoeBy;
import static com.abcmart.shoestore.testutil.ShoeTestDomainFactory.createShoeByShoeCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import com.abcmart.shoestore.admin.application.AdminService;
import com.abcmart.shoestore.admin.application.response.ShoeSaleAmountResponse;
import com.abcmart.shoestore.admin.application.response.ShoeSaleAmountResponse.CreditCardSaleAmountResponse;
import com.abcmart.shoestore.admin.application.response.ShoeSaleCountResponse;
import com.abcmart.shoestore.admin.application.response.ShoeSaleCountResponse.SoldShoe;
import com.abcmart.shoestore.admin.application.response.ShoeStockResponse;
import com.abcmart.shoestore.inventory.domain.Inventory;
import com.abcmart.shoestore.inventory.repository.InventoryRepository;
import com.abcmart.shoestore.order.domain.OrderDetail;
import com.abcmart.shoestore.order.repository.OrderRepository;
import com.abcmart.shoestore.payment.domain.CardPayment;
import com.abcmart.shoestore.payment.domain.CreditCardType;
import com.abcmart.shoestore.payment.repository.PaymentRepository;
import com.abcmart.shoestore.shoe.domain.Shoe;
import com.abcmart.shoestore.shoe.repository.ShoeRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ShoeRepository shoeRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AdminService adminService;

    static final Long shoeCode1 = 1L;
    static final Long shoeCode2 = 2L;
    static final Long shoeCode3 = 3L;

    @Test
    @DisplayName("신발 상품 별 판매 수량이 정상적으로 조회되는지 확인한다.")
    void getShoeSaleCount() {

        // given
        Shoe shoe1 = createShoeBy(shoeCode1, BigDecimal.valueOf(50_000));
        Shoe shoe2 = createShoeBy(shoeCode2, BigDecimal.valueOf(100_000));
        Shoe shoe3 = createShoeBy(shoeCode3, BigDecimal.valueOf(70_000));
        List<Shoe> shoeEntities = List.of(shoe1, shoe2, shoe3);

        given(shoeRepository.findAllByShoeCodes(anyList())).willReturn(shoeEntities);

        long shoe1SaleCount = 2L;
        long shoe2SaleCount = 3L;
        long shoe3SaleCount = 5L;
        OrderDetail orderDetail1 = createOrderDetail(shoe1, shoe1SaleCount);
        OrderDetail orderDetail2 = createOrderDetail(shoe2, shoe2SaleCount);
        OrderDetail orderDetail3 = createOrderDetail(shoe3, shoe3SaleCount);
        List<OrderDetail> orderDetailList = List.of(
            orderDetail1, orderDetail2, orderDetail3
        );

        given(orderRepository.findAllNormalStatusOrderDetails()).willReturn(orderDetailList);

        // when
        ShoeSaleCountResponse shoeSaleCountResponse = adminService.getShoeSaleCount();

        // then
        assertThat(shoeSaleCountResponse.getTotalElements()).isEqualTo(shoeEntities.size());

        SoldShoe soldShoe1 = shoeSaleCountResponse.getSoldShoes().stream()
            .filter(soldShoe -> soldShoe.getShoeCode().equals(shoeCode1)).findFirst().get();
        assertThat(
            soldShoe1.getSaleCount().equals(orderDetail1.getCount())
                && soldShoe1.getTotalPrice()
                .equals(shoe1.getPrice().multiply(BigDecimal.valueOf(shoe1SaleCount)))
        ).isTrue();

        SoldShoe soldShoe2 = shoeSaleCountResponse.getSoldShoes().stream()
            .filter(soldShoe -> soldShoe.getShoeCode().equals(shoeCode2)).findFirst().get();
        assertThat(
            soldShoe2.getSaleCount().equals(orderDetail2.getCount())
                && soldShoe2.getTotalPrice()
                .equals(shoe2.getPrice().multiply(BigDecimal.valueOf(shoe2SaleCount)))
        ).isTrue();

        SoldShoe soldShoe3 = shoeSaleCountResponse.getSoldShoes().stream()
            .filter(soldShoe -> soldShoe.getShoeCode().equals(shoeCode3)).findFirst().get();
        assertThat(
            soldShoe3.getSaleCount().equals(orderDetail3.getCount())
                && soldShoe3.getTotalPrice()
                .equals(shoe3.getPrice().multiply(BigDecimal.valueOf(shoe3SaleCount)))
        ).isTrue();
    }


    @Test
    @DisplayName("카드사 별 판매 금액을 조회한다.")
    void getShoeSaleAmountByCreditCardType() {

        // given
        CardPayment cardPayment1 = (CardPayment) createCreditCard();
        CardPayment cardPayment2 = (CardPayment) createCreditCard();
        CardPayment cardPayment3 = (CardPayment) createCreditCard();
        List<CardPayment> cardPaymentList = List.of(cardPayment1, cardPayment2, cardPayment3);
        given(paymentRepository.findAllCreditCardPayments()).willReturn(cardPaymentList);

        // when
        ShoeSaleAmountResponse result = adminService.getShoeSaleAmountByCreditCardType();

        // then
        List<CreditCardType> requestCreditCardTypeList = cardPaymentList.stream()
            .map(CardPayment::getCreditCardType)
            .distinct()
            .toList();
        List<CreditCardType> creditCardTypeResultList = result.creditCardSaleAmounts().stream()
            .map(CreditCardSaleAmountResponse::creditCardType)
            .toList();
        assertThat(creditCardTypeResultList).contains(
            requestCreditCardTypeList.toArray(new CreditCardType[0]));
    }


    @Test
    @DisplayName("재고에서 신발 상품을 조회한다.")
    void findShoeInventoryByShoeCode() {

        // given
        Shoe shoe1 = createShoeByShoeCode(shoeCode1);
        Long stock = 10L;
        Inventory shoe1Inventory = createInventory(shoeCode1, stock);
        given(shoeRepository.findByShoeCode(any())).willReturn(Optional.of(shoe1));
        given(inventoryRepository.findByShoeCode(any())).willReturn(Optional.of(shoe1Inventory));

        // when
        ShoeStockResponse stockResponse = adminService.findInventoryByShoeCode(shoeCode1);

        // then
        assertThat(stockResponse.shoeCode()).isEqualTo(shoe1.getShoeCode());
        assertThat(stockResponse.shoeName()).isEqualTo(shoe1.getShoeName());
        assertThat(stockResponse.color()).isEqualTo(shoe1.getColor());
        assertThat(stockResponse.size()).isEqualTo(shoe1.getSize());
        assertThat(stockResponse.price()).isEqualTo(shoe1.getPrice());
        assertThat(stockResponse.stock()).isEqualTo(stock);
    }
}