package com.mycompany.app;

import java.util.List;

import com.mycompany.app.App.ExecutionRow;
import com.mycompany.app.App.Order;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;

public class ServerRouteVerticle extends AbstractVerticle {

    private static MongoClient mongoClient;
    @Override
    public void start() {
        JsonObject mongoConfig = new JsonObject()
            .put("connection_string", "mongodb://localhost:27017")
            .put("db_name", "Trading");

        // Create a MongoClient
        mongoClient = MongoClient.createShared(vertx, mongoConfig);

        // Create HTTP server and router
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        // Define routes
        router.post("/post/save").handler(this::handleSavePost);
        router.post("/register").handler(this::handleSaveUser);
        // router.post("MSI/trader/save").handler(this::handleSaveTraders);
        router.get("/posts").handler(this::handleGetPosts); 
        router.get("/traders").handler(this::handleGetTraders);
        router.get("/post/:id").handler(this::handleGetPost);
        router.put("/post/update/:id").handler(this::handleUpdatePost);
        router.route("/post/delete/:id").handler(this::handleDeletePost);

        // Bind the router to the server
        server.requestHandler(router);

        // Listen on port 8000
        server.listen(8000);
    }

   

    private void updateOrderStatusInMongo(List<Order> orderList, String symbol) {


    // Iterate through the list of orders
    for (Order updatedOrder : orderList) {
        String orderID = updatedOrder.orderID;
        String newStatus = updatedOrder.status;

        // Use MongoDB query to find the order by order ID
        JsonObject query = new JsonObject().put("orderID", orderID);

        // Use MongoDB update operation to set the new status
        JsonObject update = new JsonObject().put("$set", new JsonObject().put("status", newStatus));

        mongoClient.updateCollection(symbol+"_Orders", query, update, ar -> {
            if (ar.succeeded()) {
                System.out.println("Order status updated successfully for order ID: " + orderID + " with " + newStatus);
            } else {
                System.err.println("Failed to update order status for order ID: " + orderID);
                ar.cause().printStackTrace();
            }
        });
    }

    }

    private void handleSavePost(RoutingContext routingContext) {
        routingContext.request().bodyHandler(body -> {
            JsonObject data = body.toJsonObject();
            Order newOrder = App.mapToOrder(data);
            vertx.eventBus().send(newOrder.instrument, newOrder);
            vertx.eventBus().<MyData>consumer("Saving_Trading", message -> {
            MyData myData = message.body();
            Order order = myData.Order;
            List<Order> orderTable = myData.OrderTable;
            List<ExecutionRow> executionTable = myData.Traders;
            String CollectionName = myData.CollectionName;
            int no_Traders_Before = myData.no_Traders_Before;
            JsonObject orderJson = App.mapToJsonOrder(order);
            saveOrder(orderJson, routingContext, CollectionName);
            int no_Traders_After = executionTable.size();
            List<ExecutionRow> lastTradings = executionTable.subList(no_Traders_Before, no_Traders_After);
            for (ExecutionRow executionRow : lastTradings) {
                saveTrader(executionRow,routingContext,CollectionName);
            }

            updateOrderStatusInMongo(orderTable,newOrder.instrument);

            });
            
        });
    }

    private void handleSaveUser(RoutingContext routingContext) {
        routingContext.request().bodyHandler(body -> {
            JsonObject user = body.toJsonObject();
            saveOrder(user, routingContext, "User");

            
        });
    }

    private void saveOrder(JsonObject data, RoutingContext routingContext, String CollectionName) {
        mongoClient.save(CollectionName+"_Orders", data, res -> {
            if (res.succeeded()) {
                routingContext.response().end(new JsonObject().put("success", true).encode());
            } else {
                routingContext.response().end(new JsonObject().put("success", false).put("error", res.cause().getMessage()).encode());
            }
        });
    }



    public static void saveTrader(ExecutionRow executionRow, RoutingContext routingContext, String CollectionName ) {
        JsonObject traderJson = App.mapToJsonTrader(executionRow);
        mongoClient.save(CollectionName+"_Traders", traderJson, res -> {
            if (res.succeeded()) {
                routingContext.response().end(new JsonObject().put("success", true).encode());
            } else {
                routingContext.response().end(new JsonObject().put("success", false).put("error", res.cause().getMessage()).encode());
            }
        });
    }





