package com.zad1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Shop implements IShop {

    //semaphore for rating
    private static final int MAX_PERMIT = 1;
    private final Semaphore ratingSemaphore = new Semaphore(MAX_PERMIT, true);

    //margin for shop on product
    double margin = 0.2d;
    //loan margin rate
    double loanMargin = 0.02d;
    // current cash (got from selling products)
    double currentCash = 0.0d;
    // current loan
    double loan = 0.0d;
    //sale margin for shop on product
    double saleMargin = 0.02d;
    //minimum of products / basic count for products
    int basicProductCount;
    //multiplier for
    // Sklep1 sprowadza duże ilości towaru, więc jest obciążony
    //dużym kosztem kredytu. Sklep3 sprowadza małe ilości towarów, sklep2 średnie
    int importedProductNumberMultiplier = 1;
    String name;
    ArrayList<Product> products;
    ArrayList<ProductsDataForShop> productsData;
    //clients ratings for this shop
    Map<Client, Integer> clientRatings;
    int ordersDone=0;
    int ordersDoneInMonth=0;
    //imported product from magazine: -> product (productTypeCount -id)*importedProductNumberMultiplier*basicProductCount
    Shop(String name, ArrayList<Product> products, int importedProductNumberMultiplier, int basicProductCount) {
        this.importedProductNumberMultiplier = importedProductNumberMultiplier;
        this.basicProductCount = basicProductCount;
        this.products = products;
        this.name = name;
        //map of ratings from clients
        this.clientRatings = new HashMap<Client, Integer>();
        this.productsData = new ArrayList<ProductsDataForShop>();
        for (var prod : products) {
            var productData = new ProductsDataForShop();
            productData.product = prod;
            productData.margin = margin;
            productData.saleMargin = saleMargin;
            this.productsData.add(productData);
        }
    }

    Shop(String name, ArrayList<Product> products, int importedProductNumberMultiplier) {
        this(name, products, importedProductNumberMultiplier, 5);
    }


    @Override
    public void processMonthlyActivities() {
        this.addLoanMargin();
        this.paidOffLoan();
        this.fillProducts();
    }

    void paidOffLoan() {
        //loan bigger than current cash
        if (loan >= currentCash) {
            this.loan = this.roundMoney(this.loan - currentCash);

            this.currentCash = 0.0d;
        } else {
            //we have money to paid of entire loan
            this.currentCash = this.roundMoney(this.currentCash - this.loan);
            this.loan = 0.0d;

        }
    }

    void fillProducts() {
        double nowLoan = 0;
        for (var productD : productsData) {
            int buyNumber = (productsData.size() + 1 - productD.product.id) * importedProductNumberMultiplier * basicProductCount;
            if (buyNumber <= productD.numberOfProductInMagazine) {
                productD.isOnSale = true;
            } else {
                productD.isOnSale = false;
            }
            productD.numberOfProductInMagazine += buyNumber;
        //    System.out.println("product with price "+productD.product.price +" filled with "+buyNumber);
            nowLoan += this.roundMoney(buyNumber * productD.product.price);
        }
        loan += nowLoan;
    }

    double addLoanMargin() {
        loan = this.roundMoney(loan + loan * loanMargin);
        return loan;
    }

    @Override
    public double getAverageRating() {
        double ret = 5;
        try {
            ratingSemaphore.acquire();
//            System.out.println("acquire from shop " + this.name);
            if (clientRatings.size() > 0) {
                ret = 0;
                for (var rating : clientRatings.values()) {
                    ret += rating;
                }
                ret=roundMoney(ret/clientRatings.size());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            System.out.println("release in shop " + this.name);

            ratingSemaphore.release();
        }

        return ret;

    }

    @Override
    public int getRatingByClient(Client client) {
        int ret = 5;
        try {
            ratingSemaphore.acquire();
//            System.out.println("acquire from shop get rating " + this.name);
            if (clientRatings.containsKey(client)) {
                ret = clientRatings.get(client);
            } else {
                ret = -1;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            System.out.println("release from shop get rating " + this.name);

            ratingSemaphore.release();
        }

        return ret;
    }

    @Override
    public void putRatingByClient(Client client, int rating) {
        try {
            ratingSemaphore.acquire();
            this.clientRatings.put(client, rating);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            System.out.println("release setClientRating shop get rating " + this.name);

            ratingSemaphore.release();
        }
    }

    //return all the clients that bought something in this queue
    @Override
    public ArrayList<Client> sellToClientsInQueue(long currentDay) {
        ArrayList<Client> clientsThatBought = new ArrayList<Client>();
        for (var idx = 0; idx < this.productsData.size(); idx++) {

            ProductsDataForShop prodData = this.productsData.get(idx);
            var orderArray = prodData.getClientQueue();
            int currentSize = orderArray.size();
            for (var orderIdx = 0; orderIdx < currentSize; orderIdx++) {
                Order order = orderArray.remove(0);
                //if order not done, buyProduct adds it to queue
                currentCash += this.roundMoney( prodData.buyProduct(order));
                if (order.orderDone) {
                    prodData.doneOrders++;

                    //if more than month
                    if( currentDay-order.client.buyDay>30){
                        this.putRatingByClient(order.client, 1);

                    }else{
                        //less than month but still in queue
                        this.putRatingByClient(order.client, 3);

                    }
                    order.client.buyDay = currentDay;
                    clientsThatBought.add(order.client);
                }
            }

        }
        return clientsThatBought;
    }

    @Override
    //true -> client bought, false -> added to queue
    public boolean buyItemsByClient(Client client, int productBuyCount, long currentDay) {
        boolean ret = false;
        ProductsDataForShop productData = getProductDataForClient(client);
        if (productData != null) {
            Order order = new Order();
            order.client = client;
            order.dayOrdered = currentDay;
            order.numberOfProducts = productBuyCount;
            //if order not done, buyProducts add it to queue
            currentCash +=this.roundMoney( productData.buyProduct(order));
            if (order.orderDone) {
                ret = true;
                order.client.buyDay = currentDay;
                this.putRatingByClient(order.client, 5);

            }
        }
        return ret;
    }

    ProductsDataForShop getProductData(Product product) {
        ProductsDataForShop ret = null;
        for (var indx = 0; indx > this.productsData.size(); indx++) {
            if (this.productsData.get(indx).product == product) {
                ret = this.productsData.get(indx);
                break;
            }
        }
        return ret;

    }

    //clients buy specific product by his age
    public ProductsDataForShop getProductDataForClient(Client client) {
        ProductsDataForShop ret = null;
        int ageGroup = client.getAgeGroup();
        if (ageGroup < this.productsData.size()) {
            ret = this.productsData.get(ageGroup);
        }
        return ret;
    }

    @Override
    public int clientRatingsCount() {
        return this.clientRatings.size();
    }


    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getOverview() {
        String ret = "*______name:" + this.name + "______\n";
        ret += "current loan: " + loan + "\n";
        ret += "current cash: " + this.roundMoney(currentCash) + "\n";
        ret += "current rating: " + this.getAverageRating() + " by "+ this.clientRatingsCount()+" clients\n";
        ret +="*_______Products magazine overview:______"+"\n";
        int allOrdersInQueue=0;

        for(var indx=0;indx<this.productsData.size();indx++){
            ret+="product:"+indx+"\n";
            ret+=productsData.get(indx).toString();
            allOrdersInQueue+=this.productsData.get(indx).getClientQueue().size();
        }
        ret+="all clients in queue for this shop: "+allOrdersInQueue+"\n";
        return ret;
    }

    double roundMoney(double input) {
        return Math.round(input * 100.0) / 100.0;
    }
}
