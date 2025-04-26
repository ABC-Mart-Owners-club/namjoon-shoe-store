package com.abcmart.shoestore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.abcmart.shoestore.application.request.CreateOrderRequest;
import com.abcmart.shoestore.application.request.CreateOrderRequest.CreateOrderDetailRequest;
import com.abcmart.shoestore.application.request.CreateOrderRequest.CreateOrderPaymentRequest;
import com.abcmart.shoestore.domain.Order;
import com.abcmart.shoestore.domain.OrderDetail;
import com.abcmart.shoestore.domain.OrderPayment;
import com.abcmart.shoestore.domain.Shoe;
import com.abcmart.shoestore.dto.OrderDetailDto;
import com.abcmart.shoestore.dto.OrderDto;
import com.abcmart.shoestore.dto.OrderPaymentDto;
import com.abcmart.shoestore.repository.OrderRepository;
import com.abcmart.shoestore.repository.ShoeRepository;
import com.abcmart.shoestore.tool.CreditCardType;
import com.abcmart.shoestore.tool.OrderStatus;
import com.abcmart.shoestore.tool.PaymentType;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    private static Shoe createShoeByShoeCode(Long shoeCode) {

        return fixtureMonkey.giveMeBuilder(Shoe.class)
            .set("shoeCode", shoeCode)
            .setNotNull("price")
            .sample();
    }

    @Test
    @DisplayName("정상적으로 유효한 주문이 생성되고 현금 결제가 되었는지 확인한다.")
    void createOrderWithOnlyCash() {

        // given
        Long shoeCode1 = 1L;
        Long shoeCode2 = 2L;
        Long shoeCode3 = 3L;

        Shoe shoe1 = createShoeByShoeCode(shoeCode1);
        Shoe shoe2 = createShoeByShoeCode(shoeCode2);
        Shoe shoe3 = createShoeByShoeCode(shoeCode3);
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
        BigDecimal totalPrice = shoe1.getPrice().add(shoe2.getPrice()).add(shoe3.getPrice());
        List<CreateOrderPaymentRequest> orderPaymentRequests = List.of(
            new CreateOrderPaymentRequest(PaymentType.CASH, null, totalPrice));
        CreateOrderRequest request = new CreateOrderRequest(orderDetailRequests, orderPaymentRequests);

        OrderDto result = orderService.createOrder(request);


        // then
        List<PaymentType> paymentTypeList = result.getOrderPayments().stream()
            .map(OrderPaymentDto::getPaymentType)
            .filter(PaymentType::isCash)
            .toList();
        assertThat(paymentTypeList).contains(PaymentType.CASH);
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
    @DisplayName("정상적으로 유효한 주문이 생성되고 카드 결제가 되었는지 확인한다.")
    void createOrderWithOnlyCreditCard() {

        // given
        Long shoeCode1 = 1L;
        Long shoeCode2 = 2L;
        Long shoeCode3 = 3L;

        Shoe shoe1 = createShoeByShoeCode(shoeCode1);
        Shoe shoe2 = createShoeByShoeCode(shoeCode2);
        Shoe shoe3 = createShoeByShoeCode(shoeCode3);
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
        BigDecimal totalPrice = shoe1.getPrice().add(shoe2.getPrice()).add(shoe3.getPrice());
        List<CreateOrderPaymentRequest> orderPaymentRequests = List.of(
            new CreateOrderPaymentRequest(PaymentType.CREDIT_CARD, CreditCardType.HYUNDAI, totalPrice));
        CreateOrderRequest request = new CreateOrderRequest(orderDetailRequests, orderPaymentRequests);

        OrderDto result = orderService.createOrder(request);


        // then
        List<PaymentType> paymentTypeList = result.getOrderPayments().stream()
            .map(OrderPaymentDto::getPaymentType)
            .filter(PaymentType::isCreditCard)
            .toList();
        assertThat(paymentTypeList).contains(PaymentType.CREDIT_CARD);
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

        List<OrderPayment> orderPayments = List.of(
            fixtureMonkey.giveMeBuilder(OrderPayment.class).setNotNull("id").sample(),
            fixtureMonkey.giveMeBuilder(OrderPayment.class).setNotNull("id").sample()
        );

        Long orderNo = 1L;
        Order order = fixtureMonkey.giveMeBuilder(Order.class)
            .set("orderNo", orderNo)
            .set("status", OrderStatus.NORMAL)
            .set("details", List.of(orderDetail))
            .set("orderPayments", orderPayments.stream()
                .collect(Collectors.toMap(OrderPayment::getId, Function.identity()))
            )
            .sample();

        Shoe shoe1 = createShoeByShoeCode(shoeCode1);

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

        List<OrderPayment> orderPayments = List.of(
            fixtureMonkey.giveMeBuilder(OrderPayment.class).setNotNull("id").sample(),
            fixtureMonkey.giveMeBuilder(OrderPayment.class).setNotNull("id").sample()
        );

        Long orderNo = 1L;
        Order order = fixtureMonkey.giveMeBuilder(Order.class)
            .set("orderNo", orderNo)
            .set("status", OrderStatus.NORMAL)
            .set("details", List.of(orderDetail))
            .set("orderPayments", orderPayments.stream()
                .collect(Collectors.toMap(OrderPayment::getId, Function.identity()))
            )
            .sample();

        Shoe shoe1 = createShoeByShoeCode(shoeCode1);

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
}