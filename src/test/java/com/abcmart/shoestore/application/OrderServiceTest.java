package com.abcmart.shoestore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import com.abcmart.shoestore.application.request.CreateOrderRequest;
import com.abcmart.shoestore.application.request.CreateOrderRequest.CreateOrderDetailRequest;
import com.abcmart.shoestore.domain.OrderPayment;
import com.abcmart.shoestore.dto.Order;
import com.abcmart.shoestore.dto.OrderDetail;
import com.abcmart.shoestore.entity.OrderEntity;
import com.abcmart.shoestore.entity.ShoeEntity;
import com.abcmart.shoestore.repository.OrderRepository;
import com.abcmart.shoestore.repository.ShoeRepository;
import com.abcmart.shoestore.tool.OrderStatus;
import com.abcmart.shoestore.tool.PaymentType;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
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

        ShoeEntity shoeEntity1 = fixtureMonkey.giveMeBuilder(ShoeEntity.class)
            .set("shoeCode", shoeCode1)
            .setNotNull("price")
            .sample();
        ShoeEntity shoeEntity2 = fixtureMonkey.giveMeBuilder(ShoeEntity.class)
            .set("shoeCode", shoeCode2)
            .setNotNull("price")
            .sample();
        ShoeEntity shoeEntity3 = fixtureMonkey.giveMeBuilder(ShoeEntity.class)
            .set("shoeCode", shoeCode3)
            .setNotNull("price")
            .sample();
        List<ShoeEntity> shoeEntities = List.of(shoeEntity1, shoeEntity2, shoeEntity3);

        given(shoeRepository.findAllByShoeCodes(anyList())).willReturn(shoeEntities);
        given(orderRepository.save(any(OrderEntity.class)))
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

        Order result = orderService.createOrder(request);


        // then
        assertThat(result.getOrderPayment().getType()).isEqualTo(PaymentType.CASH);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.NORMAL);
        assertThat(result.getDetails()).isNotEmpty();
        assertThat(result.getDetails()).hasSize(3);

        List<Long> shoeCodes = shoeEntities.stream().map(ShoeEntity::getShoeCode).toList();
        assertThat(
            result.getDetails().stream().map(OrderDetail::getShoeCode).toList()
        ).containsExactlyInAnyOrderElementsOf(shoeCodes);

        OrderDetail orderDetail1 = result.getDetails().stream()
            .filter(orderDetail -> orderDetail.getShoeCode().equals(shoeCode1)).findFirst().get();
        assertThat(
            orderDetail1.getShoeCode().equals(shoeCode1)
                && orderDetail1.getCount().equals(countOfShoeCode1)
        ).isTrue();

        OrderDetail orderDetail2 = result.getDetails().stream()
            .filter(orderDetail -> orderDetail.getShoeCode().equals(shoeCode2)).findFirst().get();
        assertThat(
            orderDetail2.getShoeCode().equals(shoeCode2)
                && orderDetail2.getCount().equals(countOfShoeCode2)
        ).isTrue();

        OrderDetail orderDetail3 = result.getDetails().stream()
            .filter(orderDetail -> orderDetail.getShoeCode().equals(shoeCode3)).findFirst().get();
        assertThat(
            orderDetail3.getShoeCode().equals(shoeCode3)
                && orderDetail3.getCount().equals(countOfShoeCode3)
        ).isTrue();
    }

    @Test
    @DisplayName("주문 취소가 가능한지 확인한다.")
    void cancelOrder() {

        // given
        OrderEntity orderEntity = fixtureMonkey.giveMeBuilder(OrderEntity.class)
            .set("orderNo", 1L)
            .set("status", OrderStatus.NORMAL)
            .sample();

        given(orderRepository.findByOrderNo(any())).willReturn(orderEntity);
        given(orderRepository.save(any(OrderEntity.class)))
            .willAnswer(invocation -> invocation.getArgument(0));


        // when
        Order order = orderService.cancelOrder(1L);


        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCEL);
    }

    @Test
    void partialCancel() {
    }

    @Test
    void getShoeSaleCount() {
    }
}