package ru.msu.cmc.webprak.entity;

import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.msu.cmc.webprak.common.BaseEntity;
import ru.msu.cmc.webprak.enums.ItemStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class OrderItem implements BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "edition_id")
    private Edition edition;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ItemStatus status;
}
