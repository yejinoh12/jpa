package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    //회원 등록 API

    //v1 : 엔티티를 파라미터로 받지 않기
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMember(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //v2 : 입력 DTO 사용
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMember(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();
        member.setName(request.getName());
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //회원 수정 API

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse editMember(@PathVariable Long id,
                                           @RequestBody @Valid UpdateMemberRequest request){
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(id, findMember.getName());
    }

    //회원 조회 API

    //v1: 엔티티 노출, 회원정보임에도 주문 정보를 넘김, 불필요한 정보 노출
    //엔티티에 프레젠테이션 계층의 역할 추가
    //엔티티 변경으로 API 스펙 변경
    @GetMapping("/api/v1/members")
    public List<Member> members1(){
        return memberService.findMembers();
    }

    //v2
    @GetMapping("/api/v2/members")
    public Result members2(){
        List<Member> members = memberService.findMembers();
        List<MemberDto> collect = members.stream().map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result(collect);
    }


    @Data
    @AllArgsConstructor
    static class CreateMemberResponse{
        private Long id;
    }

    @Data
    @AllArgsConstructor
    static class CreateMemberRequest{
        private String name;
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }
    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }
}
