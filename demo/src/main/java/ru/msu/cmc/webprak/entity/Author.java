package ru.msu.cmc.webprak.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.msu.cmc.webprak.common.BaseEntity;

import java.util.List;

@Entity
@Table(name = "authors")

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class Author implements BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    private Integer id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @ManyToMany(mappedBy = "authors")
    private List<Work> works;
}
