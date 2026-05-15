package ru.msu.cmc.webprak.entity;

import ru.msu.cmc.webprak.common.BaseEntity;
import ru.msu.cmc.webprak.enums.OrderStatus;

import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "orders")

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class Order implements BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private OrderStatus status;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> items;
}
