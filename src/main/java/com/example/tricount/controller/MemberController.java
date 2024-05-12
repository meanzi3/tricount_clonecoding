package com.example.tricount.controller;

import com.example.tricount.TricountConst;
import com.example.tricount.model.Member;
import com.example.tricount.model.MemberLoginRequest;
import com.example.tricount.model.MemberSignupRequest;
import com.example.tricount.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {
  private final MemberService memberService;

  @PostMapping("signup")
  public ResponseEntity<Member> signup(@RequestBody MemberSignupRequest request){
    Member newMember = Member.builder()
            .loginId(request.getLoginId())
            .password(request.getPassword())
            .name(request.getName())
            .build();

    return new ResponseEntity<>(memberService.signup(newMember), HttpStatus.OK);
  }

  @PostMapping("login")
  public ResponseEntity<Member> login(
          @RequestBody MemberLoginRequest request, HttpServletResponse response
          ){
    Member loginMember = memberService.login(request.getLoginId(), request.getPassword());

    // 쿠키 생성, 추가
    Cookie cookie = new Cookie(TricountConst.LOGIN_MEMBER_COOKIE, String.valueOf(loginMember.getId()));
    response.addCookie(cookie);

    return new ResponseEntity<>(loginMember, HttpStatus.OK);
  }

  @PostMapping("logout")
  public ResponseEntity<Void> logout(HttpServletResponse response){
    // 쿠키 삭제
    Cookie cookie = new Cookie(TricountConst.LOGIN_MEMBER_COOKIE, null);
    cookie.setMaxAge(0);
    response.addCookie(cookie);

    return new ResponseEntity<>(HttpStatus.OK);
  }
}
