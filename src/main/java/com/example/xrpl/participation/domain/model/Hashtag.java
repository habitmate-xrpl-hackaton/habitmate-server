package com.example.xrpl.participation.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hashtags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "name")
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private Hashtag(String name) {
        this.name = name;
    }

    public static Hashtag of(String name) {
        return new Hashtag(name);
    }
}
