package com.example.thedayoftoday.repository;

import com.example.thedayoftoday.entity.Sentimentalanalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SentimentalanalysisRepository extends JpaRepository<Sentimentalanalysis, Long> {

}
