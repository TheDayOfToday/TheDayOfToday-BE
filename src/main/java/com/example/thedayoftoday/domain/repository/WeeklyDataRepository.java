package com.example.thedayoftoday.domain.repository;

import com.example.thedayoftoday.domain.entity.WeeklyData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyDataRepository extends JpaRepository<WeeklyData, Long> {


}
