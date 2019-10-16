package monitor;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

/**
 * File: Job.java
 * Date: 27 May 2019
 * @author Logan Hershberger
 * Purpose: Object Representation of a Job at a Sea port
 */
public class Job extends Thing implements Runnable{
  private double duration;
  ArrayList<String> requirements = new ArrayList<>();
  Ship ship;
  private Port port;
  private boolean goFlag;
  private boolean killFlag;
  enum Status {RUNNING, SUSPENDED, WAITINGTODOCK, DONE, CANCELLED, LACKOFRESOURCES,
    WAITINGFORRESOURCES}
  private int progress;
  private Status status;


  /**
   * Constructs a job using the line scanner for the data file.
   * Links to the proper ship and adds itself to the Jobs table.
   * @param sc token Scanner with Job data
   * @param hms hashmap of all current ships
   * @param table the program's jobs table
   */
  public Job(Scanner sc, Map<Integer, Ship> hms, JobTable table){
    super(sc);
    sc.nextInt();
    ship = hms.get(sc.nextInt());
    ship.jobs.add(this);
    port = ship.port;
    duration = Double.parseDouble(sc.next());
    while(sc.hasNext()){
      requirements.add(sc.next());
    }
    goFlag = true;
    killFlag = false;
    progress = 0;
    status = Status.WAITINGTODOCK;
    table.addJob(this);
    (new Thread(this)).start();
  }


  /**
   * When a new thread is run, method waits until a ship is docked then the Job progresses for
   * the full duration. Status is updated as job progresses and when finished it waits until the
   * ship departs to update the ship location in the jobs table.
   */
  public void run(){
    long time;
    long startTime;
    long stopTime;
    double durationMillis = 1000*duration;

    // wait until ship is docked
    synchronized (ship) {
      try {
        if ((ship.dock == null) && !killFlag) {
          ship.wait();
        }
      } catch (InterruptedException e){}
    }

    while(!ship.initiateJob(this) && !killFlag) {
      synchronized (getPort()) {
        try {
          status = Status.WAITINGFORRESOURCES;
          getPort().wait();
        } catch (InterruptedException e) {
        }
      }
    }

    try {
      time = System.currentTimeMillis();
      startTime = time;
      stopTime = (long) (time + durationMillis);

      while (time < stopTime && !killFlag) {

        int delay = (SeaPortMonitor.testMode) ? 10 : 100;       // speed up for testing
        Thread.sleep(delay);

        // progress job or suspend
        if (goFlag) {
          status = Status.RUNNING;
          time += 100;
          progress = (int) (((time - startTime) / durationMillis) * 100);
        } else {
          status = Status.SUSPENDED;
        }
      }

      // check if job was cancelled
      if (killFlag) {
        progress = 0;
        if(status != Status.LACKOFRESOURCES) {
          status = Status.CANCELLED;
        }
      } else {
        progress = 100;
        status = Status.DONE;
      }

      // tell ship to remove this job
      // true is returned if this was the last job for the ship
      ship.removeJob(this);

    } catch (InterruptedException e){}
  }

  void cancel(){
    kill();
    status = Status.LACKOFRESOURCES;
  }

  int getProgress(){
    return progress;
  }

  Status getStatus(){
    return status;
  }

  void toggleGoFlag(){
    goFlag = !goFlag;
  }

  void kill(){
    killFlag = true;
  }

  Port getPort(){
    return port;
  }

  public Ship getShip(){
    return ship;
  }

}
