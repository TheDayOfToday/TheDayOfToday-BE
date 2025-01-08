package com.example.thedayoftoday.repository;

import com.example.thedayoftoday.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {



}
