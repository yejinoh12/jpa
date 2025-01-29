package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import jpabook.jpashop.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

//오더 -> 맴버
//오더 -> 배송
//ManyToOne, OneToOne 성능 최적화 --> 결론 패치조인
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * v1 엔티티를 직접 노출
     * 1. 맴버와 오더의 양방향 관계로 잭슨 라이브러리 무한 루프 빠짐 -> JsonIgnore 추가
     * 2. bytebuddy 에러 발생
     * order member 와 order delivery 는 지연 로딩이다. 따라서 실제 엔티티 대신에 프록시 존재
     * jackson 라이브러리는 기본적으로 이 프록시 객체를 json 으로 어떻게 생성해야 하는지 모름 예외 발생
     */

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        return orderRepository.findAllByString(new OrderSearch()); //에러 발생
    }

    /**
     * v2: Dto 반환
     * 추가 쿼리가 나가는 문제 발생
     * 오더를 조회했는데 5번 쿼리 실행
     * 엔티티를 페치 조인(fetch join)을 사용해서 쿼리 1번에 조회
     * 페치 조인으로 order -> member , order -> delivery 는 이미 조회 된 상태 이므로 지연 로딩X
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        //order 2개
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        //N + 1 문제 발생
        //1개의 쿼리를 날렸는데
        //1 + 회원(N) + 배송(N) 쿼리 실행
        List<SimpleOrderDto> collect = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());

        return collect;
    }

    /**
     * v3: 패치 조인 사용 (Lazy 로딩을 무시하고 엔티티를 가져옴)
     * 엔티티들의 모든 필드를 조회
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> collect = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());

        return collect;
    }

    /**
     * v4 : dto 직접 조회 : 원하는 필드를 선택적으로 조회
     * 데이터 사이즈가 너무 크지 않으면 v3도 성능에 크게 영향 없음
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        List<OrderSimpleQueryDto> orders = orderSimpleQueryRepository.findOrderDtoList();
        return orders;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //Lazy 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();  //Lazy 초기화
        }
    }
}
