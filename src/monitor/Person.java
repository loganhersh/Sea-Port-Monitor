package monitor;

import java.util.Map;
import java.util.Scanner;

/**
 * File: Person.java
 * Date: 27 May 2019
 * @author Logan Hershberger
 * Purpose: Object Representation of a Person who works at a sea port
 */
public class Person extends Thing {
  Port port;
  String skill;
  Thing location;
  Job currentjob;

  public Person(Scanner sc, Map<Integer,Port> hmp){
    super(sc);
    sc.next();
    port = hmp.get(sc.nextInt());
    port.persons.add(this);
    skill = sc.next();
    location = port;
    currentjob = null;
  }

  public String toString(){
    return String.format("%-20s %-20s %-20s",name,port.name,skill);
  }

  boolean isAvailable(){
    return (location instanceof Port);
  }

  public String getSkill(){
    return skill;
  }

  public void assignJob(Job job){
    currentjob = job;
    location = job.getShip();
  }

  public void clearJobAndReturn(Port port){
    currentjob = null;
    location = port;
  }
}
