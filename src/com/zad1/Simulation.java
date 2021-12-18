package com.zad1;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//Simulation by Patryk Trzeciak
public class Simulation {
    public static CyclicBarrier myCyclicDayBarier;
    public static CyclicBarrier myCyclicMonthBarier;
    ArrayList<Client> clientData;
    ArrayList<Product> products;
    ArrayList<IShop> shops;
    int ageGroupCount;
    int ageMin = 20;
    int ageMax = 100;
    int betweenAge = 10;

    int months = 12;
    Random random;
    SimulationManager simuManager;

    Simulation() {
        this.random = new Random();
        this.ageGroupCount = (ageMax - ageMin) / betweenAge;
        simuManager = new SimulationManager();
        //SIMULATION DATA
        //1000 clients
        //3 shops
        //12 months

        //SHOPS
        //8 products from low to high price (80-2000)
        //prepare products
        this.products = prepareProductData();
        //prepare client data
        this.clientData = prepareClientData(65, 20, 100);

        this.shops = new ArrayList<IShop>();

        this.shops.add(new Shop1( this.products));
        this.shops.add(new Shop2(this.products));
        this.shops.add(new Shop3(this.products));
        this.simuManager.setShopList(shops);

    }

    void startSimulation() {
        //prepare barier
        myCyclicDayBarier = new CyclicBarrier(shops.size());
        myCyclicMonthBarier = new CyclicBarrier(shops.size());
        //day 0
        boolean day0Flag = true;
        //set time (day) that client will do first purchase
        simuManager.setDaysOfBuyForClients(clientData);
        //day0

        //basic lambda
        clientData.forEach(client->{
            IShop chosenShop = this.simuManager.getShopChosenByCustomer(client, null);
            client.setChoosenShop(chosenShop);
        });

        ArrayList<Thread> threadArray = new ArrayList<Thread>();
        final ExecutorService exService = Executors.newFixedThreadPool(shops.size());

        for (var shop : shops) {
            Thread t = new Thread(new Runnable() {
                private IShop myShop;
                private SimulationManager simuManager;
                private int months;
                ArrayList<Client> allClients;
                public Runnable init(IShop myParam, SimulationManager simuMan, ArrayList<Client> allClients, int months) {
                    this.myShop = myParam;
                    this.simuManager = simuMan;
                    this.months = months;
                    this.allClients=allClients;
                    return this;
                }

                @Override
                public void run() {
                    for (int month = 0; month < this.months; month++) {
                    //    System.out.println("_______"+this.myShop.getName()+"_____month start:" + month+"___________________________________________________________________________");

                        var currentMonth = month;
                        var currentDay = (month) * 31;
                        //add margin, pay off loan, fill shop
                        this.myShop.processMonthlyActivities();
                        var clientsAfterQueue = this.myShop.sellToClientsInQueue(currentDay);
                        //set buy day for clients after queue
                        simuManager.setDaysOfBuyForClients(clientsAfterQueue);
                       //set clients that was from queue
                        clientsAfterQueue.forEach(client -> {
                            IShop chosenShop = this.simuManager.getShopChosenByCustomer(client, this.myShop);
                            client.setChoosenShop(chosenShop);
                        });



                        for (int day = 0; day < 31; day++) {
                            this.simuManager.buyItemsInADay(this.myShop, currentDay + day, allClients);
                            try {

                                //wait for rest of shops to end simulation for this day
                                myCyclicDayBarier.await();
                            } catch (InterruptedException | BrokenBarrierException e) {
                                e.printStackTrace();
                            }
                        }



                        try {

                            //wait for rest of shops to end simulation for this month
                            myCyclicMonthBarier.await();
                           // System.out.println("_______"+this.myShop.getName()+"____month end: " + month + "_________________________________________");
                            System.out.println("\n\n shop overview on month "+month+"\n"+this.myShop.getOverview()+"_________________________________________\n\n");
                        } catch (InterruptedException | BrokenBarrierException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }.init(shop, this.simuManager,this.clientData, this.months));
            threadArray.add(t);

        }

        for (var thr : threadArray) {
            exService.execute(thr);
        }
        exService.shutdown();
    }


    ArrayList<Product> prepareProductData() {
        var products = new ArrayList<Product>();
        int addPrice = (2000 - 80) / 7;
        products.add(new Product(1, 80d));
        for (int indx = 1;indx < 7; indx++) {
            products.add(new Product(indx + 1, 80 + addPrice * indx));
        }
        products.add(new Product(8, 2000));
//        for (var pr : products) {
//            System.out.println("price of product: " + String.valueOf(pr.price));
//        }
        return products;
    }


    ArrayList<Client> prepareClientData() {
        return prepareClientData(65, 20, 100);
    }

    ArrayList<Client> prepareClientData(int averageAge, int minAge, int maxAge) {
        return this.prepareClientData(averageAge, minAge, maxAge, 1000);
    }

    //   [min,max) ->
    ArrayList<Client> prepareClientData(int averageAge, int minAge, int maxAge, int clientCount) {
        ArrayList<Client> ret = new ArrayList<Client>();
        while (ret.size() < clientCount) {
            int age = getRandomAge(averageAge);
            if (age >= minAge && age < maxAge) {
                var client = new Client();
                client.setAge(age);
                ret.add(client);
                //   System.out.println("Age of client: " + String.valueOf(age) + " age group: " + String.valueOf(client.getAgeGroup()));

            } else {
                //  System.out.println("age over min and max: " + String.valueOf(age));
            }
        }
        return ret;
    }

    int getRandomAge(int average) {
        Random r = new Random();
        double myG = r.nextGaussian() * 20 + average;
        return (int) myG;
    }


}

