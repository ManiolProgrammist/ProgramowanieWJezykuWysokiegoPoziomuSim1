package com.zad1;

public class Order {
    Client client=null;
    int numberOfProducts=1;
    long dayOrdered=0;
    boolean orderDone=false;
    public String toString(){
        String ret="order:\n";
        ret+="client age Group: "+client.getAgeGroup()+"\n";
        ret+="number of products ordered: "+numberOfProducts+"\n";
        return ret;
    }
}
