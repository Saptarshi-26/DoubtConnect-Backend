package com.saptarshi.doubtconnect.service;

import com.razorpay.Utils;
import com.saptarshi.doubtconnect.entity.SessionEvent;
import com.saptarshi.doubtconnect.entity.SessionRequest;
import com.saptarshi.doubtconnect.entity.payment.Payment;
import com.saptarshi.doubtconnect.entity.payment.RazorpayOrder;
import com.saptarshi.doubtconnect.repository.PaymentRepository;
import com.saptarshi.doubtconnect.repository.RazorpayOrderRepository;
import com.saptarshi.doubtconnect.repository.SessionEventRepository;
import com.saptarshi.doubtconnect.repository.SessionRequestRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    SessionRequestRepository requestRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    private RazorpayOrderRepository razorpayOrderRepository;

    @Autowired
    private SessionEventRepository sessionEventRepository;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @Transactional
    public boolean handleWebhook(String payload, String signature){
        try {
            Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            JSONObject payloadJson = new JSONObject(payload);
            JSONObject paymentEntity = payloadJson.
                    getJSONObject("payload").
                    getJSONObject("payment").
                    getJSONObject("entity");

            String razorpayOrderId = paymentEntity.getString("order_id");
            String transactionId = paymentEntity.getString("id");
            String paymentMethod = paymentEntity.getString("method");

            Optional<RazorpayOrder> razorpayOrder =
                    razorpayOrderRepository.findByRazorpayOrderId(razorpayOrderId);

            if(razorpayOrder.isEmpty())return false;

            if ("PAID".equals(razorpayOrder.get().getOrderStatus())) {
                return true;
            }
            if (paymentRepository.findByTransactionId(transactionId).isPresent()) {
                return true;
            }

            razorpayOrder.get().setOrderStatus("PAID");
            razorpayOrderRepository.save(razorpayOrder.get());


            Payment payment = new Payment();
            payment.setTransactionId(transactionId);
            payment.setPaymentMethod(paymentMethod);
            payment.setPaymentStatus("COMPLETED");
            payment.setPaymentTime(LocalDateTime.now());
            payment.setAmount(razorpayOrder.get().getAmount());

            paymentRepository.save(payment);


            SessionRequest sessionRequest = razorpayOrder.get().getSessionRequest();
            sessionRequest.setPaymentStatus("COMPLETED");
            requestRepository.save(sessionRequest);

            SessionEvent sessionEvent = new SessionEvent();
            sessionEvent.setPayment(payment);
            sessionEvent.setEventStatus("UPCOMING");
            sessionEvent.setSessionRequest(razorpayOrder.get().getSessionRequest());
            sessionEvent.setStartTime(razorpayOrder.get().getStartTime());
            sessionEvent.setEndTime(razorpayOrder.get().getEndTime());
            sessionEvent.setStudentProfile(razorpayOrder.get().getSessionRequest().getStudentProfile());
            sessionEvent.setTeacherProfile(razorpayOrder.get().getSessionRequest().getTeacherProfile());
            sessionEvent.setMeetLink(null);

            sessionEventRepository.save(sessionEvent);


        }

       catch (Exception e){
           e.printStackTrace();
            return false;
       }
        return true;
    }


}
