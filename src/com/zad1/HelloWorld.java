//notes:
//Ctrl+Alt+L -> formating code
// modifier     class   package     subclass    world
// public       Y       Y           Y           Y
// protected    Y       Y           Y           N
// no modifier  Y       Y           N           N
// private      Y       N           N           N
//toString() method -> print textual representation of object
//this class belong to this package
package com.zad1;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class HelloWorld {
    //Simulation of shops/clients in 12 months by Patryk Trzeciak
    public static void main(String[] args) {
        Simulation sim=new Simulation();
        sim.startSimulation();
    }
}
