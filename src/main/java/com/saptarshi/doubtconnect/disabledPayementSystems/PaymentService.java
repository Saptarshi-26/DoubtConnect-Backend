//package com.saptarshi.doubtconnect.disabledPayementSystems;
//
//import com.razorpay.RazorpayClient;
//import com.razorpay.Refund;
//import com.razorpay.Utils;
//import com.saptarshi.doubtconnect.entity.SessionEvent;
//import com.saptarshi.doubtconnect.entity.SessionRequest;
//import com.saptarshi.doubtconnect.entity.User;
//import com.saptarshi.doubtconnect.entity.payment.Payment;
//import com.saptarshi.doubtconnect.disabledPayementSystems.RazorpayOrder;
//import com.saptarshi.doubtconnect.repository.*;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//@Service
//public class PaymentService {
//
//    @Value("${razorpay.key.id}")
//    private String keyId;
//
//    @Value("${razorpay.key.secret}")
//    private String keySecret;
//
//    @Autowired
//    SessionRequestRepository requestRepository;
//
//    @Autowired
//    PaymentRepository paymentRepository;
//
//    @Autowired
//    private RazorpayOrderRepository razorpayOrderRepository;
//
//    @Autowired
//    private SessionEventRepository sessionEventRepository;
//
//    @Value("${razorpay.webhook.secret}")
//    private String webhookSecret;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private boolean isOwner(SessionEvent sessionEvent,
//                            Authentication authentication) {
//
//        Optional<User> user =
//                userRepository.findByUsername(authentication.getName());
//
//        return sessionEvent.getStudentProfile().getUser().getUsername()
//                .equals(authentication.getName())
//                ||
//                sessionEvent.getTeacherProfile().getUser().getUsername()
//                        .equals(authentication.getName())
//                ||
//                user.isPresent() &&
//                        user.get().getRole().equals("ADMIN");
//    }
//
//    @Transactional
//    public boolean handleWebhook(String payload, String signature){
//        try {
//            Utils.verifyWebhookSignature(payload, signature, webhookSecret);
//            JSONObject payloadJson = new JSONObject(payload);
//            JSONObject paymentEntity = payloadJson.
//                    getJSONObject("payload").
//                    getJSONObject("payment").
//                    getJSONObject("entity");
//
//            String razorpayOrderId = paymentEntity.getString("order_id");
//            String transactionId = paymentEntity.getString("id");
//            String paymentMethod = paymentEntity.getString("method");
//
//            Optional<RazorpayOrder> razorpayOrder =
//                    razorpayOrderRepository.findByRazorpayOrderId(razorpayOrderId);
//
//            if(razorpayOrder.isEmpty())return false;
//
//            if ("PAID".equals(razorpayOrder.get().getOrderStatus())) {
//                return true;
//            }
//            if (paymentRepository.findByTransactionId(transactionId).isPresent()) {
//                return true;
//            }
//
//            razorpayOrder.get().setOrderStatus("PAID");
//            razorpayOrderRepository.save(razorpayOrder.get());
//
//
//            Payment payment = new Payment();
//            payment.setTransactionId(transactionId);
//            payment.setPaymentMethod(paymentMethod);
//            payment.setPaymentStatus("COMPLETED");
//            payment.setPaymentStatus("COMPLETED");
//            payment.setPaymentTime(LocalDateTime.now());
//            payment.setAmount(razorpayOrder.get().getAmount());
//
//            paymentRepository.save(payment);
//
//
//            SessionRequest sessionRequest = razorpayOrder.get().getSessionRequest();
//            sessionRequest.setPaymentStatus("COMPLETED");
//            requestRepository.save(sessionRequest);
//
//            SessionEvent sessionEvent = new SessionEvent();
//            sessionEvent.setPayment(payment);
//            sessionEvent.setEventStatus("UPCOMING");
//            sessionEvent.setSessionRequest(razorpayOrder.get().getSessionRequest());
//            sessionEvent.setStartTime(razorpayOrder.get().getStartTime());
//            sessionEvent.setEndTime(razorpayOrder.get().getEndTime());
//            sessionEvent.setStudentProfile(razorpayOrder.get().getSessionRequest().getStudentProfile());
//            sessionEvent.setTeacherProfile(razorpayOrder.get().getSessionRequest().getTeacherProfile());
//            sessionEvent.setMeetLink(null);
//
//            sessionEventRepository.save(sessionEvent);
//
//
//        }
//
//       catch (Exception e){
//           e.printStackTrace();
//            return false;
//       }
//        return true;
//    }
//
//    @Transactional
//    public boolean refundPayment(
//            Long sessionEventId,
//            Authentication authentication) {
//        Optional<SessionEvent> session =
//                sessionEventRepository.findById(sessionEventId);
//
//        if (session.isEmpty()) {
//            return false;
//        }
//        if (!isOwner(session.get(), authentication)) {
//            return false;
//        }
//        if (session.get().getPayment() == null ) {
//            return false;
//        }
//        if (!session.get().getPayment()
//                .getPaymentStatus()
//                .equals("COMPLETED")) {
//
//            return false;
//        }
//        if (session.get().getStartTime().isBefore(LocalDateTime.now())) {
//            return false;
//        }
//
//
//        try {
//            RazorpayClient razorpayClient =
//                    new RazorpayClient(keyId, keySecret);
//            JSONObject refundRequest = new JSONObject();
//            refundRequest.put("amount",
//                    (long) (session.get().getPayment().getAmount() * 100));
//            String transactionId = session.get().getPayment().getTransactionId();
//            Refund refund = razorpayClient.payments
//                    .refund(transactionId, refundRequest);
//            if (refund == null) {
//                return false;
//            }
//            session.get().getPayment().setPaymentStatus("REFUNDED");
//
//            session.get().setEventStatus("CANCELLED");
//
//            session.get().getSessionRequest().setPaymentStatus("REFUNDED");
//
//            paymentRepository.save(session.get().getPayment());
//            requestRepository.save(session.get().getSessionRequest());
//            sessionEventRepository.save(session.get());
//        }
//        catch (Exception e){
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//
//    }
//
//
//}
