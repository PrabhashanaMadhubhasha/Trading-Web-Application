package com.mycompany.app;

import java.util.List;

import com.mycompany.app.App.ExecutionRow;
import com.mycompany.app.App.Order;

import io.vertx.core.AbstractVerticle;


public class HPMatchingVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.eventBus().<Order>consumer("HP", message -> {
            Order newOrder = message.body();
            System.out.println("New Order: "+newOrder.instrument);
            List<Order> orderTable = App.readCSVOrders("/home/prabhashana/my-app/Data/HP/orderProcess.csv");
            int ordIndex = App.getLastOrderID(orderTable);
            newOrder.orderID = "ORD" + ordIndex;
            int Quantity = newOrder.quantity;
            List<ExecutionRow> executionTable = App.readCSVTraders("/home/prabhashana/my-app/Data/HP/ExchangedTable.csv");
            int no_Traders_Before = executionTable.size();
            orderTable.add(newOrder);
            List<Order> buyTable = App.readCSVOrders("/home/prabhashana/my-app/Data/HP/buyTable.csv");
            List<Order> sellTable = App.readCSVOrders("/home/prabhashana/my-app/Data/HP/sellTable.csv");
            App.OrderExchanger( orderTable, newOrder, buyTable, sellTable, executionTable );
            App.writeExecutionToCSV(executionTable, "/home/prabhashana/my-app/Data/HP/ExchangedTable.csv");
            App.writeOrdersToCSV(orderTable, "/home/prabhashana/my-app/Data/HP/orderProcess.csv");
            App.writeOrdersToCSV(buyTable, "/home/prabhashana/my-app/Data/HP/buyTable.csv");
            App.writeOrdersToCSV(sellTable, "/home/prabhashana/my-app/Data/HP/sellTable.csv");

            Order order = orderTable.get(orderTable.size() - 1);
            order.quantity = Quantity;
            MyData myData = new MyData();
            myData.Order = order;
            myData.OrderTable = orderTable;
            myData.Traders= executionTable;
            myData.CollectionName = "HP";
            myData.no_Traders_Before = no_Traders_Before;

            vertx.eventBus().send("Saving_Trading", myData);

        });

        vertx.eventBus().<Order>consumer("HP_Update", message -> {
            Order updateOrder = message.body();
            List<Order> orderTable = App.readCSVOrders("/home/prabhashana/my-app/Data/HP/orderProcess.csv");
            int preSide = App.updateOrder(orderTable,updateOrder);
            App.writeOrdersToCSV(orderTable, "/home/prabhashana/my-app/Data/HP/orderProcess.csv");          
            List<Order> buyTable = App.readCSVOrders("/home/prabhashana/my-app/Data/HP/buyTable.csv");
            List<Order> sellTable = App.readCSVOrders("/home/prabhashana/my-app/Data/HP/sellTable.csv");
            App.deleteOrder(buyTable, sellTable, updateOrder, preSide);    
            App.insertOrder(buyTable, sellTable, updateOrder);      
            App.writeOrdersToCSV(buyTable, "/home/prabhashana/my-app/Data/HP/buyTable.csv");
            App.writeOrdersToCSV(sellTable, "/home/prabhashana/my-app/Data/HP/sellTable.csv");

        });         

    }

    
}
