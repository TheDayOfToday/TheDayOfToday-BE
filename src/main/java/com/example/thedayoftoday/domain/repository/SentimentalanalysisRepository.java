package com.example.thedayoftoday.domain.repository;

import com.example.thedayoftoday.domain.entity.Sentimentalanalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SentimentalanalysisRepository extends JpaRepository<Sentimentalanalysis, Long> {

}
