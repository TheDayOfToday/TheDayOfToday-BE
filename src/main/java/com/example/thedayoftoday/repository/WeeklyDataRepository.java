package com.example.thedayoftoday.repository;

import com.example.thedayoftoday.entity.WeeklyData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyDataRepository extends JpaRepository<WeeklyData, Long> {


}
