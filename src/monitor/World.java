package monitor;

import java.util.ArrayList;

/**
 * File: World.java
 * Date: 27 May 2019
 * @author Logan Hershberger
 * Purpose: Top level class for Sea Port Program.
 */
public class World {

  ArrayList<Port> ports = new ArrayList<>();
  String name;

  public World(String name) {
    this.name = name;
  }


  /**
   * Searches for all persons who have the given skill
   * @param skill skill as a String
   * @return ArrayList of persons having a particular skill
   */
  public ArrayList<Person> searchBySkill(String skill){
    ArrayList<Person> results = new ArrayList<>();

    for(Port p : ports){
      for(Person person : p.persons){
        if(person.skill.toLowerCase().equals(skill)){
          results.add(person);
        }
      } // end of Person foreach
    } // end of Port foreach

    return results;
  } // end of searchBySkill


  /**
   * Gets all objects that match the given name
   * @param name name as a String
   * @return Always returns an ArrayList containing 5 ArrayLists.
   *         Inner ArrayLists are in the following order:
   *         SeaPorts, Docks, CargoShips, PassengerShips, Persons.
   *         Each ArrayList contains its assigned type of objects that match the given name.
   */
  public ArrayList<ArrayList<Thing>> searchByName(String name){
    ArrayList<ArrayList<Thing>> results = new ArrayList<>();
    for(int i = 0; i < 5; i++){
      results.add(new ArrayList<>());
    }

    for(Port port : ports){
      if(port.name.toLowerCase().equals(name)){
        results.get(0).add(port);
      }

      for(Dock dock : port.docks){
        if(dock.name.toLowerCase().equals(name)){
          results.get(1).add(dock);
        }
      } // end of Dock foreach

      for(Ship ship : port.ships){
        if(ship.name.toLowerCase().equals(name)){
          results.get(2).add(ship);
        }
      } // end of Ship foreach

      for(Person pers : port.persons){
        if(pers.name.toLowerCase().equals(name)){
          results.get(3).add(pers);
        }
      } // end of Person foreach
    } // end of Port foreach

    return results;
  } // end of searchByName


  /**
   * Searches for ships whose specified attr is less than the given max value
   * @param value ship attr max value as a double
   * @return ArrayList of ships
   */
  public ArrayList<Ship> searchShipByMaxX(String attr, double value){
    ArrayList<Ship> results = new ArrayList<>();

    for(Port port : ports){
      for(Ship ship : port.ships){

        if(attr.equals("length")){
          if(ship.length < value){
            results.add(ship);
          }
        } else if(attr.equals("width")){
          if(ship.width < value){
            results.add(ship);
          }
        }

      } // end of Ship foreach
    } // end of Port foreach

    return results;
  } // end of searchShipByMaxX

  // returns all ports in the world
  public ArrayList<Thing> getPorts(){
    ArrayList<Thing> portslist = new ArrayList<>();
    portslist.addAll(ports);
    return portslist;
  }

  // returns all docks in the world
  public ArrayList<Thing> getDocks(){
    ArrayList<Thing> results = new ArrayList<>();
    for(Port p : ports){
      results.addAll(p.docks);
    }
    return results;
  }

  // returns all persons in the world
  public ArrayList<Thing> getPersons(){
    ArrayList<Thing> results = new ArrayList<>();
    for(Port p : ports){
      results.addAll(p.persons);
    }
    return results;
  }


  /**
   * Gets the ships in the current world that the boolean flags call for
   * @param cargo adds all cargo ships to the return arraylist
   * @param passenger adds all passenger ships to the return arraylist
   * @return ArrayList of Ships that match the defined criteria
   */
  public ArrayList<Thing> getShips(boolean cargo, boolean passenger){
    ArrayList<Thing> results = new ArrayList<>();

    for(Port p : ports){
      for(Ship s : p.ships){
        if(cargo && (s instanceof CargoShip)){
          results.add(s);
        } else if(passenger && (s instanceof PassengerShip)){
          results.add(s);
        }
      } // end of Ship foreach
    } // end of Port foreach

    return results;
  } // end of getShips

} // end of World class
