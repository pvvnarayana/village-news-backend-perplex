package com.village.news.controller;

import com.village.news.entity.User;
import com.village.news.service.JwtService;
import com.village.news.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private JwtService  jwtService;

    @GetMapping("/profile")
    public User profile(@RequestHeader("Authorization") String hdr) {
        String email = jwtService.getEmailFromToken(hdr.substring(7));
        return userService.findByEmail(email).get();
    }

    @PutMapping("/profile")
    public User updateName(@RequestHeader("Authorization") String hdr,
                           @RequestParam String name) {
        String email = jwtService.getEmailFromToken(hdr.substring(7));
        User u = userService.findByEmail(email).get();
        u.setName(name);
        return userService.save(u);
    }
}
