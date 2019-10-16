package monitor;

import java.util.Map;
import java.util.Scanner;

/**
 * File: Dock.java
 * Date: 27 May 2019
 * @author Logan Hershberger
 * Purpose: Object Representation of a Dock at a sea port
 */
public class Dock extends Thing {
  Ship ship;
  Port port;

  public Dock(Scanner sc, Map<Integer, Port> hmp, Map<Integer, Dock> hmd){
    super(sc);
    hmd.put(sc.nextInt(),this);
    port = hmp.get(sc.nextInt());
    port.docks.add(this);
  }

  public String toString(){
    return String.format("%-20s %-20s %-20s", "Dock:", name, port.name);
  }

  public Ship getShip(){
    if(ship == null){
      return null;
    }
    return ship;
  }

  // tell ship to dock and update ship reference
  void assignShip(Ship s){
    s.dock(this);
    ship = s;
  }

  // clears ship reference and notifies port that its ship has departed
  void notifyOfDeparture(Ship s){
    this.ship = null;
    synchronized (port){
      port.notifyOfDeparture(this,s);
    }
  }
}
