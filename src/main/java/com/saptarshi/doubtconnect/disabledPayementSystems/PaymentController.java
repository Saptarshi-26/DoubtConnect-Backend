//package com.saptarshi.doubtconnect.controller;
//
//import com.saptarshi.doubtconnect.dto.OrderRequestDto;
//import com.saptarshi.doubtconnect.disabledPayementSystems.RazorpayOrder;
//import com.saptarshi.doubtconnect.disabledPayementSystems.PaymentService;
//import com.saptarshi.doubtconnect.service.payment.RazorpayService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/payment")
//public class PaymentController {
//
//    @Autowired
//    RazorpayService razorpayService;
//
//    @Autowired
//    private PaymentService paymentService;
//
//    @PostMapping("/order/{sessionId}")
//    public ResponseEntity<?> paymentOrder(@PathVariable long sessionId ,
//                                          Authentication authentication ,
//                                          @RequestBody OrderRequestDto dto){
//
//        RazorpayOrder order = razorpayService.createOrder(sessionId,authentication,dto.getStartTime(),dto.getEndTime());
//        return order==null?new ResponseEntity<>(HttpStatus.NOT_FOUND):
//                new ResponseEntity<>(order,HttpStatus.OK);
//    }
//
//    @PostMapping("/webhook")
//    public ResponseEntity<?> webhook(@RequestBody String payload ,
//                                     @RequestHeader("X-Razorpay-Signature") String signature){
//        boolean success = paymentService.handleWebhook(payload, signature);
//        return success ? new ResponseEntity<>(HttpStatus.OK) :
//                new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//    }
//    @PostMapping("/refund/{sessionEventId}")
//    public ResponseEntity<?> refund(
//            @PathVariable Long sessionEventId,
//            Authentication authentication) {
//
//    }
//}
