package com.example.musica_be.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InstrumentCategoryMapper {

    private static final Map<String, String> instrumentToCategoryMap = Map.ofEntries(
        Map.entry("bass", "BASS"),
        Map.entry("drums", "DRUM"),
        Map.entry("guitars", "GUITAR"),
        Map.entry("keys", "KEYBOARD"),
        Map.entry("percussion", "PERCUSSION"),
        Map.entry("piano", "PIANO"),
        Map.entry("strings", "STRINGS"),
        Map.entry("vocals", "VOCAL"),
        Map.entry("wind", "WIND")
    );

    public static List<String> toCategories(List<String> instruments) {
        return instruments.stream()
            .map(instrumentToCategoryMap::get)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }
}