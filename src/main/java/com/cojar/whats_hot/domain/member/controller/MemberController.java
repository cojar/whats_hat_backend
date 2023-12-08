package com.cojar.whats_hot.domain.member.controller;

import com.cojar.whats_hot.domain.member.api_response.MemberApiResponse;
import com.cojar.whats_hot.domain.member.dto.MemberDto;
import com.cojar.whats_hot.domain.member.entity.Member;
import com.cojar.whats_hot.domain.member.entity.MemberRole;
import com.cojar.whats_hot.domain.member.request.MemberRequest;
import com.cojar.whats_hot.domain.member.response.MemberResponse;
import com.cojar.whats_hot.domain.member.service.MemberService;
import com.cojar.whats_hot.global.response.ResData;
import com.cojar.whats_hot.global.util.AppConfig;
import com.cojar.whats_hot.domain.index.controller.IndexController;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Tag(name = "Member", description = "API 인덱스")
@RequestMapping(value = "/api/members", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;

    @MemberApiResponse.Signup
    @PostMapping(value = "/signup")
    public ResponseEntity signup(@Valid @RequestBody MemberRequest.Signup signup, Errors errors) {

        Member member = this.memberService.signup("aa", "aaa", "aaaa", List.of(MemberRole.USER));

        ResData resData = ResData.of(
                HttpStatus.CREATED,
                "S-01-01",
                "회원가입을 완료했습니다",
                "",
                linkTo(this.getClass()).slash("login")
        );
        resData.add(Link.of(AppConfig.getBaseURL() + "/swagger-ui/index.html#/Member/signup").withRel("profile"));
        return ResponseEntity.created(linkTo(this.getClass()).slash("me").toUri())
                .body(resData);
    }

    @MemberApiResponse.Login
    @PostMapping(value = "/login")
    public ResponseEntity login(@Valid @RequestBody MemberRequest.Login loginReq, Errors errors) {

        ResData resData = this.memberService.loginValidate(loginReq, errors);
        if (resData != null) return ResponseEntity.badRequest().body(resData);

        String accessToken = this.memberService.getAccessToken(loginReq);

        resData = ResData.of(
                HttpStatus.OK,
                "S-01-02",
                "액세스 토큰이 생성되었습니다",
                new MemberResponse.Login(accessToken),
                linkTo(IndexController.class).slash("/api/index")
        );
        resData.add(Link.of(AppConfig.getBaseURL() + "/swagger-ui/index.html#/Member/login").withRel("profile"));
        return ResponseEntity.ok()
                .body(resData);
    }

    @MemberApiResponse.Logout
    @PostMapping(value = "/logout", consumes = MediaType.ALL_VALUE)
    public ResponseEntity logout(@AuthenticationPrincipal User user) {

        Member member = this.memberService.getUserByUsername(user.getUsername());
        this.memberService.logout(member);

        ResData resData = ResData.of(
                HttpStatus.OK,
                "S-01-03",
                "로그아웃이 완료되었습니다",
                linkTo(IndexController.class).slash("/api/index")
        );
        resData.add(Link.of(AppConfig.getBaseURL() + "/swagger-ui/index.html#/Member/logout").withRel("profile"));
        return ResponseEntity.ok()
                .body(resData);
    }

    @MemberApiResponse.Me
    @GetMapping(value = "/me", consumes = MediaType.ALL_VALUE)
    public ResponseEntity me(@AuthenticationPrincipal User user) {

        Member member = this.memberService.getUserByUsername(user.getUsername());

        ResData resData = ResData.of(
                HttpStatus.OK,
                "S-01-04",
                "로그인된 회원 정보를 반환합니다",
                new MemberResponse.Me(MemberDto.of(member)),
                linkTo(this.getClass()).slash("me")
        );
        resData.add(Link.of(AppConfig.getBaseURL() + "/swagger-ui/index.html#/Member/me").withRel("profile"));
        return ResponseEntity.ok()
                .body(resData);
    }

    @MemberApiResponse.UpdatePassword
    @PatchMapping(value = "/password")
    public ResponseEntity updatePassword(@Valid @RequestBody MemberRequest.UpdatePassword updatePassword, @AuthenticationPrincipal User user) {

        ResData resData = ResData.of(
                HttpStatus.OK,
                "S-01-05",
                "비밀번호 변경을 완료했습니다",
                linkTo(this.getClass()).slash("me")
        );
        resData.add(Link.of(AppConfig.getBaseURL() + "/swagger-ui/index.html#/Member/updatePassword").withRel("profile"));
        return ResponseEntity.ok()
                .body(resData);
    }

    @MemberApiResponse.FindUsername
    @PostMapping(value = "/username")
    public ResponseEntity findUsername(@Valid @RequestBody MemberRequest.FindUsername findUsername) {

        String username = "user";

        ResData resData = ResData.of(
                HttpStatus.OK,
                "S-01-06",
                "요청하신 아이디를 반환합니다",
                new MemberResponse.FindUsername(username),
                linkTo(this.getClass()).slash("login")
        );
        resData.add(Link.of(AppConfig.getBaseURL() + "/swagger-ui/index.html#/Member/findUsername").withRel("profile"));
        return ResponseEntity.ok()
                .body(resData);
    }

    @MemberApiResponse.FindPassword
    @PostMapping(value = "/password")
    public ResponseEntity findPassword(@Valid @RequestBody MemberRequest.FindPassword findPassword) {

        ResData resData = ResData.of(
                HttpStatus.OK,
                "S-01-07",
                "이메일로 임시비밀번호를 발송했습니다",
                linkTo(this.getClass()).slash("login")
        );
        resData.add(Link.of(AppConfig.getBaseURL() + "/swagger-ui/index.html#/Member/findPassword").withRel("profile"));
        return ResponseEntity.ok()
                .body(resData);
    }


}
