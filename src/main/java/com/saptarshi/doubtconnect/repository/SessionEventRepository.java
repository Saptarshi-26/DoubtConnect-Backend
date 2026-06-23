package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.SessionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionEventRepository extends JpaRepository<SessionEvent,Long> {
}
