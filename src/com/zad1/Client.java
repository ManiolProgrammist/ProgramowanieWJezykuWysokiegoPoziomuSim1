package com.zad1;

public class Client
{

    Client(){
        this.age=20;
    }
    Client(int age){
        this.age=age;
    }
    private int age;
    private int ageGroup;
    long buyDay=0;
    IShop choosenShop=null;
    //expected from 0 to 7
    private void refreshAgeGroup(){
        int ageGr=0;
        if(age>=20&&age<100) {
            this.ageGroup= (age / 10)-2;
        }else {
            this.ageGroup = 0;
        }
    }
    public int getAgeGroup(){
        return this.ageGroup;
    }

    public void setAge(int age){
        this.age=age;
        refreshAgeGroup();
    }
    public int getPointsByAge(){
        var ret= this.getAgeGroup()+1;
        return ret;
    }
    public void setChoosenShop(IShop shop){
        this.choosenShop=shop;

    }
}
