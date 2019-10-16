package monitor;

import java.util.Map;
import java.util.Scanner;

/**
 * File: PassengerShip.java
 * Date: 27 May 2019
 * @author Logan Hershberger
 * Purpose: Object Representation of a Passenger Ship
 */
public class PassengerShip extends Ship {
  int numberOfOccupiedRooms, numberOfPassengers, numberOfRooms;

  public PassengerShip(Scanner sc, Map<Integer,Port> hmp, Map<Integer,Dock> hmd,
          Map<Integer, Ship> hms){
    super(sc,hmp,hmd,hms);
    numberOfPassengers = Integer.parseInt(sc.next());
    numberOfRooms = Integer.parseInt(sc.next());
    numberOfOccupiedRooms = Integer.parseInt(sc.next());
  }

  public String toString(){
    String s = String.format("%-20s %-20s %-20s %-12.02f %-12.02f %-12.02f %-12.02f %7d %-15s "
                    + "%7d %-15s %7d %-15s","Ship: Passenger", name, port.name, length, width,
            weight, draft, numberOfPassengers,"Passengers",numberOfRooms, "Total Rooms",
            numberOfOccupiedRooms,"Occupied");
    return s;
  }
}
