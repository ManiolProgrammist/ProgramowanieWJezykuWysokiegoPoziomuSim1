package com.zad1;

import java.util.ArrayList;
import java.util.Map;

public interface IShop {
    void processMonthlyActivities();

    double getAverageRating();

    //return all the clients that bought something in this queue
    ArrayList<Client> sellToClientsInQueue(long currentDay);

    //true -> client bought, false -> added to queue
    boolean buyItemsByClient(Client client, int productBuyCount, long currentDay);

    ProductsDataForShop getProductDataForClient(Client client);
    int clientRatingsCount();
    int getRatingByClient(Client client);
    void putRatingByClient(Client client, int rating);

    String getName();

    String getOverview();
}
