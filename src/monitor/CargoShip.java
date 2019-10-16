package monitor;

import java.util.Map;
import java.util.Scanner;

/**
 * File: CargoShip.java
 * Date: 27 May 2019
 * @author Logan Hershberger
 * Purpose: Object Representation of a Cargo Ship
 */
public class CargoShip extends Ship {
  double cargoValue, cargoVolume, cargoWeight;

  public CargoShip(Scanner sc, Map<Integer,Port> hmp, Map<Integer,Dock> hmd,
          Map<Integer, Ship> hms){
    super(sc,hmp,hmd,hms);
    cargoWeight = Double.parseDouble(sc.next());
    cargoVolume = Double.parseDouble(sc.next());
    cargoValue = Double.parseDouble(sc.next());
  }

  public String toString(){
    String s = String.format("%-20s %-20s %-20s %-12.02f %-12.02f %-12.02f %-12.02f %7.02f %-15s "
                    + "%7.02f %-15s %7.02f %-15s", "Ship: Cargo",
            name, port.name, length, width, weight, draft, cargoWeight,"Cargo Weight",cargoVolume,
            "Cargo Volume",cargoValue, "Cargo Value");
    return s;
  }
}
