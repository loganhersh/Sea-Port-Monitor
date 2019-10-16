package monitor;

        import java.util.Scanner;

/**
 * File: Thing.java
 * Date: 27 May 2019
 * @author Logan Hershberger
 * Purpose: Foundation class for most of the objects implemented in this program
 */
public class Thing implements Comparable<Thing> {

  String name;

  public Thing(Scanner sc) {
    name = sc.next();
  }

  public int compareTo(Thing t) {
    return name.compareTo(t.name);
  }

  public String getName(){
    return name;
  }
}
