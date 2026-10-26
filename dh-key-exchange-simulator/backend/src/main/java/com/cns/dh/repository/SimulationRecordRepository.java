package com.cns.dh.repository;

import com.cns.dh.model.SimulationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SimulationRecordRepository extends JpaRepository<SimulationRecord, Long> {
    List<SimulationRecord> findTop50ByOrderByCreatedAtDesc();
}
