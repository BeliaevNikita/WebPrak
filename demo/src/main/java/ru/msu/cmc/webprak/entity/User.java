package ru.msu.cmc.webprak.entity;

import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.msu.cmc.webprak.common.BaseEntity;
import ru.msu.cmc.webprak.enums.UserRole;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "users")

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class User implements BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserRole role;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String contacts;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private Boolean gender;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true, nullable = false)
    private String login;

    private String password;
}
