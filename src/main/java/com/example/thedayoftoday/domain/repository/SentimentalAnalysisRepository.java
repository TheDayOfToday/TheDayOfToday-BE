package com.example.thedayoftoday.domain.repository;

import com.example.thedayoftoday.domain.entity.SentimentalAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SentimentalAnalysisRepository extends JpaRepository <SentimentalAnalysis, Long> {

import org.springframework.stereotype.Repository;

@Repository
public interface SentimentalAnalysisRepository extends JpaRepository<SentimentalAnalysis, Long> {


}
