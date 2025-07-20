//package com.village.news.controller;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.village.news.entity.Video;
//import com.village.news.service.JwtService;
//import com.village.news.service.UserService;
//import com.village.news.service.VideoService;
//
//@RestController
//@RequestMapping("/api/admin")
//@PreAuthorize("hasRole('ADMIN')")
//public class AdminController {
//
//    @Autowired private VideoService videoService;
//    @Autowired private UserService  userService;
//    @Autowired private JwtService   jwtService;
//
//    @GetMapping("/videos/pending")
//    public List<Video> pending() { return videoService.getPendingVideos(); }
//
//    @PostMapping("/videos/{id}/approve")
//    public void approve(@RequestHeader("Authorization") String hdr,
//                        @PathVariable Long id) {
//        String adminEmail = jwtService.getEmailFromToken(hdr.substring(7));
//        videoService.approveVideo(id);
//    }
//
//    @DeleteMapping("/videos/{id}")
//    public void deleteAny(@RequestHeader("Authorization") String hdr,
//                          @PathVariable Long id) {
//        String adminEmail = jwtService.getEmailFromToken(hdr.substring(7));
//        videoService.deleteVideo(id); // admin privilege checked inside
//    }
//}
