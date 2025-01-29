package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    /**
     * v2 : 엔티티를 DTO 로 변환
     */

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        //order 조회(2)
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(toList());
        return result;
    }

    /**
     * 페치 조인 -> 데이터 중복 발생
     * 조인하면서 주문이 2개이지만 4개가 조회되는 문제
     * (현재는 버전 업그레이드로 distinct 적용되어 중복x)
     *
     * 일대다 페치조인 시 페이징 불가
     * 모든 데이터를 디비에서 읽어오고 메모리에서 페이징해버림
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(toList());
        return result;
    }


    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems; //추가

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //멤버 조회
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //배달 조회
            orderItems = order.getOrderItems().stream() //오더 아이템 조회
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
