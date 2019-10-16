package monitor;

import java.util.ArrayList;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * File: PortTree.java
 * Date: 21 June 2019
 * @author Logan Hershberger
 * Purpose: Subclass of JTree used to override the convertValueToText method so as not have to
 *          reformat the already used toString method
 */
public class PortTree extends JTree {

  static final long serialVersionUID = 1L;

  private DefaultTreeModel model;

  /**
   * Constructs tree and sets tree model
   * @param ports Port arraylist used to build the tree
   */
  PortTree(ArrayList<Port> ports){
    super();
    model = new DefaultTreeModel(buildTreeModel(ports));
    setModel(model);
  }

  // Returns what is displayed for a given tree node
  @Override
  public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf,
          int row, boolean hasFocus){
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
    Object obj = node.getUserObject();

    // if node is just one of the categories
    if(!(obj instanceof Thing))
      return value.toString();

    Thing thing = (Thing)obj;

    // Nodes will display their type and name
    String s = "";
    if(thing instanceof CargoShip){
      s = "Cargo: ";
    } else if(thing instanceof PassengerShip){
      s = "Passenger: ";
    } else if(thing instanceof Port){
      s = "Port: ";
    } else if(thing instanceof Dock){
      s = "Dock: ";
    } else if(thing instanceof Person){
      // Persons will display their name and skill
      return thing.getName() + " - " + ((Person)thing).getSkill();
    }
    return s + thing.getName();
  }

  // builds the tree hierarchy from the arraylist of ports
  private DefaultMutableTreeNode buildTreeModel(ArrayList<Port> ports){
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("World");
    DefaultMutableTreeNode portNode;
    DefaultMutableTreeNode node;

    for (Port port : ports) {
      portNode = new DefaultMutableTreeNode(port);
      DefaultMutableTreeNode docksDir = new DefaultMutableTreeNode("Docks");
      DefaultMutableTreeNode queueDir = new DefaultMutableTreeNode("Ship Queue");
      DefaultMutableTreeNode shipsDir = new DefaultMutableTreeNode("All Ships");
      DefaultMutableTreeNode personsDir = new DefaultMutableTreeNode("Persons");
      DefaultMutableTreeNode jobsDir = new DefaultMutableTreeNode("Jobs");

      for (Dock dock : port.docks) {
        node = new DefaultMutableTreeNode(dock);
        if (dock.getShip() != null) {
          node.add(new DefaultMutableTreeNode(dock.getShip()));
        }
        docksDir.add(node);
      }
      portNode.add(docksDir);

      for (Ship ship : port.queue) {
        queueDir.add(new DefaultMutableTreeNode(ship));
      }
      portNode.add(queueDir);

      for (Ship ship : port.ships) {
        shipsDir.add(new DefaultMutableTreeNode(ship));
        if (!ship.jobs.isEmpty()) {
          for (Job job : ship.jobs) {
            jobsDir.add(new DefaultMutableTreeNode(job));
          }
        }
      }
      portNode.add(shipsDir);
      portNode.add(jobsDir);

      for (Person person : port.persons) {
        personsDir.add(new DefaultMutableTreeNode(person));
      }
      portNode.add(personsDir);
      root.add(portNode);
    }

    return root;
  }

  // updates tree model with new port arraylist
  void updateModel(ArrayList<Port> ports){
    DefaultTreeModel model = new DefaultTreeModel(buildTreeModel(ports));
    setModel(model);
  }
}
