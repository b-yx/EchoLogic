package com.example.smartparse.repository;

import com.example.smartparse.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
}