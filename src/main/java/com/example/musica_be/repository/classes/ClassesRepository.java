package com.example.musica_be.repository.classes;

import com.example.musica_be.domain.classes.Classes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassesRepository extends JpaRepository<Classes, Long> {

}
