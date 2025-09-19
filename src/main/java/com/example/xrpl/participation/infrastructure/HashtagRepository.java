package com.example.xrpl.participation.infrastructure;

import com.example.xrpl.participation.domain.model.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);

    List<Hashtag> findByNameIn(Set<String> names);
}