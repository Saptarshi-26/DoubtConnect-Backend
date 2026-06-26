package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.payment.RazorpayOrder;
import com.saptarshi.doubtconnect.service.RazorpayService;
import org.aspectj.apache.bcel.classfile.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.saptarshi.doubtconnect.repository.RazorpayOrderRepository;

import java.util.Optional;

@Repository
public interface RazorpayOrderRepository
        extends JpaRepository<RazorpayOrder, Long> {
    public Optional<RazorpayOrder> findByRazorpayOrderId(String razorpayOrderId);

}
