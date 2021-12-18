package com.zad1;

public class Product {

    Product(int id,double price){
       this(id,price,"Default");
    }
    Product(int id,double price,String name){
        this.id=id;
        this.price=price;
        this.name=name;
    }
    public int id;
    public double price;
    public String name="default";
}
