//package com.saptarshi.doubtconnect.google;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.view.RedirectView;
//
//@RestController
//@RequestMapping("/oauth/google")
//public class GoogleOAuthController {
//
//    @Autowired
//    private GoogleOAuthService googleOAuthService;
//
//    @GetMapping("/status/{teacherProfileId}")
//    public ResponseEntity<GoogleConnectionStatusDto> isGoogleConnected(
//            @PathVariable Long teacherProfileId,
//            Authentication authentication) {
//
//        return ResponseEntity.ok(
//                googleOAuthService.isGoogleConnected(
//                        teacherProfileId,
//                        authentication));
//    }
//
//
//
//    @GetMapping("/connect")
//    public ResponseEntity<String> connectGoogle(
//            @RequestParam Long teacherProfileId,
//            Authentication authentication) {
//
//        String url = googleOAuthService.getAuthorizationUrl(
//                teacherProfileId,
//                authentication);
//
//        if (url == null) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        return ResponseEntity.ok(url);
//    }
//
//    @GetMapping("/callback")
//    public RedirectView callback(
//            @RequestParam("code") String code,
//            @RequestParam("state") Long teacherProfileId) {
//
//        googleOAuthService.saveTeacherCredential(code, teacherProfileId);
//
//        return new RedirectView(
//                "http://localhost:5173/teacher-availability"
//        );
//    }
//
//}
