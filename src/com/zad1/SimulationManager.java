package com.zad1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SimulationManager {
    //lambda for timer, the older client, the bigger is lambda
    int timerLambdaMax = 60;
    int timerLambdaMin = 20;
    //age groups count
    int ageGroupCount = 8;
    //start points of chance for all shops
    int defaultChancePoints = 5;

    Random random;
    ArrayList<IShop> shopList;


    SimulationManager(int ageGroupCount) {
        this.ageGroupCount = ageGroupCount;
        this.random = new Random();
        this.shopList=new ArrayList<IShop>();
    }

    SimulationManager() {
        this(8);
    }


    void setShopList(ArrayList<IShop> shopList){
        this.shopList=shopList;
    }

    IShop getShopChosenByCustomer(Client client, IShop lastShop) {
        IShop ret = null;
        ArrayList<Integer> pointsForShop = new ArrayList<Integer>();
        //clientsForShop -> keys are our shops so we cant iterate through keys
        shopList.forEach(shop->{
            pointsForShop.add(checkPointsForShop(shop, client, lastShop));

        });
        int sum = 0;
        for (Integer points : pointsForShop) {
            sum += points;
        }
        //if sum =0 client do not want to go to any shop!
        if(sum!=0) {
            int random = (int) (Math.random() * sum);
            //shop1
            int shopIndx = 0;
            int pointsSumRan = 0;
            //find out what shop win
            for (Integer points : pointsForShop) {
                pointsSumRan += points;
                if (random < pointsSumRan) {
                    break;
                } else {
                    shopIndx++;
                }
            }
            int indx = 0;
            //get win shop
            for (var shop : shopList) {
                if (shopIndx == indx) {
                    ret = shop;
                    break;
                }
                indx++;
            }
        }
        return ret;
    }

    //    wybór sklepu
    //    uzależniony jest od:
    //  - aktualnej średniej oceny sklepu przez klientów
    //  - stopnia zadowolenia z poprzednich zakupów dokonywanych przez klienta w określonych
    //    sklepach (np. ocena = 1 oznacza, że kolejny zakup na pewno nie będzie wykonany w tym   samym sklepie)
    //  - ceny (promocje cenowe)
    //  - wieku (klienci młodsi w większym stopniu kierują się ocenami sklepów, oraz są bardziej
    //we point each shop then we use random to find out what shop will client choose
    //get "points" for going to this shop
    int checkPointsForShop(IShop shop, Client client, IShop lastShop) {
        int ret = this.defaultChancePoints;
        var productData = shop.getProductDataForClient(client);
        //if product is on sale, client want this product
        if (true == productData.isOnSale) {
            ret += 5;
        }

        //if current check shop is last shop
        //clients do not want to change shops -> by age, the older the less likely to change shop
        if (lastShop == shop) {
            //get points by age -> from 1 points
            ret += client.getPointsByAge();
        }

        var clientRatingsSize = shop.clientRatingsCount();
        if (clientRatingsSize> 0) {
            //averange rating 1-5 -> get averange rating and subtract 3

            //younger clients pays more attention of rating
            var rating = (shop.getAverageRating() - 2.5) * (ageGroupCount + 1- client.getPointsByAge());
            ret += rating;

            //check if this client already rated this shop
            int ratingByClient=shop.getRatingByClient(client);
            //-1 if client did not rated this shop yet
            if (ratingByClient!=-1) {
                if (ratingByClient >1) {
                    ret += ratingByClient;
                } else {
                    //if client gave rating 1 we are sure he will not go to this shop
                    ret = 0;
                }
            }
        }
        //cant have less than 0 points
        if (ret < 0) {
            ret = 0;
        }
        return ret;

    }

    //buy items by clients from a shop at set day.
    void buyItemsInADay(IShop shop, int day, ArrayList<Client> clients) {
        var clientsSize=clients.size();
        for(int indx=0;indx<clientsSize;indx++){
            var client=clients.get(indx);
            if ( (client.buyDay == day)&& (client.choosenShop==shop)) {
                int countBuyProduct = getQuantityOfPurchased(client);
                //buy items
                if (shop.buyItemsByClient(client, countBuyProduct, day)) {
                    //if client managed to buy all items
                    //check next buy day
                    //next buyDay
                    var nextBuyDay = getDayOfBuyForClient(client);
                    if (nextBuyDay == 0) {
                        nextBuyDay = 1;
                    }
                    client.buyDay = client.buyDay + nextBuyDay;
                    var shopForClient = this.getShopChosenByCustomer(client, shop);
                    client.choosenShop=shopForClient;
                }
            }
        }

    }

    void setDaysOfBuyForClients(ArrayList<Client> clients) {

        if(clients!=null) {
            clients.forEach(client -> {
                int buyDay = getDayOfBuyForClient(client);
                if (buyDay == 0 && client.buyDay != 0) {
                    buyDay = 1;
                }
                client.buyDay = client.buyDay + buyDay;
            });
        }

    }

    int getDayOfBuyForClient(Client client) {
        var lambda = this.getLambdaForTimer(client);
        return (int) this.getBuyTimer(random, lambda);
    }

    //lambda for setting purchase day
    double getLambdaForTimer(Client client) {
        var addByGroup = (double) ((double) (timerLambdaMax - timerLambdaMin) / (double) (ageGroupCount - 1));

        var ret = timerLambdaMin + client.getAgeGroup() * addByGroup;
        //round to 2 places after
        ret = ret * 100;
        ret = Math.round(ret);
        ret = ret / 100;
        //    System.out.println("for age group " + String.valueOf(client.getAgeGroup()) + " lambda timer is " + ret);

        return ret;
    }

    //for setting buy Day
    double getBuyTimer(Random rand, double lambda) {
        return -lambda * Math.log(1 - rand.nextDouble());
    }

    // retirm quantity of goods for client
    int getQuantityOfPurchased(Client client) {
        double lambdaPoison = getLambdaForPoisson(client);
        int quantity = this.getPoisson(random, lambdaPoison);
        return quantity;
    }

    //get lambda for poision (quantity of purchased goods)
    double getLambdaForPoisson(Client client) {
        int max = 15;
        int min = 4;
        //8 is count of age groups, but we do not count first "min" age group because it is 7
        double difference = (max - min) / 7;

        double lambda = min + (8 - client.getAgeGroup()) * difference;
        return lambda;
    }

    //get number of product by poision
    int getPoisson(Random rand, double lambda) {
        double l = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;
        do {
            k++;
            p *= rand.nextDouble();
        } while (p > l);
        if (k <= 1) {
            return getPoisson(rand, lambda);
        } else {
            return k - 1;
        }

    }
}
