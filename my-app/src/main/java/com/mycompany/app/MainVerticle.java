package com.mycompany.app;

import com.mycompany.app.App.Order;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.EventBus;

public class MainVerticle {
    public static class OrderCodec implements MessageCodec<Order, Order> {

    @Override
    public void encodeToWire(Buffer buffer, Order order) {
        // Implement serialization logic to write to the buffer
    }

    @Override
    public Order decodeFromWire(int pos, Buffer buffer) {
        // Implement deserialization logic to read from the buffer
        return null;
    }

    @Override
    public Order transform(Order order) {
        // Return the object itself (no transformation)
        return order;
    }

    @Override
    public String name() {
        return "orderCodec";
    }

    @Override
    public byte systemCodecID() {
        // Use a negative identifier to avoid conflicts with built-in codecs
        return -1;
    }
}


    public static class ReturnCodec implements MessageCodec<MyData, MyData> {

    @Override
    public void encodeToWire(Buffer buffer, MyData myData) {
        // Implement serialization logic to write to the buffer
    }

    @Override
    public MyData decodeFromWire(int pos, Buffer buffer) {
        // Implement deserialization logic to read from the buffer
        return null;
    }

    @Override
    public MyData transform(MyData myData) {
        // Return the object itself (no transformation)
        return myData;
    }

    @Override
    public String name() {
        return "ReturnCodec";
    }

    @Override
    public byte systemCodecID() {
        // Use a negative identifier to avoid conflicts with built-in codecs
        return -1;
    }
}


    
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        EventBus eventBus = vertx.eventBus();

        eventBus.registerDefaultCodec(MyData.class, new ReturnCodec());
        eventBus.registerDefaultCodec(Order.class, new OrderCodec());

                // Deploy ServerRouteVerticle
        vertx.deployVerticle(new ServerRouteVerticle(), ar -> {
            if (ar.succeeded()) {
                System.out.println("ServerRouteVerticle deployed successfully");
            } else {
                System.out.println("ServerRouteVerticle deployment failed: " + ar.cause().getMessage());
            }
        });

                // Deploy MSIMatchingRouteVerticle
        vertx.deployVerticle(new MSIMatchingVerticle(), ar -> {
            if (ar.succeeded()) {
                System.out.println("MSIMatchingVerticle deployed successfully");
            } else {
                System.out.println("MSIMatchingVerticle deployment failed: " + ar.cause().getMessage());
            }
        });
                // Deploy AsusMatchingRouteVerticle
        vertx.deployVerticle(new AsusMatchingVerticle(), ar -> {
            if (ar.succeeded()) {
                System.out.println("AsusMatchingVerticle deployed successfully");
            } else {
                System.out.println("AsusMatchingVerticle deployment failed: " + ar.cause().getMessage());
            }
        });
                // Deploy AppleMatchingRouteVerticle
        vertx.deployVerticle(new AppleMatchingVerticle(), ar -> {
            if (ar.succeeded()) {
                System.out.println("AppleMatchingVerticle deployed successfully");
            } else {
                System.out.println("AppleMatchingVerticle deployment failed: " + ar.cause().getMessage());
            }
        });
                // Deploy HPMatchingRouteVerticle
        vertx.deployVerticle(new HPMatchingVerticle(), ar -> {
            if (ar.succeeded()) {
                System.out.println("HPMatchingVerticle deployed successfully");
            } else {
                System.out.println("HPMatchingVerticle deployment failed: " + ar.cause().getMessage());
            }
        });
                // Deploy DellMatchingRouteVerticle
        vertx.deployVerticle(new DellMatchingVerticle(), ar -> {
            if (ar.succeeded()) {
                System.out.println("DellMatchingVerticle deployed successfully");
            } else {
                System.out.println("DellMatchingVerticle deployment failed: " + ar.cause().getMessage());
            }
        });



    }
    
}
