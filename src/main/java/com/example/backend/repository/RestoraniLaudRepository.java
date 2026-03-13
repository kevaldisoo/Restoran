package com.example.backend.repository;

import com.example.backend.model.RestoraniLaud;
import com.example.backend.model.TsooniTyyp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestoraniLaudRepository extends JpaRepository<RestoraniLaud, Long> {
    List<RestoraniLaud> findByTsoon(TsooniTyyp tsoon);
}
