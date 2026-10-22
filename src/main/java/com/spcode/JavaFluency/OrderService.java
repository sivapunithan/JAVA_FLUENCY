package com.spcode.JavaFluency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public void placeOrder() {
        System.out.println("placeOrder() called - about to call saveOrder() via 'this'");
        this.saveOrder(); // self-invocation - bypasses the proxy
        System.out.println("placeOrder() finished (you should NOT reach here if rollback worked)");
    }

    @Transactional
    public void saveOrder() {
        System.out.println("saveOrder() started - inside what SHOULD be a transaction");
        orderRepository.save(new Order("Laptop"));
        System.out.println("Order saved (pending commit) - about to throw");
        throw new RuntimeException("Something went wrong mid-save!");
    }

    public long getOrderCount() {
        return orderRepository.count();
    }
}
