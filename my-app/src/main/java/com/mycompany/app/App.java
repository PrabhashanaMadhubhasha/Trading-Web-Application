package com.mycompany.app;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.mongo.MongoClient;

public class App{
    

public static class Order {
    public String orderID;
    public String instrument;
    public String clOrdID;
    public Integer quantity;
    public Integer price;
    public Integer side;
    public String status; 
}

public static class ExecutionRow {
    String tradeID;
    String instrument;
    int quantity;
    double price;
    String buyClOrdID;
    String sellClOrdID;
    String status;
    String buyOrdID;  
    String sellOrdID;  
    String reason;
}

    private static MongoClient mongoClient;

    public static void saveTrader(ExecutionRow executionRow, RoutingContext routingContext ) {
        JsonObject traderJson = App.mapToJsonTrader(executionRow);
        mongoClient.save("MSI_Traders", traderJson, res -> {
            if (res.succeeded()) {
                routingContext.response().end(new JsonObject().put("success", true).encode());
            } else {
                routingContext.response().end(new JsonObject().put("success", false).put("error", res.cause().getMessage()).encode());
            }
        });
    }

    public static int getLastTradeID(List<ExecutionRow> executionTable) {
        if (!executionTable.isEmpty()) {
            ExecutionRow lastRow = executionTable.get(executionTable.size() - 1);
            String tradeID_intValue = lastRow.tradeID.substring(3);
            int intTradeID = Integer.parseInt(tradeID_intValue);
            return intTradeID+1;
        } else {
            return 0; 
        }
    }

    public static Order mapToOrder(JsonObject json) {
        Order order = new Order();
        // order.orderID = json.getString("orderID");
        order.instrument = json.getString("symbol");
        order.clOrdID = json.getString("topic");
        order.quantity = Integer.parseInt(json.getString("description"));
        order.price = Integer.parseInt(json.getString("postCategory"));
        if("BUY".equals(json.getString("side"))){
            order.side = 1;
        }else if("SELL".equals(json.getString("side"))) {
            order.side = 2;
        }
        order.status = "NEW"; 

        return order;
    };

    public static Order mapToDefOrder(JsonObject json) {
        Order order = new Order();
        order.orderID = json.getString("orderID");
        order.instrument = json.getString("symbol");
        order.clOrdID = json.getString("topic");
        order.quantity = Integer.parseInt(json.getString("description"));
        order.price = Integer.parseInt(json.getString("postCategory"));
        System.out.println(json.getString("side"));
        if("BUY".equals(json.getString("side"))){
            order.side = 1;
        }else if("SELL".equals(json.getString("side"))) {
            order.side = 2;
        }
        order.status = json.getString("status"); 

        return order;
    };

    public static JsonObject mapToJsonOrder(Order order) {
        JsonObject json = new JsonObject();
        json.put("orderID", order.orderID);
        json.put("symbol", order.instrument);
        json.put("topic", order.clOrdID);
        json.put("description", String.valueOf(order.quantity));
        json.put("postCategory", String.valueOf(order.price));

        String sideString = "";
        switch (order.side) {
            case 1:
                sideString = "BUY";
                break;
            case 2:
                sideString = "SELL";
                break;
        }
        json.put("side", sideString);

        json.put("status", order.status);

        return json;
    }

    public static JsonObject mapToJsonTrader(ExecutionRow trader) {
        JsonObject json = new JsonObject();
        json.put("tradeID", trader.tradeID);
        json.put("instrument", trader.instrument);
        json.put("quantity", String.valueOf(trader.quantity));
        json.put("price", String.valueOf(trader.price));
        json.put("buyClOrdID", trader.buyClOrdID);
        json.put("sellClOrdID", trader.sellClOrdID);
        json.put("status", trader.status);
        json.put("buyOrdID", trader.buyOrdID);
        json.put("sellOrdID", trader.sellOrdID);
        json.put("reason", trader.reason);

        return json;
    }

    public static int updateOrder(List<Order> orderTable, Order updateOrder) {
        int preSide = 0;
        for (Order order : orderTable) {
            if (order.orderID.equals(updateOrder.orderID)) {
                order.clOrdID = updateOrder.clOrdID;
                order.quantity = updateOrder.quantity;
                order.price = updateOrder.price;
                order.side = updateOrder.side;
                preSide = order.side; 
                break;
            }
            
        }
        return preSide;
        
    };

