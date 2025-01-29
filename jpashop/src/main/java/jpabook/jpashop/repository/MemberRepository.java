package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    //@PersistenceContext
//    @Autowired //스프링 부트 지원
//    private EntityManager em; //JPA 엔티티 매니저 주입
//
//    public MemberRepository(EntityManager em) {
//        this.em = em;
//    }

    //persist 하는 순간 영속성 컨텍스트에 객체를 올림
    //디비에 값이 들어가는 시점이 아니어도 pk 값이 생성되는 것이 항상 보장됨
    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name =:name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
