package com.spcode.JavaFluency;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String item;

    public Order() {}

    public Order(String item) {
        this.item = item;
    }

    public Long getId() { return id; }
    public String getItem() { return item; }
}
