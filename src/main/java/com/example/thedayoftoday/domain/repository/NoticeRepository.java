package com.example.thedayoftoday.domain.repository;

import com.example.thedayoftoday.domain.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

}
