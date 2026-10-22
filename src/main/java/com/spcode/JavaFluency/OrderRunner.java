package com.spcode.JavaFluency;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class OrderRunner implements CommandLineRunner {

    private final OrderService orderService;

    public OrderRunner(OrderService orderService){
        this.orderService = orderService;
    }

    @Override
    public void run(String... args){
        try {
            orderService.placeOrder();
        } catch (RuntimeException e){
            System.out.println("Caught exception in runner: "+e.getMessage());
        }
        System.out.println("Orders remaining in DB after the failed placeOrder(): " + orderService.getOrderCount());
    }
}
