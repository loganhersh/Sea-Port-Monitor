package monitor;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import monitor.JobTable.SortColumn;

/**
 * Seaport Monitor
 * @author Logan Hershberger
 * Date: 14 July 2019
 * Purpose: This is a monitoring program that could be used to track and manage the movement of
 *          ships and resources throughout a seaport.
 *          This program demonstrates synchronization principles.
 */
public class SeaPortMonitor extends JFrame {

  static final long serialVersionUID = 1L;
  private JPanel mainPanel;
  private JTextArea outputArea;
  private JTextArea searchResultArea;
  private JPanel searchSortContainer;
  private JPanel sortPanel;
  private JPanel searchPanel;
  private JPanel filePanel;
  private JSplitPane outputSplitPane;
  private JTextField searchField;
  private JComboBox<String> targetCBox;
  private JButton searchButton;
  private JComboBox<String> sortItemCbox;
  private JComboBox<String> sortOrderCbox;
  private JButton sortButton;
  private JPanel sortByCardPanel;
  private JTextField fileNameField;
  private JButton chooseFileButton;
  private JButton loadFileButton;
  private JComboBox<String> sortPortByCbox;
  private JComboBox<String> sortDockByCbox;
  private JComboBox<String> sortShipByCbox;
  private JComboBox<String> sortPersonByCbox;
  private JPanel filePanelContainer;
  private JPanel outputPanel;
  private JPanel dataPanel;
  private JScrollPane scrollPaneBottom;
  private JPanel topPanel;
  private JSplitPane topSplitPane;
  private JPanel treePanel;
  private JPanel resultsPanel;
  private JScrollPane searchScrollPane;
  private JPanel mainTabPanel;
  private JTabbedPane mainTabbedPane;
  private JPanel jobTabPanel;
  private JPanel tabPanePanel;
  private JScrollPane treeScroll;
  private JPanel jobStatusPanel;
  private JScrollPane jobScrollPane;
  private JCheckBox updateDataCheckBox;
  private JPanel portSelectionPanel;
  private JScrollPane portSelectionScrollPane;
  private JPanel portResourceContainerPanel;
  private JPanel portSelectionBtnPanel;
  private JFileChooser fileChooser;

  private int lastPos;
  private monitor.PortTree worldTree;
  private File dataFile;
  private monitor.World world;
  private monitor.JobTable jobsTable;
  private ButtonGroup portSelectGroup;
  private monitor.PortResourcePanel portResourcePanel;
  private boolean isInitialFileLoad = true;

  // When set true, jobs progress 10 times faster and the data file is hard-coded into the load
  // file button
  public static boolean testMode = false;
  // In test mode, this file will load simply by clicking the Load file button
  public String testFilePath = FileSystems.getDefault().getPath("input2.txt").toString();

  enum SortEnum {
    PORT, DOCK, SHIP, PERSON
  }