    public static void cancelOrder(List<Order> orderTable, Order deleteOrder) {
        for (Order order : orderTable) {
            if (order.orderID.equals(deleteOrder.orderID)) {
                order.status = "CANCEL"; 
                break;
            }
            
        }
        
    };    

    public static void deleteOrder(List<Order> buyTable, List<Order> sellTable, Order updateOrder, int preSide) {
        int no_Orders = 0;
        if(preSide == 1){
            for (Order buyOrder : buyTable) {
                if (buyOrder.orderID.equals(updateOrder.orderID)) {
                    buyTable.remove(no_Orders);
                    return; 
            }
            no_Orders++;
        }

        }else if(preSide == 2){        
            for (Order sellOrder : sellTable) {
                if (sellOrder.orderID.equals(updateOrder.orderID)) {
                    sellTable.remove(no_Orders);
                    return; 
            }
            no_Orders++;
        }

        };
        
    };

    

    public static List<Order> readCSVOrders(String filename) {
        List<Order> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine(); 
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                Order row = new Order();
                row.orderID = fields[0];
                row.instrument = fields[1];
                row.clOrdID = fields[2];
                row.quantity = Integer.parseInt(fields[3]);
                row.price = Integer.parseInt(fields[4]);
                row.side = Integer.parseInt(fields[5]);
                row.status = fields[6];
                rows.add(row);
            }
        } catch (IOException e) {
            System.err.println("Error: Could not open the file " + filename);
        }
        return rows;
    }

    public static List<ExecutionRow> readCSVTraders(String filename) {
        List<ExecutionRow> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine(); 
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                ExecutionRow row = new ExecutionRow();
                row.tradeID = fields[0];
                row.instrument = fields[1];
                row.quantity = Integer.parseInt(fields[2]);
                row.price = Double.parseDouble(fields[3]);
                row.buyClOrdID = fields[4];
                row.sellClOrdID = fields[5];
                row.status = fields[6];
                row.buyOrdID = fields[7];
                row.sellOrdID = fields[8];
                row.reason = fields[9];
                rows.add(row);
            }
        } catch (IOException e) {
            System.err.println("Error: Could not open the file " + filename);
        }
        return rows;
    }

    

    public static void compareBuyOrders(List<Order> buyTable) {
        Collections.sort(buyTable, new Comparator<Order>() {
            @Override
            public int compare(Order a, Order b) {
                if (a.price != b.price) {
                    return Double.compare(b.price, a.price);
                } else {
                    return a.orderID.compareTo(b.orderID);
                }
            }
        });
    }

    public static void compareSellOrders(List<Order> sellTable) {
        Collections.sort(sellTable, new Comparator<Order>() {
            @Override
            public int compare(Order a, Order b) {
                if (a.price != b.price) {
                    return Double.compare(a.price, b.price);
                } else {
                    return a.orderID.compareTo(b.orderID);
                }
            }
        });
    }

    public static void insertOrder(List<Order> buyTable, List<Order> sellTable, Order csvRow) {
        switch (csvRow.side) {
            case 1:
                buyTable.add(csvRow);
                compareBuyOrders(buyTable);
                break;
            case 2:
                sellTable.add(csvRow);
                compareSellOrders(sellTable);
                break;
            default:
                System.err.println("Invalid side value: " + csvRow.side);
        }
    }

    public static void deleteAndRearrangeFirstRow(List<Order> table) {
        if (!table.isEmpty()) {
            table.remove(0);
        }
    }

    public static void insertExecutionRow(List<ExecutionRow> executionTable, Order order, int trdIndex, String ClOrdID, String status, String OrdID, String reason) {
        ExecutionRow executionRow = new ExecutionRow();
        executionRow.tradeID = "TRD" + trdIndex;
        executionRow.instrument = order.instrument;
        executionRow.quantity = order.quantity;
        executionRow.price = order.price;
        if (order.side == 1){
            executionRow.buyClOrdID = order.clOrdID;
            executionRow.sellClOrdID = ClOrdID;
        }else{
            executionRow.buyClOrdID = ClOrdID;
            executionRow.sellClOrdID = order.clOrdID;
        }
        executionRow.status = status;
        if (order.side == 1){
            executionRow.buyOrdID = order.orderID;
            executionRow.sellOrdID = OrdID;
        }else{
            executionRow.buyOrdID = OrdID;
            executionRow.sellOrdID = order.orderID;
        }
        executionRow.reason = reason;
        executionTable.add(executionRow);
    }

    public static void writeExecutionToCSV(List<ExecutionRow> executionTable, String filepath) {
        try (FileWriter file = new FileWriter(filepath)) {
            file.write("tradeID,instrument,quantity,price,buyClOrdID,sellClOrdID,Status,buyOrdID,sellOrdID,Reason\n");
            for (ExecutionRow executionRow : executionTable) {
                file.write(executionRow.tradeID + "," + executionRow.instrument + ","
                        + executionRow.quantity + "," + executionRow.price + ","
                        + executionRow.buyClOrdID + "," + executionRow.sellClOrdID + ","
                        + executionRow.status + "," + executionRow.buyOrdID + "," + executionRow.sellOrdID + "," + executionRow.reason + ","
                        + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error: Could not open the file " + filepath);
        }
    }

    public static void writeOrdersToCSV(List<Order> orderTable, String filepath) {
        try (FileWriter file = new FileWriter(filepath)) {
            file.write("OrderID,Instrument,ClOrdID,Quantity,Price,Side,Status\n");
            for (Order orderRow : orderTable) {
                file.write(orderRow.orderID + "," + orderRow.instrument + ","
                        + orderRow.clOrdID + "," + orderRow.quantity + ","
                        + orderRow.price + "," + orderRow.side + ","
                        + orderRow.status + ","
                        + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error: Could not open the file " + filepath);
        }
    }

    public static void updateOrderStatus(List<Order> orderTable, Order currTrade, String Status) {

        String orderIDToChange = currTrade.orderID;
            for (Order Order : orderTable) {
                if (orderIDToChange.equals(Order.orderID)) {
                    Order.status = Status;
                    break; 
                }
            }
        }

    public static int getLastOrderID(List<Order> orderTable) {
        if (!orderTable.isEmpty()) {
            Order lastRow = orderTable.get(orderTable.size() - 1);
            String orderID_intValue = lastRow.orderID.substring(3);
            int intTradeID = Integer.parseInt(orderID_intValue);
            return intTradeID+1;
        } else {
            return 0; 
        }
    }


    public static void OrderExchanger( List<Order> firstOrderTable, Order selectedRow, List<Order> buyTable, List<Order> sellTable, List<ExecutionRow> executionTable) {

        int trdIndex = App.getLastTradeID(executionTable);
        boolean x = false;
        String reason = "";

        if (selectedRow.quantity > 1000 || selectedRow.quantity < 10) {
            reason = "Invalid Quantity";
            x = true;
        } else if (selectedRow.quantity % 10 != 0) {
            reason = "Invalid Quantity";
            x = true;
        } else if (!(selectedRow.price > 0)) {
            reason = "Invalid Price";
            x = true;
        } else if (!("MSI".equals(selectedRow.instrument) || "Asus".equals(selectedRow.instrument) ||
                "Apple".equals(selectedRow.instrument) || "HP".equals(selectedRow.instrument) ||
                "Dell".equals(selectedRow.instrument))) {
            reason = "Invalid Instrument";
            x = true;
        } else if (selectedRow.side != 1 && selectedRow.side != 2) {
            reason = "Invalid Side";
            x = true;
        }


        if (x) {
            App.insertExecutionRow(executionTable, selectedRow, trdIndex, "None", "FAILED", "None", reason);
            // MSIServerRouteVerticle.saveTrader(executionRow,routingContext);
            App.updateOrderStatus( firstOrderTable, selectedRow, "FAILED" );
            trdIndex++;
        } else if (selectedRow.side == 1 && "NEW".equals(selectedRow.status) ) {
            if (!sellTable.isEmpty() && selectedRow.price >= sellTable.get(0).price) {
                int xValue = 1;
                while (!sellTable.isEmpty() && xValue != 0 && selectedRow.price >= sellTable.get(0).price) {
                    int Quantity = selectedRow.quantity;
                    if (Quantity > sellTable.get(0).quantity) {
                        xValue = 1;
                        int newQuantity = selectedRow.quantity - sellTable.get(0).quantity;
                        App.insertExecutionRow(executionTable, sellTable.get(0), trdIndex, selectedRow.clOrdID, "MATCHED", selectedRow.orderID, "None");
                        // MSIServerRouteVerticle.saveTrader(executionRow,routingContext);
                        App.updateOrderStatus( firstOrderTable, sellTable.get(0), "MATCHED");
                        App.updateOrderStatus( firstOrderTable, selectedRow, "PMATCHED");
                        trdIndex++;
                        App.deleteAndRearrangeFirstRow(sellTable);
                        selectedRow.quantity = newQuantity;
                    } else if (Quantity == sellTable.get(0).quantity) {
                        xValue = 0;
                        App.updateOrderStatus( firstOrderTable, selectedRow, "MATCHED" );
                        App.insertExecutionRow(executionTable, sellTable.get(0), trdIndex, selectedRow.clOrdID, "MATCHED", selectedRow.orderID, "None");
                        // MSIServerRouteVerticle.saveTrader(executionRow,routingContext);
                        App.updateOrderStatus( firstOrderTable, sellTable.get(0), "MATCHED" );
                        trdIndex++;
                        App.deleteAndRearrangeFirstRow(sellTable);
                    } else {
                        xValue = 0;
                        int newQuantity = sellTable.get(0).quantity - selectedRow.quantity;
                        App.updateOrderStatus( firstOrderTable, sellTable.get(0), "PMATCHED");
                        App.updateOrderStatus( firstOrderTable, selectedRow, "MATCHED" );
                        sellTable.get(0).quantity = selectedRow.quantity;
                        App.insertExecutionRow(executionTable, sellTable.get(0), trdIndex, selectedRow.clOrdID, "MATCHED", selectedRow.orderID, "None");
                        // MSIServerRouteVerticle.saveTrader(executionRow,routingContext);
                        trdIndex++;
                        sellTable.get(0).quantity = newQuantity;
                        App.insertOrder(buyTable, sellTable, sellTable.get(0));
                        App.deleteAndRearrangeFirstRow(sellTable);
                    }
                }
                if (xValue == 1) {
                    App.insertOrder(buyTable, sellTable, selectedRow);
                }
            } else {
                App.insertOrder(buyTable, sellTable, selectedRow);
            }

        } else if (selectedRow.side == 2 && "NEW".equals(selectedRow.status)) {
            if (!buyTable.isEmpty() && selectedRow.price <= buyTable.get(0).price) {
                int xValue = 1;
                while (!buyTable.isEmpty() && xValue != 0 && selectedRow.price <= buyTable.get(0).price) {
                    int Quantity = selectedRow.quantity;
                    if (Quantity > buyTable.get(0).quantity) {
                        xValue = 1;
                        int newQuantity = selectedRow.quantity - buyTable.get(0).quantity;
                        App.insertExecutionRow(executionTable, buyTable.get(0), trdIndex, selectedRow.clOrdID, "MATCHED", selectedRow.orderID, "None");
                        // MSIServerRouteVerticle.saveTrader(executionRow,routingContext);
                        App.updateOrderStatus( firstOrderTable, buyTable.get(0), "MATCHED" );
                        App.updateOrderStatus( firstOrderTable, selectedRow, "PMATCHED");
                        trdIndex++;
                        App.deleteAndRearrangeFirstRow(buyTable);
                        selectedRow.quantity = newQuantity;
                    } else if (Quantity == buyTable.get(0).quantity) {
                        xValue = 0;
                        App.updateOrderStatus( firstOrderTable, selectedRow, "MATCHED" );
                        App.insertExecutionRow(executionTable, buyTable.get(0), trdIndex, selectedRow.clOrdID, "MATCHED", selectedRow.orderID, "None");
                        // MSIServerRouteVerticle.saveTrader(executionRow,routingContext);
                        App.updateOrderStatus( firstOrderTable, buyTable.get(0), "MATCHED" );
                        trdIndex++;
                        App.deleteAndRearrangeFirstRow(buyTable);
                    } else {
                        xValue = 0;
                        int newQuantity = buyTable.get(0).quantity - selectedRow.quantity;
                        App.updateOrderStatus( firstOrderTable, buyTable.get(0), "PMATCHED" );
                        App.updateOrderStatus( firstOrderTable, selectedRow, "MATCHED" );
                        
                        buyTable.get(0).quantity = selectedRow.quantity;

                        App.insertExecutionRow(executionTable, buyTable.get(0), trdIndex, selectedRow.clOrdID, "MATCHED", selectedRow.orderID, "None");
                        // MSIServerRouteVerticle.saveTrader(executionRow,routingContext);
                        trdIndex++;
                        
                        // selectedRow.price = price;
                        buyTable.get(0).quantity = newQuantity;
                        App.insertOrder(buyTable, sellTable, buyTable.get(0));
                        App.deleteAndRearrangeFirstRow(buyTable);
                    }
                }
                if (xValue == 1) {
                    App.insertOrder(buyTable, sellTable, selectedRow);
                }
            } else {
                App.insertOrder(buyTable, sellTable, selectedRow);
            }
        }

   
}

    

}
