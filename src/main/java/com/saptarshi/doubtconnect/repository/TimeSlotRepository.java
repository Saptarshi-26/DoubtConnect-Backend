package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    public List<TimeSlot> findByTeacherProfile(TeacherProfile teacherProfile);
    public List<TimeSlot> findByTeacherProfileAndSlotStatus(TeacherProfile teacherProfile,
                                                            String slotStatus);
}
