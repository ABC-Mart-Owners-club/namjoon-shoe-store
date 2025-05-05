package com.abcmart.shoestore.application;

import static com.abcmart.shoestore.testutil.PaymentTestDomainFactory.createCash;
import static com.abcmart.shoestore.testutil.PaymentTestDomainFactory.createCreditCard;
import static com.abcmart.shoestore.testutil.OrderTestDomainFactory.createOrderBy;
import static com.abcmart.shoestore.testutil.OrderTestDomainFactory.createOrderDetail;
import static com.abcmart.shoestore.testutil.ShoeTestDomainFactory.createShoeByShoeCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.abcmart.shoestore.order.application.request.CreateOrderRequest;
import com.abcmart.shoestore.order.application.request.CreateOrderRequest.CreateOrderDetailRequest;
import com.abcmart.shoestore.order.application.request.CreateOrderRequest.CreatePaymentRequest;
import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.OrderDetail;
import com.abcmart.shoestore.payment.domain.Payment;
import com.abcmart.shoestore.shoe.domain.Shoe;
import com.abcmart.shoestore.order.application.OrderService;
import com.abcmart.shoestore.order.dto.OrderDetailDto;
import com.abcmart.shoestore.order.dto.OrderDto;
import com.abcmart.shoestore.payment.dto.PaymentDto;
import com.abcmart.shoestore.order.repository.OrderRepository;
import com.abcmart.shoestore.shoe.repository.ShoeRepository;
import com.abcmart.shoestore.payment.domain.CreditCardType;
import com.abcmart.shoestore.order.domain.OrderStatus;
import com.abcmart.shoestore.payment.domain.PaymentType;
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

    static final Long orderNo1 = 1L;
    static final Long orderNo2 = 2L;
    static final Long orderNo3 = 3L;

    static final Long shoeCode1 = 1L;
    static final Long shoeCode2 = 2L;
    static final Long shoeCode3 = 3L;

    @Test
    @DisplayName("정상적으로 유효한 주문이 생성되고 현금 결제가 되었는지 확인한다.")
    void createOrderWithOnlyCash() {

        // given
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
        List<CreatePaymentRequest> paymentRequests = List.of(
            new CreatePaymentRequest(PaymentType.CASH, null, totalPrice));
        CreateOrderRequest request = new CreateOrderRequest(orderDetailRequests, paymentRequests);

        OrderDto result = orderService.createOrder(request);


        // then
        List<PaymentType> paymentTypeList = result.getPayments().stream()
            .map(PaymentDto::getPaymentType)
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
        List<CreatePaymentRequest> paymentRequests = List.of(
            new CreatePaymentRequest(PaymentType.CREDIT_CARD, CreditCardType.HYUNDAI, totalPrice));
        CreateOrderRequest request = new CreateOrderRequest(orderDetailRequests, paymentRequests);

        OrderDto result = orderService.createOrder(request);


        // then
        List<PaymentType> paymentTypeList = result.getPayments().stream()
            .map(PaymentDto::getPaymentType)
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
    @DisplayName("정상적으로 유효한 주문이 생성되고 분할결제(현금+카드) 결제가 되었는지 확인한다.")
    void createOrderWithCashAndCreditCard() {

        // given
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
        BigDecimal firstPaymentPrice = totalPrice.divide(BigDecimal.TWO, BigDecimal.ROUND_HALF_UP);
        BigDecimal secondPaymentPrice = totalPrice.subtract(firstPaymentPrice);
        List<CreatePaymentRequest> paymentRequests = List.of(
            new CreatePaymentRequest(PaymentType.CASH, null, firstPaymentPrice),
            new CreatePaymentRequest(PaymentType.CREDIT_CARD, CreditCardType.HYUNDAI, secondPaymentPrice)
        );
        CreateOrderRequest request = new CreateOrderRequest(orderDetailRequests, paymentRequests);

        OrderDto result = orderService.createOrder(request);


        // then
        List<PaymentType> paymentTypeList = result.getPayments().stream()
            .map(PaymentDto::getPaymentType)
            .toList();
        assertThat(paymentTypeList).contains(PaymentType.CASH, PaymentType.CREDIT_CARD);
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
        OrderDetail orderDetail = createOrderDetail(shoeCode1, 5L);
        List<Payment> payments = List.of(createCash(), createCreditCard());
        Order order = createOrderBy(orderNo1, List.of(orderDetail), payments);

        given(orderRepository.findByOrderNo(any())).willReturn(order);
        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> invocation.getArgument(0));


        // when
        OrderDto orderDto = orderService.cancelOrder(orderNo1);


        // then
        assertThat(orderDto.getStatus()).isEqualTo(OrderStatus.CANCEL);
    }

    @Test
    @DisplayName("부분 취소가 가능하고, 주문이 정상으로 남아있는지 확인한다.")
    void partialCancel() {

        // given
        Shoe shoe1 = createShoeByShoeCode(shoeCode1);

        OrderDetail orderDetail = createOrderDetail(shoeCode1, 5L);
        List<Payment> payments = List.of(createCash(), createCreditCard());
        Order order = createOrderBy(orderNo1, List.of(orderDetail), payments);


        given(shoeRepository.findByShoeCode(anyLong())).willReturn(shoe1);
        given(orderRepository.findByOrderNo(any())).willReturn(order);
        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> invocation.getArgument(0));


        // when
        long removeCount = 1L;
        OrderDto orderDto = orderService.partialCancel(orderNo1, shoeCode1, removeCount);


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
        Shoe shoe1 = createShoeByShoeCode(shoeCode1);

        OrderDetail orderDetail = createOrderDetail(shoeCode1, 5L);
        List<Payment> payments = List.of(createCash(), createCreditCard());
        Order order = createOrderBy(orderNo1, List.of(orderDetail), payments);

        given(shoeRepository.findByShoeCode(anyLong())).willReturn(shoe1);
        given(orderRepository.findByOrderNo(any())).willReturn(order);
        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> invocation.getArgument(0));


        // when
        long removeCount = 5L;
        OrderDto orderDto = orderService.partialCancel(orderNo1, shoeCode1, removeCount);


        // then
        assertThat(orderDto.getStatus()).isEqualTo(OrderStatus.CANCEL);

        OrderDetailDto orderDetailDto = orderDto.getDetails().stream()
            .filter(detail -> detail.getShoeCode().equals(shoeCode1)).findFirst().get();
        assertThat(orderDetailDto.getCount()).isEqualTo(orderDetail.getCount() - removeCount);
        assertThat(orderDetailDto.getOrderStatus()).isEqualTo(OrderStatus.CANCEL);
    }
}