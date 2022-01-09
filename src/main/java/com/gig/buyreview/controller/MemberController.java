package com.gig.buyreview.controller;

import com.gig.buyreview.domain.Member;
import com.gig.buyreview.dto.UserDto;
import com.gig.buyreview.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author : Jake
 * @date : 2022-01-09
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("signup")
    public ResponseEntity<Member> signup(
            @Valid @RequestBody UserDto userDto
            ) {
        return ResponseEntity.ok(memberService.signUp(userDto));
    }

    @CrossOrigin("*")
    @GetMapping("user")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Member> getMyUserInfo() {
        return ResponseEntity.ok(memberService.getMyMemberWithAuthorities().get());
    }

    // Admin 권한만 호출 가능
    @GetMapping("user/{username}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Member> getUserInfo(@PathVariable String username) {
        return ResponseEntity.ok(memberService.getMemberWithAuthorities(username).get());
    }
}
