# Seaport-Monitor
Graphical interface used to monitor the resources of a sea port. 

This program reads in the state of one or more seaports from a specially formatted text file.

The state includes:
  + one or more seaports
  + one or more docks per seaport
  + passenger and/or cargo ships assigned to docks or seaports
  + persons (workers) assigned to seaports
  + jobs assigned to ships
  

Ships have jobs that require resources (workers). Workers can have a specifc job or be just a generic worker. 
Once a ship has docked, if the needed workers are available, then the ship acquires those workers and the job(s) progress.
Each job takes a certain amount of time to complete. Once the ship's jobs are complete, the workers are returned and the ship departs. 

There are two example input files included, input1.txt and input2.txt. Objects in the input files are linked to parent objects using ID numbers. It is important that an object's parent appears higher in the input file than the object itself. 

Ship names were generated using random dictionary words. 

The GUI was built using the IntelliJ IDEA GUI designer interface. 
