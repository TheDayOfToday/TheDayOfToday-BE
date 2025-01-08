package com.example.thedayoftoday.repository;

import com.example.thedayoftoday.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

}
