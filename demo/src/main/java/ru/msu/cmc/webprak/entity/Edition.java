package ru.msu.cmc.webprak.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.msu.cmc.webprak.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "editions")

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class Edition implements BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "edition_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "work_id")
    private Work work;

    @ManyToOne
    @JoinColumn(name = "cover_id")
    private Cover cover;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    private String publisher;

    private String language;

    private Integer quantity;

    private BigDecimal price;
}
