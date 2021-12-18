package com.zad1;

import java.util.ArrayList;

public class ProductsDataForShop {
    public Product product;
    //if number of products in magazine is 0 and client try to buy this products from shop, we add him to clientQueue
    public int numberOfProductInMagazine = 0;
    boolean isOnSale = false;
    double margin=0.0d;
    double saleMargin=0.0d;
    //at start of the month first we sell clients that are in queue
    public ArrayList<Order> orderQueue;
    int doneOrders=0;
    public ProductsDataForShop() {
        this.orderQueue = new ArrayList<Order>();
    }

    public ArrayList<Order> getClientQueue() {
        return orderQueue;
    }


    //return how much client paid for order
    //if order is not done, add it to queue
    public double buyProduct(Order order) {
        double ret = 0;
        double currentMargin=isOnSale? saleMargin:margin;
        if (numberOfProductInMagazine > 0) {

            if (numberOfProductInMagazine > order.numberOfProducts) {
                numberOfProductInMagazine -= order.numberOfProducts;
                ret = order.numberOfProducts * this.product.price;
                order.orderDone=true;
                doneOrders++;
            } else {
                var numberOfProductsNotBoughtByClient = order.numberOfProducts - numberOfProductInMagazine;
                ret = numberOfProductInMagazine * this.product.price;

                numberOfProductInMagazine = 0;
                order.numberOfProducts = numberOfProductsNotBoughtByClient;
                this.orderQueue.add(order);

            }
        } else {

            this.orderQueue.add(order);
        }
        //add margin price
        ret=ret+ret*currentMargin;
        return ret;
    }

    public String toString(){
        String ret="";
        //ret+=product.name+"\n";
        ret+="Product price: "+product.price+"\n";
        ret+="Number of products in magazine: "+numberOfProductInMagazine+"\n";
        ret+="Number of orders in queue: "+this.orderQueue.size() +"\n";
        ret+="Is on sale: "+ (this.isOnSale?"true ": "false ")+"\n";
//        ret+="Not done order descriptions: \n";
//        for(var indx=0;indx<this.orderQueue.size();indx++){
//            ret+=orderQueue.get(indx).toString();
//        }
//        ret+="\n";
        return ret;
    }

}
