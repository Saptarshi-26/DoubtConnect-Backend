package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.*;
import com.saptarshi.doubtconnect.entity.SessionRequest;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.entity.payment.BankDetails;
import com.saptarshi.doubtconnect.entity.payment.PayoutDetails;
import com.saptarshi.doubtconnect.entity.payment.UpiDetails;
import com.saptarshi.doubtconnect.repository.SessionRequestRepository;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import com.saptarshi.doubtconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRequestRepository sessionRequestRepository;

    private boolean isOwner(TeacherProfile teacher, String username) {

        Optional<User> user = userRepository.findByUsername(username);

        return teacher.getUser().getUsername().equals(username) || user.isPresent() && user.get().getRole().equals("ADMIN");
    }


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


    public List<TeacherProfile> findAll() {
        return teacherProfileRepository.findAll().stream().filter(x -> x.getPayoutDetails() != null && x.getPayoutDetails().getAccountStatus().equals("ACTIVE")).toList();
    }

    public Optional<TeacherProfile> findTeacher(Long id) {
        return teacherProfileRepository.findById(id);
    }


    public boolean updateBio(Long id, UpdateBioDTO dto, String username) {

        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(id);

        if (teacher.isEmpty() || !isOwner(teacher.get(), username)) {
            return false;
        }

        teacher.get().setBio(dto.getBio());
        teacherProfileRepository.save(teacher.get());

        return true;
    }

    public boolean updateRate(Long id, UpdateRateDTO dto, String username) {

        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(id);

        if (teacher.isEmpty() || !isOwner(teacher.get(), username)) {
            return false;
        }

        teacher.get().setRatePerThirtyMin(dto.getRatePerThirtyMin());

        teacherProfileRepository.save(teacher.get());

        return true;
    }

    public boolean addSubject(Long id, SubjectDTO dto, String username) {

        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(id);

        if (teacher.isEmpty() || !isOwner(teacher.get(), username)) {
            return false;
        }

        if (!teacher.get().getSubjects().contains(dto.getSubject())) {

            teacher.get().getSubjects().add(dto.getSubject());

            teacherProfileRepository.save(teacher.get());
        }

        return true;
    }

    public boolean removeSubject(Long id, SubjectDTO dto, String username) {

        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(id);

        if (teacher.isEmpty() || !isOwner(teacher.get(), username)) {
            return false;
        }

        teacher.get().getSubjects().remove(dto.getSubject());

        teacherProfileRepository.save(teacher.get());

        return true;
    }
}