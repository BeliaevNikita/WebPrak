package ru.msu.cmc.webprak.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.msu.cmc.webprak.common.BaseEntity;

@Entity
@Table(name = "covers")

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class Cover implements BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cover_id")
    private Integer id;

    @Column(name = "cover_name", unique = true)
    private String coverName;
}
