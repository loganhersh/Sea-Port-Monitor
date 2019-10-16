package monitor;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * File: CargoShip.java
 * Date: 27 May 2019
 * @author Logan Hershberger
 * Purpose: Object Representation of a Ship
 */
public class Ship extends Thing implements Comparable<Thing>{
  double draft, length, weight, width;
  ArrayList<Job> jobs = new ArrayList<>();
  CopyOnWriteArrayList<Person> currentWorkers = new CopyOnWriteArrayList<>();
//  ArrayList<Person> currentWorkers = new ArrayList<>();
  Dock dock;
  Port port;

  public Ship(Scanner sc, Map<Integer,Port> hmp, Map<Integer,Dock> hmd,
          Map<Integer, Ship> hms){
    super(sc);
    hms.put(sc.nextInt(), this);

    int parent = sc.nextInt();
    dock = hmd.get(parent);
    if(dock != null){
      dock.ship = this;
      port = dock.port;
    } else {
      port = hmp.get(parent);
      port.queue.add(this);
    }
    port.ships.add(this);

    weight = Double.parseDouble(sc.next());
    length = Double.parseDouble(sc.next());
    width = Double.parseDouble(sc.next());
    draft = Double.parseDouble(sc.next());
  }

  public String toString(){
    return String.format("%-20s %-20s", "Ship:", name);
  }

  public double getLength(){
    return length;
  }

  public double getWidth(){
    return width;
  }

  public double getWeight(){
    return weight;
  }

  public double getDraft(){
    return draft;
  }

  public String getName(){
    return name;
  }

  // Removes provided Job from jobs list and checks if that was the last job.
  // Departs port if no more jobs
  synchronized void removeJob(Job job){
    jobs.remove(job);

    for (Person p : currentWorkers) {
      if (p.currentjob == job) {
        port.returnWorker(p);
      }
    }

    if(jobs.isEmpty()){
      leavePort();
    }
  }

  boolean initiateJob(Job job){
    // initiate the job if it has no requirements
    if(job.requirements.isEmpty()){
      return true;
    }
    return port.requestWorkers(this, job);
  }

  void addWorkers(ArrayList<Person> workers){
    currentWorkers.addAll(workers);
  }

  // notifies dock of departure and clears dock and port references.
  void leavePort(){
    if(dock != null) {
      dock.notifyOfDeparture(this);
    }
    dock = null;
    port = null;
  }

  // updates dock reference and notifies ship's jobs to begin progressing
  synchronized void dock(Dock d){
    this.dock = d;
    notifyAll();
  }
}
