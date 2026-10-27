package com.example.watchlog.repository;

import com.example.watchlog.domain.Work;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkRepository extends JpaRepository<Work, Long> {}
