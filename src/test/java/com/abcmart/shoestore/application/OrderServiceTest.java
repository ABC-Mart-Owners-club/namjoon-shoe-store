package com.abcmart.shoestore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.abcmart.shoestore.application.request.CreateOrderRequest;
import com.abcmart.shoestore.application.request.CreateOrderRequest.CreateOrderDetailRequest;
import com.abcmart.shoestore.application.response.ShoeSaleCountResponse;
import com.abcmart.shoestore.application.response.ShoeSaleCountResponse.SoldShoe;
import com.abcmart.shoestore.dto.OrderDto;
import com.abcmart.shoestore.dto.OrderDetailDto;
import com.abcmart.shoestore.domain.OrderDetail;
import com.abcmart.shoestore.domain.Order;
import com.abcmart.shoestore.domain.Shoe;
import com.abcmart.shoestore.repository.OrderRepository;
import com.abcmart.shoestore.repository.ShoeRepository;
import com.abcmart.shoestore.tool.OrderStatus;
import com.abcmart.shoestore.tool.PaymentType;
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
class OrderServiceTest {

    @Mock
    private ShoeRepository shoeRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
        .build();

    @Test
    @DisplayName("정상적으로 유효한 주문이 생성되었는지 확인한다.")
    void createOrder() {

        // given
        Long shoeCode1 = 1L;
        Long shoeCode2 = 2L;
        Long shoeCode3 = 3L;

        Shoe shoe1 = fixtureMonkey.giveMeBuilder(Shoe.class)
            .set("shoeCode", shoeCode1)
            .setNotNull("price")
            .sample();
        Shoe shoe2 = fixtureMonkey.giveMeBuilder(Shoe.class)
            .set("shoeCode", shoeCode2)
            .setNotNull("price")
            .sample();
        Shoe shoe3 = fixtureMonkey.giveMeBuilder(Shoe.class)
            .set("shoeCode", shoeCode3)
            .setNotNull("price")
            .sample();
        List<Shoe> shoeEntities = List.of(shoe1, shoe2, shoe3);

        given(shoeRepository.findAllByShoeCodes(anyList())).willReturn(shoeEntities);
        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> invocation.getArgument(0));


        // when
        Long countOfShoeCode1 = 2L;
        Long countOfShoeCode2 = 3L;
        Long countOfShoeCode3 = 5L;

        List<CreateOrderDetailRequest> orderDetailRequests = List.of(
            new CreateOrderDetailRequest(shoeCode1, countOfShoeCode1),
            new CreateOrderDetailRequest(shoeCode2, countOfShoeCode2),
            new CreateOrderDetailRequest(shoeCode3, countOfShoeCode3)
        );
        CreateOrderRequest request = new CreateOrderRequest(orderDetailRequests, PaymentType.CASH);

        OrderDto result = orderService.createOrder(request);


