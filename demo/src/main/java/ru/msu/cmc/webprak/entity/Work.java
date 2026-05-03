package ru.msu.cmc.webprak.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.msu.cmc.webprak.common.BaseEntity;

import java.util.List;

@Entity
@Table(name = "works")

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor


public class Work implements BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_id")
    private Integer id;

    private String title;

    private String description;

    @ManyToMany
    @JoinTable(
            name = "publications",
            joinColumns = @JoinColumn(name = "work_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private List<Author> authors;
}
