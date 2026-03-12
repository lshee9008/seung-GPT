package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class ChatController {
    
    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("nickname", session.getAttribute("nickname"));
        return "chat"; 
    }
}