package com.mycompany.app;

import java.util.List;

import com.mycompany.app.App.ExecutionRow;
import com.mycompany.app.App.Order;

import io.vertx.core.AbstractVerticle;


public class AppleMatchingVerticle extends AbstractVerticle {
    public String absolutePath = "C:/Users/MSI/Desktop/Ubuntu/my-app";
    @Override
    public void start() {
        vertx.eventBus().<Order>consumer("Apple", message -> {
            Order newOrder = message.body();
            System.out.println("New Order: "+newOrder.instrument);
            List<Order> orderTable = App.readCSVOrders(absolutePath + "/Data/Apple/orderProcess.csv");
            int ordIndex = App.getLastOrderID(orderTable);
            newOrder.orderID = "ORD" + ordIndex;
            int Quantity = newOrder.quantity;
            List<ExecutionRow> executionTable = App.readCSVTraders(absolutePath + "/Data/Apple/ExchangedTable.csv");
            int no_Traders_Before = executionTable.size();
            orderTable.add(newOrder);
            List<Order> buyTable = App.readCSVOrders(absolutePath + "/Data/Apple/buyTable.csv");
            List<Order> sellTable = App.readCSVOrders(absolutePath + "/Data/Apple/sellTable.csv");
            App.OrderExchanger( orderTable, newOrder, buyTable, sellTable, executionTable );
            App.writeExecutionToCSV(executionTable, absolutePath + "/Data/Apple/ExchangedTable.csv");
            App.writeOrdersToCSV(orderTable, absolutePath + "/Data/Apple/orderProcess.csv");
            App.writeOrdersToCSV(buyTable, absolutePath + "/Data/Apple/buyTable.csv");
            App.writeOrdersToCSV(sellTable, absolutePath + "/Data/Apple/sellTable.csv");

            Order order = orderTable.get(orderTable.size() - 1);
            order.quantity = Quantity;
            MyData myData = new MyData();
            myData.Order = order;
            myData.OrderTable = orderTable;
            myData.Traders= executionTable;
            myData.CollectionName = "Apple";
            myData.no_Traders_Before = no_Traders_Before;

            vertx.eventBus().send("Saving_Trading", myData);

        });

        vertx.eventBus().<Order>consumer("Apple_Update", message -> {
            Order updateOrder = message.body();
            List<Order> orderTable = App.readCSVOrders(absolutePath + "/Data/Apple/orderProcess.csv");
            int preSide = App.updateOrder(orderTable,updateOrder);
            App.writeOrdersToCSV(orderTable, absolutePath + "/Data/Apple/orderProcess.csv");
            List<Order> buyTable = App.readCSVOrders(absolutePath + "/Data/Apple/buyTable.csv");
            List<Order> sellTable = App.readCSVOrders(absolutePath + "/Data/Apple/sellTable.csv");
            App.deleteOrder(buyTable, sellTable, updateOrder, preSide);    
            App.insertOrder(buyTable, sellTable, updateOrder);      
            App.writeOrdersToCSV(buyTable, absolutePath + "/Data/Apple/buyTable.csv");
            App.writeOrdersToCSV(sellTable, absolutePath + "/Data/Apple/sellTable.csv");

        });

    }


    
}
