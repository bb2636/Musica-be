package com.example.musica_be.repository.classes;

import com.example.musica_be.domain.classes.Category;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCode(String code);
}
