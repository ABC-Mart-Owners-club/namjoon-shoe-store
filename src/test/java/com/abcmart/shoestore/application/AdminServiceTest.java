package com.abcmart.shoestore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import com.abcmart.shoestore.application.response.ShoeSaleCountResponse;
import com.abcmart.shoestore.application.response.ShoeSaleCountResponse.SoldShoe;
import com.abcmart.shoestore.domain.OrderDetail;
import com.abcmart.shoestore.domain.Shoe;
import com.abcmart.shoestore.repository.OrderRepository;
import com.abcmart.shoestore.repository.ShoeRepository;
import com.abcmart.shoestore.tool.OrderStatus;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.math.BigDecimal;
import java.util.List;
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
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminService adminService;

    private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
        .build();

    @Test
    @DisplayName("신발 상품 별 판매 수량이 정상적으로 조회되는지 확인한다.")
    void getShoeSaleCount() {

        // given
        Long shoeCode1 = 1L;
        Long shoeCode2 = 2L;
        Long shoeCode3 = 3L;

        Shoe shoe1 = fixtureMonkey.giveMeBuilder(Shoe.class)
            .set("shoeCode", shoeCode1)
            .set("price", BigDecimal.valueOf(50_000))
            .sample();
        Shoe shoe2 = fixtureMonkey.giveMeBuilder(Shoe.class)
            .set("shoeCode", shoeCode2)
            .set("price", BigDecimal.valueOf(100_000))
            .sample();
        Shoe shoe3 = fixtureMonkey.giveMeBuilder(Shoe.class)
            .set("shoeCode", shoeCode3)
            .set("price", BigDecimal.valueOf(70_000))
            .sample();
        List<Shoe> shoeEntities = List.of(shoe1, shoe2, shoe3);

        given(shoeRepository.findAllByShoeCodes(anyList())).willReturn(shoeEntities);

        long shoe1SaleCount = 2L;
        OrderDetail orderDetail1 = fixtureMonkey.giveMeBuilder(OrderDetail.class)
            .set("orderStatus", OrderStatus.NORMAL)
            .set("shoeCode", shoeCode1)
            .set("count", shoe1SaleCount)
            .sample();
        long shoe2SaleCount = 3L;
        OrderDetail orderDetail2 = fixtureMonkey.giveMeBuilder(OrderDetail.class)
            .set("orderStatus", OrderStatus.NORMAL)
            .set("shoeCode", shoeCode2)
            .set("count", shoe2SaleCount)
            .sample();
        long shoe3SaleCount = 5L;
        OrderDetail orderDetail3 = fixtureMonkey.giveMeBuilder(OrderDetail.class)
            .set("orderStatus", OrderStatus.NORMAL)
            .set("shoeCode", shoeCode3)
            .set("count", shoe3SaleCount)
            .sample();
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
                && soldShoe1.getTotalPrice().equals(shoe1.getPrice().multiply(BigDecimal.valueOf(shoe1SaleCount)))
        ).isTrue();

        SoldShoe soldShoe2 = shoeSaleCountResponse.getSoldShoes().stream()
            .filter(soldShoe -> soldShoe.getShoeCode().equals(shoeCode2)).findFirst().get();
        assertThat(
            soldShoe2.getSaleCount().equals(orderDetail2.getCount())
                && soldShoe2.getTotalPrice().equals(shoe2.getPrice().multiply(BigDecimal.valueOf(shoe2SaleCount)))
        ).isTrue();

        SoldShoe soldShoe3 = shoeSaleCountResponse.getSoldShoes().stream()
            .filter(soldShoe -> soldShoe.getShoeCode().equals(shoeCode3)).findFirst().get();
        assertThat(
            soldShoe3.getSaleCount().equals(orderDetail3.getCount())
                && soldShoe3.getTotalPrice().equals(shoe3.getPrice().multiply(BigDecimal.valueOf(shoe3SaleCount)))
        ).isTrue();
    }
}