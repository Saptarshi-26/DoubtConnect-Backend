package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.entity.payment.RazorpayOrder;
import com.saptarshi.doubtconnect.repository.RazorpayOrderRepository;
import com.saptarshi.doubtconnect.repository.SessionRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {
    @Autowired
    private SessionRequestRepository sessionRequestRepository;

    @Autowired
    private RazorpayOrderRepository razorpayOrderRepository;

    public RazorpayOrder createOrder(long sessionRequestId){
        return null;
    }
}
