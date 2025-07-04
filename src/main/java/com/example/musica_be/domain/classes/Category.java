package com.example.musica_be.domain.classes;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
    Piano("피아노"),
    Guitar("기타"),
    Drums("드럼"),
    Violin("바이올린"),
    Viola("비올라"),
    Cello("첼로"),
    DoubleBass("더블베이스"),
    Flute("플루트"),
    Clarinet("클라리넷"),
    Oboe("오보에"),
    Bassoon("바순"),
    Trumpet("트럼펫"),
    Trombone("트럼본"),
    Harp("하프"),
    Ukulele("우쿨렐레"),
    ElectricGuitar("일렉기타"),
    AcousticGuitar("통기타"),
    Keyboard("전자 키보드"),
    Xylophone("실로폰"),
    Saxophone("색소폰"),
    Harmonica("하모니카"),
    Recorder("리코더"),
    Melodica("멜로디카"),
    Vocal("보컬"),
    Composition("작곡"),
    Beatbox("비트박스"),
    Mixing("믹싱"),
    Mastering("마스터링"),
    MusicTheory("화성학");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}