package ru.msu.cmc.webprak.entity;

import ru.msu.cmc.webprak.common.BaseEntity;
import ru.msu.cmc.webprak.enums.OrderStatus;

import lombok.*;

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
    private OrderStatus status;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> items;
}
