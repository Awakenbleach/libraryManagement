package com.skillovilla.readingmaterial.repository;

import com.skillovilla.readingmaterial.entity.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, UUID> {
}
