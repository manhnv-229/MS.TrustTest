package com.mstrust.exam.repository;

import com.mstrust.exam.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findBySubmissionId(Long submissionId);
    List<SystemLog> findByLevel(String level);
}
