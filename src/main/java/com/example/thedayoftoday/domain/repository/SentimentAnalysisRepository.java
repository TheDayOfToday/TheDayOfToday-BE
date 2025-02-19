package com.example.thedayoftoday.domain.repository;

import com.example.thedayoftoday.domain.entity.SentimentalAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SentimentAnalysisRepository extends JpaRepository<SentimentalAnalysis, Long> {

}
