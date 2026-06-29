//package com.saptarshi.doubtconnect.service.payment;
//
//import com.razorpay.Order;
//import com.razorpay.RazorpayClient;
//import com.saptarshi.doubtconnect.entity.SessionRequest;
//import com.saptarshi.doubtconnect.entity.StudentProfile;
//import com.saptarshi.doubtconnect.entity.User;
//import com.saptarshi.doubtconnect.disabledPayementSystems.RazorpayOrder;
//import com.saptarshi.doubtconnect.disabledPayementSystems.RazorpayOrderRepository;
//import com.saptarshi.doubtconnect.repository.SessionRequestRepository;
//import com.saptarshi.doubtconnect.repository.UserRepository;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//@Service
//public class RazorpayService {
//    @Autowired
//    private SessionRequestRepository sessionRequestRepository;
//
//    @Autowired
//    private RazorpayOrderRepository razorpayOrderRepository;
//
//    @Value("${razorpay.key.id}")
//    private String keyId;
//
//    @Value("${razorpay.key.secret}")
//    private String keySecret;
//
//    @Autowired
//    UserRepository userRepository;
//
//    private boolean ownership(StudentProfile studentProfile , Authentication authentication) {
//        Optional<User> user  = userRepository.findByUsername(authentication.getName());
//        return user.filter(value -> studentProfile.getUser().getUsername().equals(authentication.getName()) ||
//                value.getRole().equals("ADMIN")).isPresent();
//    }
//    public RazorpayOrder createOrder(long sessionRequestId, Authentication authentication,
//                                     LocalDateTime startTime, LocalDateTime endTime){
//
//
//        Optional<SessionRequest> session = sessionRequestRepository.findById(sessionRequestId);
//        if(session.isPresent()){
//            if(!ownership(session.get().getStudentProfile(), authentication))return null;
//            if(session.get().getPaymentStatus().equals("PENDING") &&
//                    session.get().getStatus().equals("ACCEPTED")){
//                try {
//                    RazorpayClient razorpayClient = new RazorpayClient(keyId,keySecret);
//
//                    JSONObject orderRequest = new JSONObject();
//                    orderRequest.put("amount",(long)(session.get().getTotalAmount()*100));
//                    orderRequest.put("currency","INR");
//                    orderRequest.put("receipt","receipt_"+sessionRequestId);
//
//                    Order order = razorpayClient.orders.create(orderRequest);
//
//                    RazorpayOrder razorpayOrder = new RazorpayOrder();
//                    razorpayOrder.setRazorpayOrderId(order.get("id"));
//                    razorpayOrder.setAmount(session.get().getTotalAmount());
//                    razorpayOrder.setSessionRequest(session.get());
//                    razorpayOrder.setStartTime(startTime);
//                    razorpayOrder.setEndTime(endTime);
//
//                    razorpayOrderRepository.save(razorpayOrder);
//                    return razorpayOrder;
//                }
//                catch (Exception e ){
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//            return null;
//        }
//        return null;
//    }
//}