  private String portColLabels = String.format("%-20s %-20s \n", "Type", "Name")
          + getCharSequence('-', 40) + "\n";
  private String dockColLabels = String.format("%-20s %-20s %-20s \n", "Type", "Name", "Port")
          + getCharSequence('-', 60) + "\n";
  private String shipColLabels = String
          .format("%-20s %-20s %-20s %-12s %-12s %-12s %-12s \n", "Type", "Name", "Port",
                  "Length", "Width", "Weight", "Draft") + getCharSequence('-', 190) + "\n";
  private String personColLabels = "----- Persons " + getCharSequence('-', 46) + "\n"
          + String.format("%-20s %-20s %-20s \n", "Name", "Port", "Skill")
          + getCharSequence('-', 60) + "\n";


  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new SeaPortMonitor());
  }

  // Builds GUI and implements listeners
  public SeaPortMonitor() {
    super("Sea Port Manager");

    try {
      for (LookAndFeelInfo lf : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(lf.getName())) {
          UIManager.setLookAndFeel(lf.getClassName());
          break;
        }
      }
    } catch (Exception e) {
      // Nimbus was not found, gui will use default look and feel
    }

    buildGUI();
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

    setContentPane(mainPanel);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(new Dimension(screen.width*5/6, screen.height*5/6));
    setLocationRelativeTo(null);
    setVisible(true);


    /*
        ==================== Listeners ========================
    */
    chooseFileButton.addActionListener(e -> {
      fileChooser = new JFileChooser(".");
      fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(".txt", "txt"));
      fileChooser.setAcceptAllFileFilterUsed(false);

      int val = fileChooser.showDialog(this, "Select");

      if (val == JFileChooser.APPROVE_OPTION) {
        fileNameField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        getRootPane().setDefaultButton(loadFileButton);
      }

    }); // end chooseFileButton listener

    loadFileButton.addActionListener(e -> {
      // if not the first file load, kill the old threads
      if (!isInitialFileLoad) {
        killOldThreads();
      }

      try {
        if (testMode) {
          // hard code file path for easier testing
          dataFile = new File(testFilePath);
        } else {
          if (fileNameField.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Must choose a data file", "No File",
                    JOptionPane.ERROR_MESSAGE);
            return;
          }
          dataFile = new File(fileNameField.getText());
        }

        // Create and populate world
        world = new monitor.World("Default World");
        jobsTable = new monitor.JobTable();
        portSelectionBtnPanel = new JPanel(new GridLayout(0, 1));
        portSelectGroup = new ButtonGroup();
        processDataFile(dataFile, world, jobsTable);

        // get and display port resource panel
        portResourcePanel = new monitor.PortResourcePanel(world.ports, this);
        portResourceContainerPanel.removeAll();
        portResourceContainerPanel.add(portResourcePanel, BorderLayout.CENTER);

        // Display World
        outputArea.setText(getFormattedOutput(null, null, null));
        worldTree = new monitor.PortTree(world.ports);
        treeScroll.setViewportView(worldTree);

        // Display Ship details in new frame when a ship in the tree is double-clicked
        worldTree.addMouseListener(new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            try {
              super.mousePressed(e);
              if (e.getClickCount() >= 2) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) worldTree
                        .getLastSelectedPathComponent();
                if (node.getUserObject() instanceof monitor.Ship) {
                  new monitor.ShipDetailsFrame((monitor.Ship) node.getUserObject());
                }
              }
            } catch (NullPointerException e1) {
            }
          }
        });

        // Display table in scrollpane
        jobsTable.sort(SortColumn.PORT);
        jobScrollPane.setViewportView(jobsTable);
        new Thread(jobsTable).start();

        // display port selection buttons and select first port
        portSelectionScrollPane.setViewportView(portSelectionBtnPanel);
        AbstractButton btn = Collections.list(portSelectGroup.getElements()).get(0);
        btn.setSelected(true);

        getRootPane().setDefaultButton(searchButton);
        searchField.requestFocus();
        searchResultArea.setText("");
        searchField.setText("");
        targetCBox.setSelectedIndex(0);
        mainTabbedPane.setEnabled(true);
        resultsPanel.setEnabled(true);
        treePanel.setEnabled(true);
        dataPanel.setEnabled(true);
        outputSplitPane.setEnabled(true);
        topSplitPane.setEnabled(true);
        enableSearch();
        enableSort();

        // Force docked ships with no jobs to leave
        for (Port p : world.ports) {
          for (monitor.Dock d : p.docks) {
            if (d.ship != null) {
              if (d.ship.jobs.isEmpty()) {
                d.ship.leavePort();
              }
            }
          }
        }
        portResourceContainerPanel.revalidate();
        isInitialFileLoad = false;

      } catch (FileNotFoundException e1) {
        outputArea.setText("Error: File could not be read");
        disableSearch();
        disableSort();
      } catch (monitor.InvalidSearchSortException e2) {
      }

    }); // end loadFileBtn listener

    sortItemCbox.addActionListener(e -> {
      CardLayout card = (CardLayout) sortByCardPanel.getLayout();

      switch (String.valueOf(sortItemCbox.getSelectedItem())) {
        case "Port":
          card.show(sortByCardPanel, "port");
          break;
        case "Dock":
          card.show(sortByCardPanel, "dock");
          break;
        case "Ship":
          card.show(sortByCardPanel, "ship");
          break;
        case "Person":
          card.show(sortByCardPanel, "person");
          break;
      }
    });

    searchButton.addActionListener(e -> {
      String targetType = String.valueOf(targetCBox.getSelectedItem());
      String target = searchField.getText().toLowerCase().trim();
      String output = "\n               Results\n"
              + getCharSequence('=', 40) + "\n\n";

      try {
        if (targetType.equals("")) {
          throw new monitor.InvalidSearchSortException(
                  "Must select a search target from the dropdown menu");
        }

        // get output depending on target type
        switch (targetType) {
          case "Name":
            output += getStringSearchByName(target);
            break;
          case "Type":
            output += getStringSearchByType(target);
            break;
          case "Person: Skill":
            output += getStringSearchBySkill(target);
            break;
          case "Ship: Max Length":
            output += getStringSearchShipByMaxX("Length", target);
            break;
          case "Ship: Max Width":
            output += getStringSearchShipByMaxX("Width", target);
            break;

        } // end of switch

        outputSplitPane.setResizeWeight(0.3);
        searchResultArea.setText(output);
        outputSplitPane.resetToPreferredSizes();

      } catch (monitor.InvalidSearchSortException e1) {
        JOptionPane.showMessageDialog(this, e1.getMessage(), "Invalid Search",
                JOptionPane.ERROR_MESSAGE);
        searchResultArea.setText("");
      } catch (NumberFormatException e2) {
        String err = "For target type '" + targetType;

        switch (targetType) {

          case "Index":
          case "Parent Index":
            err += "' an integer must be entered.    ";
            break;
          case "Ship: Max Length":
          case "Ship: Max Width":
            err += "' a double must be entered.    ";

        } // end of switch
        JOptionPane.showMessageDialog(this, err, "Invalid Input", JOptionPane.ERROR_MESSAGE);
      } // end of catch


    }); // end searchButton listener

    sortButton.addActionListener(e -> {
      try {
        if (sortItemCbox.getSelectedIndex() == 0) {
          throw new monitor.InvalidSearchSortException("Must select an object to sort");
        }

        String sortObject = sortItemCbox.getSelectedItem().toString();
        String sortBy = null;
        String sortOrder = sortOrderCbox.getSelectedItem().toString();
        SortEnum obj = null;
        if (!sortObject.equals("")) {
          switch (sortObject) {
            case "Port":
              obj = SortEnum.PORT;
              sortBy = sortPortByCbox.getSelectedItem().toString();
              break;
            case "Dock":
              obj = SortEnum.DOCK;
              sortBy = sortDockByCbox.getSelectedItem().toString();
              break;
            case "Ship":
              obj = SortEnum.SHIP;
              sortBy = sortShipByCbox.getSelectedItem().toString();
              break;
            case "Person":
              obj = SortEnum.PERSON;
              sortBy = sortPersonByCbox.getSelectedItem().toString();
              break;
          }
        }

        String output = getFormattedOutput(obj, sortBy, sortOrder);
        outputArea.setText(output);

      } catch (monitor.InvalidSearchSortException e1) {
        JOptionPane.showMessageDialog(this, e1.getMessage(), "Invalid Sort",
                JOptionPane.ERROR_MESSAGE);
      }
    }); // end sortButton listener

    // updates information in the world view when the user returns to the world view tab
    mainTabbedPane.addChangeListener(e -> {
      if (updateDataCheckBox.isSelected()) {
        if (mainTabbedPane.getSelectedIndex() == 0) {
          update();
          SwingUtilities
                  .invokeLater(() -> scrollPaneBottom.getVerticalScrollBar().setValue(lastPos));
        } else {
          lastPos = scrollPaneBottom.getVerticalScrollBar().getValue();
        }
      }
    });


  } // end SeaPortProgram constructor


  /*
   * Populates a World object based on a provided,
   * properly formatted data file
   */
  private void processDataFile(File file, monitor.World world, monitor.JobTable table)
          throws FileNotFoundException {

    Scanner lineSc = new Scanner(file);
    Scanner tokenSc;
    int lineCounter = 0;
    int errCounter = 0;
    boolean invalidRefReportedFlag = false;
    Map<Integer, Port> hmp = new HashMap<>();
    Map<Integer, monitor.Dock> hmd = new HashMap<>();
    Map<Integer, monitor.Ship> hms = new HashMap<>();

    while (lineSc.hasNextLine()) {
      tokenSc = new Scanner(lineSc.nextLine());
      lineCounter++;
      if (!tokenSc.hasNext()) {
        continue;
      }

      String nextType = (tokenSc.next());

      try { // first try

        try { // second try

          // add object based on first token in each line
          switch (nextType) {
            case "//":
              break;
            case "port":
              Port p = new Port(tokenSc, world, hmp);
              JRadioButton btn = new JRadioButton(p.getName());
              btn.addActionListener(e -> portResourcePanel.setPort(btn.getText()));
              portSelectGroup.add(btn);
              portSelectionBtnPanel.add(btn);
              break;
            case "dock":
              new monitor.Dock(tokenSc, hmp, hmd);
              break;
            case "pship":
              new monitor.PassengerShip(tokenSc, hmp, hmd, hms);
              break;
            case "cship":
              new monitor.CargoShip(tokenSc, hmp, hmd, hms);
              break;
            case "person":
              new monitor.Person(tokenSc, hmp);
              break;
            case "job":
              new monitor.Job(tokenSc, hms, table);
              break;
            // invalid object type found
            default:
              tokenSc.close();
              throw new monitor.DataFileFormatException(lineCounter, errCounter);
          } // end of switch

        } catch (NumberFormatException e) {
          throw new monitor.DataFileFormatException(lineCounter, errCounter);

        } catch (NullPointerException e1) {
          if (!invalidRefReportedFlag) {
            JOptionPane.showMessageDialog(this, "Error: Attempt to link "
                            + "object on line " + lineCounter + " to an object that does not exist.\n"
                            + "Program will continue but objects may be missing.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            invalidRefReportedFlag = true;
          }
        } // end second try

      } catch (monitor.DataFileFormatException e1) {
        errCounter++;
        // stop reporting if more than 3 errors have occurred
        if (errCounter <= 3) {
          JOptionPane.showMessageDialog(this, e1.getMessage(), "Data File Error",
                  JOptionPane.INFORMATION_MESSAGE);
        }

      } // end first try

      tokenSc.close();
    } // end of while

    lineSc.close();

    for (Port port : world.ports) {
      port.buildResourceMap();
    }
  } // end of processDataFile


  /*
   * Gets formatted text for all objects matching the supplied name
   * Formats the output
   */
  private String getStringSearchByName(String name) throws monitor.InvalidSearchSortException {
    if (name.equals("")) {
      throw new monitor.InvalidSearchSortException("Must enter a name");
    }

    ArrayList<ArrayList<monitor.Thing>> results = world.searchByName(name);

    StringBuilder output = new StringBuilder();

    for (ArrayList<monitor.Thing> currentList : results) {
      if (currentList.isEmpty()) {
        continue;
      }

      monitor.Thing temp = currentList.get(0);
      if (temp instanceof Port) {
        output.append(portColLabels);
      } else if (temp instanceof monitor.Dock) {
        output.append(dockColLabels);
      } else if (temp instanceof monitor.Ship) {
        output.append(shipColLabels);
      } else if (temp instanceof monitor.Person) {
        output.append(personColLabels);
      }

      for (monitor.Thing t : currentList) {
        output.append(t.toString() + "\n");
      }

      output.append("\n");
    }

    if (output.toString().equals("")) {
      return "No Results Found";
    }

    return output.toString();
  } // end getStringSearchByName


  /*
   * Gets formatted text for all persons who have the supplied skill
   */
  private String getStringSearchBySkill(String skill) throws monitor.InvalidSearchSortException {
    if (skill.equals("")) {
      throw new monitor.InvalidSearchSortException("Must enter a skill");
    }

    StringBuilder output = new StringBuilder();
    ArrayList<monitor.Person> results = world.searchBySkill(skill);

    if (results.isEmpty()) {
      return "No Results Found";
    } else {
      output.append(personColLabels);
      for (monitor.Person p : results) {
        output.append(p.toString() + "\n");
      }
    }
    return output.toString();
  } // end getStringSearchBySkill


  /*
   * Gets formatted text for all objects of the supplied type
   */
  private String getStringSearchByType(String type) throws monitor.InvalidSearchSortException {
    if (type.equals("")) {
      throw new monitor.InvalidSearchSortException("Must enter an object type");
    }
    ArrayList<monitor.Thing> results;
    StringBuilder output = new StringBuilder();

    switch (type.replaceAll("\\s", "")) {
      case "port":
      case "seaport":
        results = world.getPorts();
        output.append(portColLabels);
        break;
      case "dock":
        results = world.getDocks();
        output.append(dockColLabels);
        break;
      case "ship":
        results = world.getShips(true, true);
        output.append(shipColLabels);
        break;
      case "cargo":
      case "cargoship":
        results = world.getShips(true, false);
        output.append(shipColLabels);
        break;
      case "passenger":
      case "passengership":
        results = world.getShips(false, true);
        output.append(shipColLabels);
        break;
      case "person":
        results = world.getPersons();
        output.append(personColLabels);
        break;
      default:
        throw new monitor.InvalidSearchSortException("Invalid Type\nValid types: Port, "
                + "Dock, Ship, Cargo Ship, Passenger Ship, Person  ");
    }

    for (Object obj : results) {
      output.append(obj.toString() + "\n");
    }

    return output.toString();
  } // end getStringSearchByType


  /*
   * Gets formatted text for all ships whose specified attribute is less than the supplied maximum
   */
  private String getStringSearchShipByMaxX(String attr, String value)
          throws monitor.InvalidSearchSortException {
    if (value.equals("")) {
      throw new monitor.InvalidSearchSortException("Must enter a value");
    }

    ArrayList<monitor.Ship> results = world.searchShipByMaxX(attr.toLowerCase(), Double.parseDouble(value));
    StringBuilder output = new StringBuilder();

    if (results.isEmpty()) {
      return "No ships found with " + attr.toLowerCase() + " less than " + value;
    }

    output.append("Max " + attr + ": " + value + "\n\n");
    output.append(shipColLabels);
    for (monitor.Ship ship : results) {
      output.append(ship.toString() + "\n");
    }

    return output.toString();
  } // end getStringSearchByMaxX


  private void enableSearch() {
    searchField.setEnabled(true);
    targetCBox.setEnabled(true);
    searchButton.setEnabled(true);
    searchPanel.setEnabled(true);
  }

  private void disableSearch() {
    searchField.setEnabled(false);
    targetCBox.setEnabled(false);
    searchButton.setEnabled(false);
    searchPanel.setEnabled(false);
  }

  private void enableSort() {
    sortPanel.setEnabled(true);
    sortItemCbox.setEnabled(true);
    sortPortByCbox.setEnabled(true);
    sortOrderCbox.setEnabled(true);
    sortButton.setEnabled(true);
  }

  private void disableSort() {
    sortPanel.setEnabled(false);
    sortItemCbox.setEnabled(false);
    sortPortByCbox.setEnabled(false);
    sortOrderCbox.setEnabled(false);
    sortButton.setEnabled(false);
  }

  /**
   * Builds an output String that summarizes the World's Ports, Docks, Ships, and Persons
   *
   * @return formatted String
   */
  public String getFormattedOutput(SortEnum type, String sortBy, String sortOrder)
          throws monitor.InvalidSearchSortException {
    StringBuilder s = new StringBuilder();

    s.append("\n\tWorld: " + world.name + "\n\n");

    if (world.ports.isEmpty()) {
      return s + "There are no ports.";
    }

    if (type == SortEnum.PORT) {
      sortPortData(world.ports, sortBy, sortOrder);
    }
    for (Port p : world.ports) {
      synchronized (p) {
        s.append("==============================\n"
                + "=                            =\n");
        s.append("= PORT: " + p.name.toUpperCase());
        s.append(getCharSequence(' ', 21 - p.name.length()) + "=\n"
                + "=                            =\n");
        s.append(getCharSequence('=', 190) + "\n");
        s.append(shipColLabels);

        if (!p.docks.isEmpty()) {
          if (type == SortEnum.DOCK) {
            sortDockData(p.docks, sortBy, sortOrder);
          }
          for (monitor.Dock d : p.docks) {
            s.append(d.toString() + "\n");
            if (d.getShip() != null) {
              s.append(d.getShip().toString() + "\n\n");
            } else {
              s.append("No Ship\n\n");
            }

          } // end of Dock foreach
        }

        if (type == SortEnum.SHIP) {
          sortShipData(p.ships, sortBy, sortOrder);
          sortShipData(p.queue, sortBy, sortOrder);
        }

        if (!p.queue.isEmpty()) {
          s.append("----- Queued Ships "
                  + getCharSequence('-', 171) + "\n");
          s.append(shipColLabels);
          for (monitor.Ship ship : p.queue) {
            s.append(ship.toString() + "\n");
          } // end of Queued Ship foreach
        }

        if (!p.ships.isEmpty()) {
          s.append("\n----- All Ships "
                  + getCharSequence('-', 174) + "\n");
          s.append(shipColLabels);
          for (monitor.Ship ship : p.ships) {
            s.append(ship.toString() + "\n");
          } // end of all Ships foreach
        }

        if (!p.persons.isEmpty()) {
          if (type == SortEnum.PERSON) {
            sortPersonData(p.persons, sortBy, sortOrder);
          }
          s.append("\n" + personColLabels);
          for (monitor.Person pers : p.persons) {
            s.append(pers.toString() + "\n");
          } // end of Persons foreach
        }
        s.append(getCharSequence('=', 190) + "\n");
        s.append("\n\n");
      }
    } // end of Port foreach

    return s.toString();
  } // end of getFormattedOutput


  // Sorts Ports by name within the arraylist in the specified order
  private void sortPortData(ArrayList<Port> data, String sortBy, String sortOrder)
          throws monitor.InvalidSearchSortException {
    if (sortBy.equals("")) {
      throw new monitor.InvalidSearchSortException("Must select an attribute to sort by");
    }

    Comparator<Port> comp = Comparator.comparing(monitor.Thing::getName);
    if (sortOrder.equals("Desc")) {
      data.sort(comp.reversed());
    } else {
      data.sort(comp);
    }
  }


  // Sorts Docks by name within the arraylist in the specified order
  private void sortDockData(ArrayList<monitor.Dock> data, String sortBy, String sortOrder)
          throws monitor.InvalidSearchSortException {
    if (sortBy.equals("")) {
      throw new monitor.InvalidSearchSortException("Must select an attribute to sort by");
    }

    Comparator<monitor.Dock> comp = Comparator.comparing(monitor.Thing::getName);
    if (sortOrder.equals("Desc")) {
      data.sort(comp.reversed());
    } else {
      data.sort(comp);
    }
  }


  // sorts Ships by the specified attribute in the specified order
  private void sortShipData(ArrayList<monitor.Ship> data, String sortBy, String sortOrder)
          throws monitor.InvalidSearchSortException {
    Comparator<monitor.Ship> comp;
    switch (sortBy) {
      case "Name":
        comp = Comparator.comparing(monitor.Ship::getName);
        break;
      case "Length":
        comp = Comparator.comparing(monitor.Ship::getLength);
        break;
      case "Width":
        comp = Comparator.comparing(monitor.Ship::getWidth);
        break;
      case "Weight":
        comp = Comparator.comparing(monitor.Ship::getWeight);
        break;
      case "Draft":
        comp = Comparator.comparing(monitor.Ship::getDraft);
        break;
      default:
        throw new monitor.InvalidSearchSortException("Must select an attribute to sort by");
    }

    if (sortOrder.equals("Desc")) {
      data.sort(comp.reversed());
    } else {
      data.sort(comp);
    }
  }


  // sorts Persons by the specified attribute in the specified order
  private void sortPersonData(ArrayList<monitor.Person> data, String sortBy, String sortOrder)
          throws monitor.InvalidSearchSortException {
    if (sortBy.equals("")) {
      throw new monitor.InvalidSearchSortException("Must select an attribute to sort by");
    }

    Comparator<monitor.Person> comp = Comparator.comparing(monitor.Person::getName);

    if (sortBy.equals("Skill")) {
      comp = Comparator.comparing(monitor.Person::getSkill);
    }

    if (sortOrder.equals("Desc")) {
      data.sort(comp.reversed());
    } else {
      data.sort(comp);
    }
  }


  // returns a string of n characters of type c
  private static String getCharSequence(char c, int n) {
    StringBuilder seq = new StringBuilder();
    for (int i = 0; i < n; i++) {
      seq.append(c);
    }
    return seq.toString();
  }


  // updates world tree and port data to account for ship departures
  private void update() {
    try {
      worldTree.updateModel(world.ports);
      outputArea.setText(getFormattedOutput(null, null, null));
      sortItemCbox.setSelectedIndex(0);
      sortOrderCbox.setSelectedIndex(0);
      ((CardLayout) sortByCardPanel.getLayout()).show(sortByCardPanel, "port");
    } catch (monitor.InvalidSearchSortException e) {
    }
  }


  boolean isJobsComplete() {
    return jobsTable.isJobsComplete();
  }

  void setSelectedJob(String jobname) {
    jobsTable.setSelectedJob(jobname);
  }

  /**
   * Kills all old threads related to the previous file load
   */
  private void killOldThreads() {
    jobsTable.kill();
    portResourcePanel.kill();
    for (Port p : world.ports) {
      for (monitor.Ship s : p.ships) {
        synchronized (s.jobs) {
          for (monitor.Job j : s.jobs) {
            if (j != null) {
              j.kill();
            }
          }
        }
      }
    }
  }

  // Generated with IntelliJ GUI designer
  private void buildGUI() {
    mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());
    mainPanel.setBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0), null));
    final JPanel spacer1 = new JPanel();
    GridBagConstraints gbc;
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.weightx = 0.1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(spacer1, gbc);
    final JPanel spacer2 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(spacer2, gbc);
    filePanelContainer = new JPanel();
    filePanelContainer.setLayout(new GridBagLayout());
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(5, 20, 5, 20);
    mainPanel.add(filePanelContainer, gbc);
    filePanel = new JPanel();
    filePanel.setLayout(new GridBagLayout());
    filePanel.setMinimumSize(new Dimension(10, 65));
    filePanel.setPreferredSize(new Dimension(10, 65));
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0, 20, 5, 20);
    filePanelContainer.add(filePanel, gbc);
    filePanel.setBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "File"));
    fileNameField = new JTextField();
    fileNameField.setMinimumSize(new Dimension(0, 26));
    fileNameField.setPreferredSize(new Dimension(0, 26));
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 0.8;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    filePanel.add(fileNameField, gbc);
    chooseFileButton = new JButton();
    chooseFileButton.setText("Choose File...");
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.weightx = 0.05;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 8, 0, 8);
    filePanel.add(chooseFileButton, gbc);
    loadFileButton = new JButton();
    loadFileButton.setText("Load File");
    gbc = new GridBagConstraints();
    gbc.gridx = 3;
    gbc.gridy = 0;
    gbc.weightx = 0.05;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 8, 0, 8);
    filePanel.add(loadFileButton, gbc);
    updateDataCheckBox = new JCheckBox();
    updateDataCheckBox.setSelected(true);
    updateDataCheckBox.setText("Update after departures");
    updateDataCheckBox.setToolTipText(
            "World View data updates after ship departures when navigating back to World View tab");
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    filePanel.add(updateDataCheckBox, gbc);
    final JPanel spacer3 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 5;
    gbc.gridy = 0;
    gbc.weightx = 0.05;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    filePanel.add(spacer3, gbc);
    final JPanel spacer4 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.05;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    filePanel.add(spacer4, gbc);
    tabPanePanel = new JPanel();
    tabPanePanel.setLayout(new GridBagLayout());
    tabPanePanel.setEnabled(true);
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 3;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    mainPanel.add(tabPanePanel, gbc);
    mainTabbedPane = new JTabbedPane();
    mainTabbedPane.setEnabled(false);
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    tabPanePanel.add(mainTabbedPane, gbc);
    mainTabPanel = new JPanel();
    mainTabPanel.setLayout(new GridBagLayout());
    mainTabbedPane.addTab("World View", null, mainTabPanel,
            "Overview of world with search/sort capabilities");
    searchSortContainer = new JPanel();
    searchSortContainer.setLayout(new GridBagLayout());
    searchSortContainer.setOpaque(true);
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(5, 0, 5, 0);
    mainTabPanel.add(searchSortContainer, gbc);
    sortPanel = new JPanel();
    sortPanel.setLayout(new GridBagLayout());
    sortPanel.setEnabled(false);
    sortPanel.setMinimumSize(new Dimension(450, 69));
    sortPanel.setPreferredSize(new Dimension(450, 69));
    sortPanel.setRequestFocusEnabled(false);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 0.3;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0, 0, 0, 10);
    searchSortContainer.add(sortPanel, gbc);
    sortPanel.setBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sort"));
    sortItemCbox = new JComboBox<>();
    sortItemCbox.setEnabled(false);
    final DefaultComboBoxModel<String> defaultComboBoxModel1 = new DefaultComboBoxModel<>();
    defaultComboBoxModel1.addElement("");
    defaultComboBoxModel1.addElement("Port");
    defaultComboBoxModel1.addElement("Dock");
    defaultComboBoxModel1.addElement("Ship");
    defaultComboBoxModel1.addElement("Person");
    sortItemCbox.setModel(defaultComboBoxModel1);
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.4;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 10, 0, 5);
    sortPanel.add(sortItemCbox, gbc);
    sortByCardPanel = new JPanel();
    sortByCardPanel.setLayout(new CardLayout(0, 0));
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 0.25;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0, 5, 0, 5);
    sortPanel.add(sortByCardPanel, gbc);
    sortPortByCbox = new JComboBox<>();
    sortPortByCbox.setEnabled(false);
    final DefaultComboBoxModel<String> defaultComboBoxModel2 = new DefaultComboBoxModel<>();
    defaultComboBoxModel2.addElement("");
    defaultComboBoxModel2.addElement("Name");
    sortPortByCbox.setModel(defaultComboBoxModel2);
    sortByCardPanel.add(sortPortByCbox, "port");
    sortDockByCbox = new JComboBox<>();
    final DefaultComboBoxModel<String> defaultComboBoxModel3 = new DefaultComboBoxModel<>();
    defaultComboBoxModel3.addElement("");
    defaultComboBoxModel3.addElement("Name");
    sortDockByCbox.setModel(defaultComboBoxModel3);
    sortByCardPanel.add(sortDockByCbox, "dock");
    sortShipByCbox = new JComboBox<>();
    final DefaultComboBoxModel<String> defaultComboBoxModel4 = new DefaultComboBoxModel<>();
    defaultComboBoxModel4.addElement("");
    defaultComboBoxModel4.addElement("Name");
    defaultComboBoxModel4.addElement("Weight");
    defaultComboBoxModel4.addElement("Length");
    defaultComboBoxModel4.addElement("Width");
    defaultComboBoxModel4.addElement("Draft");
    sortShipByCbox.setModel(defaultComboBoxModel4);
    sortByCardPanel.add(sortShipByCbox, "ship");
    sortPersonByCbox = new JComboBox<>();
    sortPersonByCbox.setEnabled(true);
    final DefaultComboBoxModel<String> defaultComboBoxModel5 = new DefaultComboBoxModel<>();
    defaultComboBoxModel5.addElement("");
    defaultComboBoxModel5.addElement("Name");
    defaultComboBoxModel5.addElement("Skill");
    sortPersonByCbox.setModel(defaultComboBoxModel5);
    sortByCardPanel.add(sortPersonByCbox, "person");
    sortOrderCbox = new JComboBox<>();
    sortOrderCbox.setEnabled(false);
    final DefaultComboBoxModel<String> defaultComboBoxModel6 = new DefaultComboBoxModel<>();
    defaultComboBoxModel6.addElement("Asc");
    defaultComboBoxModel6.addElement("Desc");
    sortOrderCbox.setModel(defaultComboBoxModel6);
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.weightx = 0.2;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 5, 0, 5);
    sortPanel.add(sortOrderCbox, gbc);
    sortButton = new JButton();
    sortButton.setEnabled(false);
    sortButton.setText("Sort");
    gbc = new GridBagConstraints();
    gbc.gridx = 3;
    gbc.gridy = 0;
    gbc.weightx = 0.15;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 5, 0, 10);
    sortPanel.add(sortButton, gbc);
    searchPanel = new JPanel();
    searchPanel.setLayout(new GridBagLayout());
    searchPanel.setEnabled(false);
    searchPanel.setMinimumSize(new Dimension(450, 69));
    searchPanel.setPreferredSize(new Dimension(450, 69));
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.weightx = 0.3;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0, 10, 0, 0);
    searchSortContainer.add(searchPanel, gbc);
    searchPanel.setBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Search"));
    searchField = new JTextField();
    searchField.setEnabled(false);
    searchField.setPreferredSize(new Dimension(0, 26));
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.5;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 10, 0, 5);
    searchPanel.add(searchField, gbc);
    targetCBox = new JComboBox<>();
    targetCBox.setEnabled(false);
    targetCBox.setMinimumSize(new Dimension(50, 30));
    final DefaultComboBoxModel<String> defaultComboBoxModel7 = new DefaultComboBoxModel<>();
    defaultComboBoxModel7.addElement("");
    defaultComboBoxModel7.addElement("Name");
    defaultComboBoxModel7.addElement("Type");
    defaultComboBoxModel7.addElement("Person: Skill");
    defaultComboBoxModel7.addElement("Ship: Max Length");
    defaultComboBoxModel7.addElement("Ship: Max Width");
    targetCBox.setModel(defaultComboBoxModel7);
    targetCBox.setPreferredSize(new Dimension(0, 26));
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 0.3;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 5, 0, 5);
    searchPanel.add(targetCBox, gbc);
    searchButton = new JButton();
    searchButton.setEnabled(false);
    searchButton.setPreferredSize(new Dimension(0, 27));
    searchButton.setText("Search");
    searchButton.setToolTipText("Perform search");
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.weightx = 0.2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 5, 0, 10);
    searchPanel.add(searchButton, gbc);
    final JPanel spacer5 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 3;
    gbc.gridy = 0;
    gbc.weightx = 0.15;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    searchSortContainer.add(spacer5, gbc);
    final JPanel spacer6 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.15;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    searchSortContainer.add(spacer6, gbc);
    outputPanel = new JPanel();
    outputPanel.setLayout(new GridBagLayout());
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(10, 10, 10, 10);
    mainTabPanel.add(outputPanel, gbc);
    outputPanel
            .setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
    outputSplitPane = new JSplitPane();
    outputSplitPane.setEnabled(false);
    outputSplitPane.setOneTouchExpandable(true);
    outputSplitPane.setOrientation(0);
    outputSplitPane.setResizeWeight(0.5);
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    outputPanel.add(outputSplitPane, gbc);
    outputSplitPane
            .setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
    dataPanel = new JPanel();
    dataPanel.setLayout(new GridBagLayout());
    dataPanel.setEnabled(false);
    outputSplitPane.setRightComponent(dataPanel);
    dataPanel.setBorder(BorderFactory.createTitledBorder(null, "Sea Port Data", TitledBorder.CENTER,
            TitledBorder.ABOVE_TOP));
    scrollPaneBottom = new JScrollPane();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    dataPanel.add(scrollPaneBottom, gbc);
    scrollPaneBottom
            .setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
    outputArea = new JTextArea();
    outputArea.setEditable(false);
    Font outputAreaFont = new Font("Monospaced",Font.PLAIN,12);
    outputArea.setFont(outputAreaFont);
    outputArea.setWrapStyleWord(false);
    scrollPaneBottom.setViewportView(outputArea);
    topPanel = new JPanel();
    topPanel.setLayout(new GridBagLayout());
    outputSplitPane.setLeftComponent(topPanel);
    topPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
    topSplitPane = new JSplitPane();
    topSplitPane.setEnabled(false);
    topSplitPane.setResizeWeight(0.8);
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    topPanel.add(topSplitPane, gbc);
    topSplitPane
            .setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
    treePanel = new JPanel();
    treePanel.setLayout(new GridBagLayout());
    treePanel.setEnabled(false);
    topSplitPane.setRightComponent(treePanel);
    treePanel.setBorder(BorderFactory
            .createTitledBorder(null, "World", TitledBorder.CENTER, TitledBorder.ABOVE_TOP));
    treeScroll = new JScrollPane();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    treePanel.add(treeScroll, gbc);
    treeScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
    resultsPanel = new JPanel();
    resultsPanel.setLayout(new GridBagLayout());
    resultsPanel.setEnabled(false);
    topSplitPane.setLeftComponent(resultsPanel);
    resultsPanel.setBorder(BorderFactory
            .createTitledBorder(null, "Search Results", TitledBorder.CENTER,
                    TitledBorder.ABOVE_TOP));
    searchScrollPane = new JScrollPane();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    resultsPanel.add(searchScrollPane, gbc);
    searchScrollPane
            .setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
    searchResultArea = new JTextArea();
    searchResultArea.setEditable(false);
    Font searchResultAreaFont = new Font("Monospaced",Font.PLAIN,12);
    searchResultArea.setFont(searchResultAreaFont);
    searchScrollPane.setViewportView(searchResultArea);
    jobTabPanel = new JPanel();
    jobTabPanel.setLayout(new GridBagLayout());
    mainTabbedPane.addTab("Job Manager", jobTabPanel);
    jobStatusPanel = new JPanel();
    jobStatusPanel.setLayout(new GridBagLayout());
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 5;
    gbc.gridheight = 2;
    gbc.weightx = 1.0;
    gbc.weighty = 0.7;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(10, 10, 10, 10);
    jobTabPanel.add(jobStatusPanel, gbc);
    jobStatusPanel.setBorder(BorderFactory
            .createTitledBorder(null, "Job Status", TitledBorder.CENTER, TitledBorder.BELOW_TOP));
    jobScrollPane = new JScrollPane();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    jobStatusPanel.add(jobScrollPane, gbc);
    portSelectionPanel = new JPanel();
    portSelectionPanel.setLayout(new GridBagLayout());
    portSelectionPanel.setMinimumSize(new Dimension(180, 39));
    portSelectionPanel.setPreferredSize(new Dimension(180, 10));
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weighty = 0.3;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(10, 10, 0, 5);
    jobTabPanel.add(portSelectionPanel, gbc);
    portSelectionPanel.setBorder(BorderFactory
            .createTitledBorder(null, "Port Selection", TitledBorder.CENTER,
                    TitledBorder.BELOW_TOP));
    portSelectionScrollPane = new JScrollPane();
    portSelectionScrollPane.setPreferredSize(new Dimension(180, 0));
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    portSelectionPanel.add(portSelectionScrollPane, gbc);
    portSelectionScrollPane
            .setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
    portResourceContainerPanel = new JPanel();
    portResourceContainerPanel.setLayout(new BorderLayout(0, 0));
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.gridwidth = 4;
    gbc.weightx = 1.0;
    gbc.weighty = 0.3;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(10, 0, 0, 10);
    jobTabPanel.add(portResourceContainerPanel, gbc);
  }
}
