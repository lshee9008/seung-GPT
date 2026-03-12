package com.example.demo.controller;

import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@Controller
public class LoginController {

    private final MemberRepository memberRepository;

    public LoginController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 1. 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 2. 로그인 처리 (디버깅 로그 추가)
    @PostMapping("/login")
    public String doLogin(@RequestParam("username") String username, 
                          @RequestParam("password") String password,
                          HttpSession session) {
        
        System.out.println("=========================================");
        System.out.println("[로그인 시도] ID: " + username + ", PW: " + password);

        Member member = memberRepository.findByUsername(username).orElse(null);

        if (member == null) {
            System.out.println("[실패] DB에서 해당 아이디를 찾을 수 없습니다.");
            System.out.println("팁: 서버를 재시작하면 회원정보가 사라집니다. 다시 가입해주세요.");
            return "redirect:/login?error=notfound";
        }

        System.out.println("[DB 확인] 찾은 회원 비밀번호: " + member.getPassword());

        if (member.getPassword().equals(password)) { 
            System.out.println("[성공] 비밀번호 일치! 로그인 승인.");
            session.setAttribute("memberId", member.getId());
            session.setAttribute("nickname", member.getNickname());
            System.out.println("=========================================");
            return "redirect:/";
        } else {
            System.out.println("[실패] 비밀번호가 틀립니다.");
            System.out.println("=========================================");
            return "redirect:/login?error=pw";
        }
    }

    // 3. 회원가입 페이지
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    // 4. 회원가입 처리
    @PostMapping("/signup")
    public String doSignup(@RequestParam("username") String username,
                           @RequestParam("nickname") String nickname,
                           @RequestParam("password") String password) {
        
        System.out.println("=========================================");
        System.out.println("[회원가입 시도] ID: " + username);

        if (memberRepository.findByUsername(username).isPresent()) {
            System.out.println("[실패] 이미 존재하는 아이디입니다.");
            return "redirect:/signup?error=exists";
        }

        Member newMember = new Member();
        newMember.setUsername(username);
        newMember.setPassword(password);
        newMember.setNickname(nickname);
        newMember.setCreatedAt(LocalDateTime.now());

        memberRepository.save(newMember);
        System.out.println("[성공] 회원가입 완료! DB에 저장됨.");
        System.out.println("=========================================");

        return "redirect:/login"; 
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}