        // then
        assertThat(result.getOrderPayment().getType()).isEqualTo(PaymentType.CASH);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.NORMAL);
        assertThat(result.getDetails()).isNotEmpty();
        assertThat(result.getDetails()).hasSize(3);

        List<Long> shoeCodes = shoeEntities.stream().map(Shoe::getShoeCode).toList();
        assertThat(
            result.getDetails().stream().map(OrderDetailDto::getShoeCode).toList()
        ).containsExactlyInAnyOrderElementsOf(shoeCodes);

        OrderDetailDto orderDetailDto1 = result.getDetails().stream()
            .filter(orderDetailDto -> orderDetailDto.getShoeCode().equals(shoeCode1)).findFirst().get();
        assertThat(
            orderDetailDto1.getShoeCode().equals(shoeCode1)
                && orderDetailDto1.getCount().equals(countOfShoeCode1)
        ).isTrue();

        OrderDetailDto orderDetailDto2 = result.getDetails().stream()
            .filter(orderDetailDto -> orderDetailDto.getShoeCode().equals(shoeCode2)).findFirst().get();
        assertThat(
            orderDetailDto2.getShoeCode().equals(shoeCode2)
                && orderDetailDto2.getCount().equals(countOfShoeCode2)
        ).isTrue();

        OrderDetailDto orderDetailDto3 = result.getDetails().stream()
            .filter(orderDetailDto -> orderDetailDto.getShoeCode().equals(shoeCode3)).findFirst().get();
        assertThat(
            orderDetailDto3.getShoeCode().equals(shoeCode3)
                && orderDetailDto3.getCount().equals(countOfShoeCode3)
        ).isTrue();
    }

    @Test
    @DisplayName("주문 취소가 가능한지 확인한다.")
    void cancelOrder() {

        // given
        Order order = fixtureMonkey.giveMeBuilder(Order.class)
            .set("orderNo", 1L)
            .set("status", OrderStatus.NORMAL)
            .sample();

        given(orderRepository.findByOrderNo(any())).willReturn(order);
        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> invocation.getArgument(0));


        // when
        OrderDto orderDto = orderService.cancelOrder(1L);


        // then
        assertThat(orderDto.getStatus()).isEqualTo(OrderStatus.CANCEL);
    }

    @Test
    @DisplayName("부분 취소가 가능하고, 주문이 정상으로 남아있는지 확인한다.")
    void partialCancel() {

        // given
        Long shoeCode1 = 1L;
        OrderDetail orderDetail = fixtureMonkey.giveMeBuilder(OrderDetail.class)
            .set("orderStatus", OrderStatus.NORMAL)
            .set("shoeCode", shoeCode1)
            .set("count", 5L)
            .sample();

        Long orderNo = 1L;
        Order order = fixtureMonkey.giveMeBuilder(Order.class)
            .set("orderNo", orderNo)
            .set("status", OrderStatus.NORMAL)
            .set("details", List.of(orderDetail))
            .sample();

        Shoe shoe1 = fixtureMonkey.giveMeBuilder(Shoe.class)
            .set("shoeCode", shoeCode1)
            .setNotNull("price")
            .sample();

        given(shoeRepository.findByShoeCode(anyLong())).willReturn(shoe1);
        given(orderRepository.findByOrderNo(any())).willReturn(order);
        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> invocation.getArgument(0));


        // when
        long removeCount = 1L;
        OrderDto orderDto = orderService.partialCancel(orderNo, shoeCode1, removeCount);


        // then
        assertThat(orderDto.getStatus()).isEqualTo(OrderStatus.NORMAL);

        OrderDetailDto orderDetailDto = orderDto.getDetails().stream()
            .filter(detail -> detail.getShoeCode().equals(shoeCode1)).findFirst().get();
        assertThat(orderDetailDto.getCount()).isEqualTo(orderDetail.getCount() - removeCount);
        assertThat(orderDetailDto.getOrderStatus()).isEqualTo(OrderStatus.NORMAL);
    }

    @Test
    @DisplayName("잔여 항목을 모두 부분 취소했을 때, 주문이 전체 취소 되는지 확인한다.")
    void totalCancelWithPartialCancel() {

        // given
        Long shoeCode1 = 1L;
        OrderDetail orderDetail = fixtureMonkey.giveMeBuilder(OrderDetail.class)
            .set("orderStatus", OrderStatus.NORMAL)
            .set("shoeCode", shoeCode1)
            .set("count", 5L)
            .sample();

        Long orderNo = 1L;
        Order order = fixtureMonkey.giveMeBuilder(Order.class)
            .set("orderNo", orderNo)
            .set("status", OrderStatus.NORMAL)
            .set("details", List.of(orderDetail))
            .sample();

        Shoe shoe1 = fixtureMonkey.giveMeBuilder(Shoe.class)
            .set("shoeCode", shoeCode1)
            .setNotNull("price")
            .sample();

        given(shoeRepository.findByShoeCode(anyLong())).willReturn(shoe1);
        given(orderRepository.findByOrderNo(any())).willReturn(order);
        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> invocation.getArgument(0));


        // when
        long removeCount = 5L;
        OrderDto orderDto = orderService.partialCancel(orderNo, shoeCode1, removeCount);


        // then
        assertThat(orderDto.getStatus()).isEqualTo(OrderStatus.CANCEL);

        OrderDetailDto orderDetailDto = orderDto.getDetails().stream()
            .filter(detail -> detail.getShoeCode().equals(shoeCode1)).findFirst().get();
        assertThat(orderDetailDto.getCount()).isEqualTo(orderDetail.getCount() - removeCount);
        assertThat(orderDetailDto.getOrderStatus()).isEqualTo(OrderStatus.CANCEL);
    }

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
        ShoeSaleCountResponse shoeSaleCountResponse = orderService.getShoeSaleCount();


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