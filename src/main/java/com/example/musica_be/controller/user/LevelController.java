package com.example.musica_be.controller.user;

import com.example.musica_be.domain.user.Level;
import com.example.musica_be.repository.user.LevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/levels")
@RequiredArgsConstructor
public class LevelController {

    private final LevelRepository levelRepository;

    @GetMapping
    public List<Level> getLevels() {
        return levelRepository.findAll();
    }
}
