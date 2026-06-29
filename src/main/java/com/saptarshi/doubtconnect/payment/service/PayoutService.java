package com.saptarshi.doubtconnect.payment.service;

import com.saptarshi.doubtconnect.payment.dto.PayOutDetailsDto;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.payment.entity.BankDetails;
import com.saptarshi.doubtconnect.payment.entity.PayoutDetails;
import com.saptarshi.doubtconnect.payment.entity.UpiDetails;
import com.saptarshi.doubtconnect.repository.SessionEventRepository;
import com.saptarshi.doubtconnect.repository.TeacherAvailabilityRepository;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import com.saptarshi.doubtconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PayoutService {
//    @Value("${razorpayx.account.number}")
//    private String accountNumber;
//
//    @Value("${razorpay.commission.percent}")
//    private double commissionPercent;
//
//    @Value("${razorpay.key.id}")
//    private String keyId;
//
//    @Value("${razorpay.key.secret}")
//    private String keySecret;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private TeacherAvailabilityRepository teacherAvailabilityRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionEventRepository sessionEventRepository;
    private boolean isOwner(TeacherProfile teacher, String username) {

        Optional<User> user = userRepository.findByUsername(username);

        return teacher.getUser().getUsername().equals(username) || user.isPresent() && user.get().getRole().equals("ADMIN");
    }
//    private String createContact(TeacherProfile teacher) {
//        try {
//            RestTemplate restTemplate = new RestTemplate();
//            String url = "https://api.razorpay.com/v1/contacts";
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBasicAuth(keyId,keySecret);
//            Map<String, Object> body = new HashMap<>();
//            body.put("name", teacher.getUser().getUsername());
//            body.put("type", "employee");
//            HttpEntity<Map<String,Object>> entity = new HttpEntity<>(body,headers);
//            ResponseEntity<Map> response = restTemplate.postForEntity(url,entity,Map.class);
//            if (!response.getStatusCode().is2xxSuccessful()) {
//                return null;
//            }
//            Map body1 = response.getBody();
//
//            if (body1 == null) {
//                return null;
//            }
//
//            return (String) body1.get("id");
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//
//    }
//    private String createFundAccount(TeacherProfile teacher) {
//        try {
//            RestTemplate restTemplate = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBasicAuth(keyId,keySecret);
//            String url = "https://api.razorpay.com/v1/fund_accounts";
//            Map<String, Object> body = new HashMap<>();
//            PayoutDetails payoutDetails = teacher.getPayoutDetails();
//            body.put("contact_id", payoutDetails.getRazorpayContactId());
//            if (payoutDetails.getUpiDetails() != null) {
//                Map<String, Object> upi = new HashMap<>();
//                upi.put("address", payoutDetails.getUpiDetails().getUpiId());
//                body.put("account_type", "vpa");
//                body.put("vpa", upi);
//
//            } else {
//                Map<String, Object> bank = new HashMap<>();
//                bank.put("name",
//                        payoutDetails.getBankDetails().getAccountHolderName());
//
//                bank.put("ifsc",
//                        payoutDetails.getBankDetails().getIfscCode());
//
//                bank.put("account_number",
//                        payoutDetails.getBankDetails().getAccountNumber());
//                body.put("account_type", "bank_account");
//                body.put("bank_account", bank);
//            }
//            HttpEntity<Map<String, Object>> entity =
//                    new HttpEntity<>(body, headers);
//            ResponseEntity<Map> response = restTemplate.postForEntity(url,entity,Map.class);
//            if (!response.getStatusCode().is2xxSuccessful()) {
//                return null;
//            }
//            Map responseBody = response.getBody();
//
//            if (responseBody == null) {
//                return null;
//            }
//            return (String) responseBody.get("id");
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//
//    }
//    @Transactional
//    public boolean payoutTeacher(Long sessionEventId) {
//
//        Optional<SessionEvent> session =
//                sessionEventRepository.findById(sessionEventId);
//
//        if (session.isEmpty()) {
//            return false;
//        }
//
//        if (!session.get().getEventStatus().equals("COMPLETED")) {
//            return false;
//        }
//
//        TeacherProfile teacher = session.get().getTeacherProfile();
//
//        if (teacher.getPayoutDetails() == null) {
//            return false;
//        }
//
//        if (teacher.getPayoutDetails().getRazorpayFundAccountId() == null) {
//            return false;
//        }
//
//        try {
//
//            RestTemplate restTemplate = new RestTemplate();
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBasicAuth(keyId, keySecret);
//
//            String url = "https://api.razorpay.com/v1/payouts";
//
//            double amount = session.get().getPayment().getAmount();
//
//            double payoutAmount =
//                    amount * (100 - commissionPercent) / 100.0;
//
//            Map<String, Object> body = new HashMap<>();
//
//            body.put("account_number", accountNumber); // Your RazorpayX account number
//            body.put("fund_account_id",
//                    teacher.getPayoutDetails().getRazorpayFundAccountId());
//            body.put("amount", (long) (payoutAmount * 100));
//            body.put("currency", "INR");
//            body.put("mode", "UPI");
//            body.put("purpose", "payout");
//
//            HttpEntity<Map<String, Object>> entity =
//                    new HttpEntity<>(body, headers);
//
//            ResponseEntity<Map> response =
//                    restTemplate.postForEntity(url, entity, Map.class);
//
//            if (!response.getStatusCode().is2xxSuccessful()) {
//                return false;
//            }
//
//            session.get().getPayment().setPaymentStatus("PAID_TO_TEACHER");
//            paymentRepository.save(session.get().getPayment());
//
//            return true;
//
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
    @Transactional
    public String savePayoutDetails(long id, PayOutDetailsDto detailsDto, String username) {
        Optional<TeacherProfile> profile = teacherProfileRepository.findById(id);
        if (profile.isPresent() && isOwner(profile.get(), username)) {
            if (profile.get().getPayoutDetails() == null || profile.get().getPayoutDetails().getAccountStatus().equals("INACTIVE")) {
                PayoutDetails payoutDetails = profile.get().getPayoutDetails();
                if (payoutDetails == null) {
                    payoutDetails = new PayoutDetails();
                }
                if (detailsDto.getUpiId()!=null && !detailsDto.getUpiId().isEmpty()) {
                    UpiDetails upiDetails = new UpiDetails();
                    upiDetails.setUpiId(detailsDto.getUpiId());
                    payoutDetails.setUpiDetails(upiDetails);
                    payoutDetails.setBankDetails(null);
                } else {
                    if (detailsDto.getAccountNumber() == null ||
                            detailsDto.getIfscCode() == null ||
                            detailsDto.getAccountHolderName() == null) {
                        return "Please provide either UPI ID or complete bank details";
                    }
                    BankDetails bankDetails = new BankDetails();
                    bankDetails.setAccountNumber(detailsDto.getAccountNumber());
                    bankDetails.setIfscCode(detailsDto.getIfscCode());
                    bankDetails.setAccountHolderName(detailsDto.getAccountHolderName());
                    payoutDetails.setBankDetails(bankDetails);
                    payoutDetails.setUpiDetails(null);
                }
                payoutDetails.setAccountStatus("ACTIVE");
                profile.get().setPayoutDetails(payoutDetails);

//                    String contactId = createContact(profile.get());
//
//                    if (contactId == null) {
//                        return "Failed to create contact";
//                    }
//
//                    payoutDetails.setRazorpayContactId(contactId);
//
//                    String fundAccountId = createFundAccount(profile.get());

//                    if (fundAccountId == null) {
//                        return "Failed to create fund account";
//                    }
//
//                    payoutDetails.setRazorpayFundAccountId(fundAccountId);


                teacherProfileRepository.save(profile.get());
            } else return "Account details already saved ";
        } else return "Profile not found ";

        return "Account details added successfully ";
    }

    @Transactional
    public String updatePaymentDetails(long id , PayOutDetailsDto detailsDto , String username){
        Optional<TeacherProfile> profile = teacherProfileRepository.findById(id);
        if(profile.isPresent() && isOwner(profile.get(),username)){
            if(profile.get().getPayoutDetails()!=null){
                PayoutDetails payoutDetails = profile.get().getPayoutDetails();
                if(detailsDto.getUpiId()!=null && !detailsDto.getUpiId().isEmpty()){
                    UpiDetails upiDetails = new UpiDetails();
                    upiDetails.setUpiId(detailsDto.getUpiId());
                    payoutDetails.setUpiDetails(upiDetails);
                    payoutDetails.setBankDetails(null);
                }
                else {
                    if (detailsDto.getAccountNumber() == null ||
                            detailsDto.getIfscCode() == null ||
                            detailsDto.getAccountHolderName() == null) {
                        return "Please provide complete bank details";
                    }
                    BankDetails bankDetails = new BankDetails();
                    bankDetails.setAccountHolderName(detailsDto.getAccountHolderName());
                    bankDetails.setAccountNumber(detailsDto.getAccountNumber());
                    bankDetails.setIfscCode(detailsDto.getIfscCode());
                    payoutDetails.setBankDetails(bankDetails);
                    payoutDetails.setUpiDetails(null);
                }
                profile.get().setPayoutDetails(payoutDetails);
                teacherProfileRepository.save(profile.get());
                return "Update successful ";
            }
            else return "No existing payment details found , need to add payment details first ";
        }
        else return "Profile not found ";
    }

}