    private void handleGetPosts(RoutingContext routingContext) {
        String symbol = routingContext.request().getParam("symbol");
        mongoClient.find(symbol+"_Orders", new JsonObject(), result -> {
            if (result.succeeded()) {
                List<JsonObject> posts = result.result();
                routingContext.response().end(new JsonObject().put("success", true).put("existingPosts", posts).encode());
            } else {
                routingContext.response().end(new JsonObject().put("success", false).put("error", result.cause().getMessage()).encode());
            }
        });
    }

    private void handleGetTraders(RoutingContext routingContext) {
        String symbol = routingContext.request().getParam("symbol");
        mongoClient.find(symbol+"_Traders", new JsonObject(), result -> {
            if (result.succeeded()) {
                List<JsonObject> posts = result.result();
                routingContext.response().end(new JsonObject().put("success", true).put("existingPosts", posts).encode());
            } else {
                routingContext.response().end(new JsonObject().put("success", false).put("error", result.cause().getMessage()).encode());
            }
        });
    }
    

    private void handleGetPost(RoutingContext routingContext) {
        String symbol = routingContext.request().getParam("symbol"); 
        String id = routingContext.request().getParam("id"); 
        JsonObject query = new JsonObject().put("_id", id);
        mongoClient.findOne(symbol+"_Orders", query, new JsonObject(), result -> {
                if (result.succeeded()) {
                    JsonObject post = result.result();
                    if (post != null) {
                        routingContext.response().end(new JsonObject().put("success", true).put("post", post).encode());
                    } else {
                        routingContext.response().setStatusCode(404).end(new JsonObject().put("success", false).put("error", "Post not found").encode());
                    }
                } else {
                    routingContext.response().setStatusCode(500).end(new JsonObject().put("success", false).put("error", result.cause().getMessage()).encode());
                }
            });
    }


    private void handleUpdatePost(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id"); 
        routingContext.request().bodyHandler(body -> {
        JsonObject updateData = body.toJsonObject();
        Order upOrderData = App.mapToDefOrder(updateData);

        if ("NEW".equals(upOrderData.status)){
        vertx.eventBus().send(upOrderData.instrument+"_Update", upOrderData);
        JsonObject query = new JsonObject().put("_id", id);
        updatePost(query,updateData, routingContext);
        }else{
            routingContext.response().end(new JsonObject().put("unsuccess", "Cannot Update").encode());
        };
            
        });

    }

    private void updatePost(JsonObject query, JsonObject updateData, RoutingContext routingContext) {
        String symbol = routingContext.request().getParam("symbol"); 
        mongoClient.updateCollection(symbol+"_Orders", query, new JsonObject().put("$set", updateData), res -> {
        if (res.succeeded()) {
            routingContext.response().end(new JsonObject().put("success", "Update Successfully").encode());
        } else {
            routingContext.response().setStatusCode(500).end(new JsonObject().put("error", res.cause().getMessage()).encode());
        }
    });
    }

    private void handleDeletePost(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id"); 
        routingContext.request().bodyHandler(body -> {
        JsonObject deleteData = body.toJsonObject();
        Order delOrderData = App.mapToDefOrder(deleteData);

        vertx.eventBus().send(delOrderData.instrument+"_Delete", delOrderData);
        JsonObject query = new JsonObject().put("_id", id);
        deleteData.put("status", "CANCEL");
        updatePost(query, deleteData, routingContext);
            
        });

    }

    // private void handleDeletePost(RoutingContext routingContext) { 
    //     String id = routingContext.request().getParam("id");
    //     String orderID = routingContext.request().getParam("orderID");
    //     String symbol = routingContext.request().getParam("symbol");
    //     JsonObject query = new JsonObject().put("_id", id);
    //     vertx.eventBus().send(symbol+"_Update", orderID);
    //     mongoClient.findOneAndDelete(symbol+"_Orders", query, res -> {
    //     if (res.succeeded()) {
    //         routingContext.response().end(new JsonObject().put("success", "Delete Successfully").encode());
    //     } else {
    //         routingContext.response().setStatusCode(500).end(new JsonObject().put("error", res.cause().getMessage()).encode());
    //     }
    // });
    // }

}
