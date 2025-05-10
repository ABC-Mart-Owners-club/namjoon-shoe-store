package com.abcmart.shoestore.application;

import static com.abcmart.shoestore.testutil.InventoryTestDomainFactory.createInventory;
import static com.abcmart.shoestore.testutil.OrderTestDomainFactory.createOrderBy;
import static com.abcmart.shoestore.testutil.OrderTestDomainFactory.createOrderDetail;
import static com.abcmart.shoestore.testutil.PaymentTestDomainFactory.createCash;
import static com.abcmart.shoestore.testutil.PaymentTestDomainFactory.createCreditCard;
import static com.abcmart.shoestore.testutil.ShoeTestDomainFactory.createShoeByShoeCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import com.abcmart.shoestore.inventory.domain.Inventory;
import com.abcmart.shoestore.inventory.repository.InventoryRepository;
import com.abcmart.shoestore.order.application.OrderFacadeService;
import com.abcmart.shoestore.order.application.OrderService;
import com.abcmart.shoestore.order.application.request.CreateOrderRequest;
import com.abcmart.shoestore.order.application.request.CreateOrderRequest.CreateOrderDetailRequest;
import com.abcmart.shoestore.order.application.request.CreateOrderRequest.CreatePaymentRequest;
import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.OrderDetail;
import com.abcmart.shoestore.order.domain.OrderStatus;
import com.abcmart.shoestore.order.dto.OrderDetailDto;
import com.abcmart.shoestore.order.dto.OrderDto;
import com.abcmart.shoestore.order.repository.OrderRepository;
import com.abcmart.shoestore.payment.application.PaymentService;
import com.abcmart.shoestore.payment.domain.CreditCardType;
import com.abcmart.shoestore.payment.domain.Payment;
import com.abcmart.shoestore.payment.domain.PaymentType;
import com.abcmart.shoestore.payment.dto.PaymentDto;
import com.abcmart.shoestore.payment.repository.PaymentRepository;
import com.abcmart.shoestore.shoe.domain.Shoe;
import com.abcmart.shoestore.shoe.repository.ShoeRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    private OrderService orderService;
    private PaymentService paymentService;

    @InjectMocks
    private OrderFacadeService orderFacadeService;

    static final Long orderNo1 = 1L;
    static final Long orderNo2 = 2L;
    static final Long orderNo3 = 3L;

    static final Long shoeCode1 = 1L;
    static final Long shoeCode2 = 2L;
    static final Long shoeCode3 = 3L;

    static final Shoe shoe1 = createShoeByShoeCode(shoeCode1);
    static final Shoe shoe2 = createShoeByShoeCode(shoeCode2);
    static final Shoe shoe3 = createShoeByShoeCode(shoeCode3);

    static final Long stockCount0 = 0L;
    static final Long stockCount1 = 1L;
    static final Long stockCount2 = 2L;
    static final Long stockCount3 = 3L;
    static final Long stockCount4 = 4L;
    static final Long stockCount5 = 5L;

    @BeforeEach
    void setUp() {

        orderService = new OrderService(shoeRepository, orderRepository, inventoryRepository);
        paymentService = new PaymentService(paymentRepository);
        orderFacadeService = new OrderFacadeService(orderService, paymentService);
    }

    @Test
    @DisplayName("정상적으로 유효한 주문이 생성되고 현금 결제가 되었는지 확인한다.")
    void createOrderWithOnlyCash() {

        // given
        List<Shoe> shoeEntities = List.of(shoe1, shoe2, shoe3);

        given(shoeRepository.findAllByShoeCodes(anyList())).willReturn(shoeEntities);
        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> invocation.getArgument(0));
        given(paymentRepository.saveAll(anyList()))
            .willAnswer(invocation -> invocation.getArgument(0));

        Inventory inventory1 = createInventory(shoeCode1, stockCount2);
        Inventory inventory2 = createInventory(shoeCode2, stockCount3);
        Inventory inventory3 = createInventory(shoeCode3, stockCount5);
        given(inventoryRepository.findAllByShoeCodes(anyList()))
            .willReturn(List.of(inventory1, inventory2, inventory3));

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

        OrderDto result = orderFacadeService.createOrder(request);


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
        List<Shoe> shoeEntities = List.of(shoe1, shoe2, shoe3);

        given(shoeRepository.findAllByShoeCodes(anyList())).willReturn(shoeEntities);
        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> invocation.getArgument(0));
        given(paymentRepository.saveAll(anyList()))
            .willAnswer(invocation -> invocation.getArgument(0));

        Inventory inventory1 = createInventory(shoeCode1, stockCount2);
        Inventory inventory2 = createInventory(shoeCode2, stockCount3);
        Inventory inventory3 = createInventory(shoeCode3, stockCount5);
        given(inventoryRepository.findAllByShoeCodes(anyList()))
            .willReturn(List.of(inventory1, inventory2, inventory3));

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

        OrderDto result = orderFacadeService.createOrder(request);


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
        List<Shoe> shoeEntities = List.of(shoe1, shoe2, shoe3);

        given(shoeRepository.findAllByShoeCodes(anyList())).willReturn(shoeEntities);
        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> invocation.getArgument(0));
        given(paymentRepository.saveAll(anyList()))
            .willAnswer(invocation -> invocation.getArgument(0));

        Inventory inventory1 = createInventory(shoeCode1, stockCount2);
        Inventory inventory2 = createInventory(shoeCode2, stockCount3);
        Inventory inventory3 = createInventory(shoeCode3, stockCount5);
        given(inventoryRepository.findAllByShoeCodes(anyList()))
            .willReturn(List.of(inventory1, inventory2, inventory3));

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

        OrderDto result = orderFacadeService.createOrder(request);


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
    @DisplayName("재고보다 많은 상품을 주문할 때 에러가 발생하는지 확인한다.")
    void checkExceptionWhenInsufficientStock() {

        // given
        List<Shoe> shoeEntities = List.of(shoe1, shoe2, shoe3);

        given(shoeRepository.findAllByShoeCodes(anyList())).willReturn(shoeEntities);

        Inventory inventory1 = createInventory(shoeCode1, stockCount2);
        Inventory inventory2 = createInventory(shoeCode2, stockCount3);
        Inventory inventory3 = createInventory(shoeCode3, stockCount4);
        given(inventoryRepository.findAllByShoeCodes(anyList()))
            .willReturn(List.of(inventory1, inventory2, inventory3));

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

        // then
        assertThatThrownBy(() -> orderFacadeService.createOrder(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("The stock is insufficient.");
    }

    @Test
    @DisplayName("주문 취소가 가능한지 확인한다.")
    void cancelOrder() {

        // given
        OrderDetail orderDetail = createOrderDetail(shoe1, 5L);
        List<Payment> payments = List.of(createCash(), createCreditCard());
        Order order = createOrderBy(orderNo1, List.of(orderDetail), payments);

        given(orderRepository.findByOrderNo(any())).willReturn(Optional.of(order));
        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        // when
        OrderDto orderDto = orderFacadeService.cancelOrder(orderNo1);

        // then
        assertThat(orderDto.getStatus()).isEqualTo(OrderStatus.CANCEL);
    }

    @Test
    @DisplayName("부분 취소가 가능하고, 주문이 정상으로 남아있는지 확인한다.")
    void partialCancel() {

        // given
        OrderDetail orderDetail = createOrderDetail(shoe1, 5L);
        List<Payment> payments = List.of(createCash(), createCreditCard());
        Order order = createOrderBy(orderNo1, List.of(orderDetail), payments);


        given(orderRepository.findByOrderNo(any())).willReturn(Optional.of(order));
        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> invocation.getArgument(0));


        // when
        long removeCount = 1L;
        OrderDto orderDto = orderFacadeService.partialCancel(orderNo1, shoeCode1, removeCount);


        // then
        assertThat(orderDto.getStatus()).isEqualTo(OrderStatus.NORMAL);

        OrderDetailDto orderDetailDto = orderDto.getDetails().stream()
            .filter(detail -> detail.getShoeCode().equals(shoeCode1)).findFirst().get();
        assertThat(orderDetailDto.getCount()).isEqualTo(orderDetail.getCount() - removeCount);
        assertThat(orderDetailDto.getOrderDetailStatus()).isEqualTo(OrderStatus.NORMAL);
    }

    @Test
    @DisplayName("잔여 항목을 모두 부분 취소했을 때, 주문이 전체 취소 되는지 확인한다.")
    void totalCancelWithPartialCancel() {

        // given
        OrderDetail orderDetail = createOrderDetail(shoe1, 5L);
        List<Payment> payments = List.of(createCash(), createCreditCard());
        Order order = createOrderBy(orderNo1, List.of(orderDetail), payments);

        given(orderRepository.findByOrderNo(any())).willReturn(Optional.of(order));
        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> invocation.getArgument(0));


        // when
        long removeCount = 5L;
        OrderDto orderDto = orderFacadeService.partialCancel(orderNo1, shoeCode1, removeCount);


        // then
        assertThat(orderDto.getStatus()).isEqualTo(OrderStatus.CANCEL);

        OrderDetailDto orderDetailDto = orderDto.getDetails().stream()
            .filter(detail -> detail.getShoeCode().equals(shoeCode1)).findFirst().get();
        assertThat(orderDetailDto.getCount()).isEqualTo(orderDetail.getCount() - removeCount);
        assertThat(orderDetailDto.getOrderDetailStatus()).isEqualTo(OrderStatus.CANCEL);
    }
}