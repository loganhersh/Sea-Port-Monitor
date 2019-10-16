package monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * File: Port.java
 * Date: 27 May 2019
 * @author Logan Hershberger
 * Purpose: Object Representation of a Sea Port
 */
public class Port extends Thing {
  ArrayList<Dock> docks = new ArrayList<>();
  ArrayList<Ship> queue = new ArrayList<>();
  ArrayList<Ship> ships = new ArrayList<>();
  ArrayList<Person> persons = new ArrayList<>();
  HashMap<String,ArrayList<Person>> resourceMap = new HashMap<>();
  HashMap<String,Integer> totalResourceList = new HashMap<>();
  World world;


  public Port(Scanner sc, World world, Map<Integer, Port> hmp){
    super(sc);
    this.world = world;
    this.world.ports.add(this);
    hmp.put(sc.nextInt(),this);
  }

  public String toString(){
    return String.format("%-20s %-20s", "Port:", name);
  }

  /**
   * Removes a ship from the list of all ships and assigns the open dock a new ship if one is available.
   * Rejects ships with no jobs
   * @param dock The Dock that is losing the ship
   * @param ship The Ship that is departing
   */
  void notifyOfDeparture(Dock dock, Ship ship){
    ships.remove(ship);
    if(!queue.isEmpty()){
      Ship s = queue.get(0);

      // force ships with no jobs to leave
      if(s.jobs.isEmpty()){
        queue.remove(0);
        notifyOfDeparture(dock,s);
      } else {
        dock.assignShip(s);
        queue.remove(0);
      }
    }
  } // end notifyOfDeparture

  /**
   * populates the ports resource pool. The resource pool is a hashmap with skill names as keys
   * and an arraylist of persons as the values. Persons are removed from the pool as they are
   * assigned jobs and added as they return from jobs. Also generates the total resource list.
   */
  void buildResourceMap(){
    for(Person person : persons) {
      if (resourceMap.containsKey(person.skill)) {
        resourceMap.get(person.skill).add(person);
        totalResourceList.replace(person.skill,totalResourceList.get(person.skill)+1);
      } else {
        ArrayList<Person> tmp = new ArrayList<>();
        tmp.add(person);
        resourceMap.put(person.skill, tmp);
        totalResourceList.put(person.skill,1);
      }
    }
    // notify jobs that tried to access the resource pool before it was populated
    synchronized (this){
      notifyAll();
    }
  }

  HashMap<String, ArrayList<Person>> getResourceMap() {
    return resourceMap;
  }

  /**
   * Checks for the requested workers for a particular job. If the workers are found and
   * available, they are sent to the ship and true is returned. If they are found but not
   * available, false is returned. If they are not found, the job is cancelled and true is
   * returned to allow the job thread to end.
   * @param ship The ship that owns the job
   * @param job The job that needs the workers
   * @return true if the workers were found and available, false otherwise
   */
  boolean requestWorkers(Ship ship, Job job){
    ArrayList<Person> assignedWorkers = new ArrayList<>();

    // prevents resources from being taken by multiple jobs
    synchronized (resourceMap){
      if(resourceMap.isEmpty()){
        // resource map has not been loaded yet
        return false;
      }

      for(String skill: job.requirements){
        // count number of persons with this skill that the job requires
        // necessary if the job requires multiple workers of the same skill
        int skillCounter = 0;
        for(int i=0; i<job.requirements.size(); i++){
          if(job.requirements.get(i).equals(skill))
            skillCounter++;
        }
        // if no persons with skill exist at port, or
        // not enough persons with skill at the port, cancel job
        if((totalResourceList.get(skill) == null) || (skillCounter > totalResourceList.get(skill))){
          job.cancel();
          return true;
        }
      }

      for(String skill : job.requirements){
        ArrayList<Person> tmp = resourceMap.get(skill);
        if(tmp.isEmpty()){
          // a worker of this skill is not currently available
          return false;
        } else {
          // worker is available, add to list to assign
          assignedWorkers.add(tmp.get(0));
        }
      }

      // All job requirements have been met, assign the workers to the job and remove them from
      // the resource pool
      for(Person person : assignedWorkers){
        person.assignJob(job);
        resourceMap.get(person.skill).remove(person);
      }
      ship.addWorkers(assignedWorkers);
    }
    return true;
  }

  /**
   * Adds person back to the resource pool and notifies all job threads that new resources are
   * available
   * @param person person being returned to the resource pool
   */
  synchronized void returnWorker(Person person){
    resourceMap.get(person.skill).add(person);
    person.clearJobAndReturn(this);
    notifyAll();
  }

}
