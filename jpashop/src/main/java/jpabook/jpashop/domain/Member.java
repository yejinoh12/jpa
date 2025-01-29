package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    //연관관계의 주인이 아님
    //맴버 필드에 의해 매핑되는 값임을 지정
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>(); //필드에서 초기화

}
