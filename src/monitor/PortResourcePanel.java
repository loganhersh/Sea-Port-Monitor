package monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * File: PortResourcePanel.java
 * Date: 13 July 2019
 * @author Logan Hershberger
 * Purpose: JPanel with a resource panel and a worker info panel that displays current details of
 *          a particular port's resources.
 */
public class PortResourcePanel extends JPanel implements Runnable{

  static final long serialVersionUID = 1L;
  private final Color fadedGreen = new Color(234,250,234);
  private final Color fadedGreenSelected = new Color(214,245,214);
  private final Color fadedRed = new Color(255,230,230);
  private final Color fadedRedSelected = new Color(255,204,204);
  private Object[] workerColNames = {"Name", "Skill", "Availability", "Location","Current Job"};
  private Object[] resourceColNames = {"Skill","Workers Available"};
  private TitledBorder border;
  private HashMap<String,Port> portmap;
  private JTable workerTable;
  private JTable resourceTable;
  private Port currentPort;
  private JPanel resourcesPanel;
  private JPanel workersPanel;
  private JScrollPane workerScrollPane;
  private JScrollPane resourceScrollPane;
  private ArrayList<Integer> resourceAvailabilityList;
  private SeaPortMonitor owner;
  private boolean killFlag = false;

  /**
   * Generates a JPanel based on a certain port. The panel contains a table of available
   * resources and a table of workers at that port. The port whose data is displayed can be changed
   * @param ports Arraylist of all the ports in the current world
   * @param owner The owning SeaPortProgram
   */
  public PortResourcePanel(ArrayList<Port> ports, SeaPortMonitor owner){

    this.owner = owner;
    portmap = new HashMap<>();
    // populate port hashmap
    for(Port p: ports){
      portmap.put(p.getName(),p);
    }
    currentPort = ports.get(0);   // set current port to the first port

    // create worker table
    workerTable = new JTable(generateWorkerModel());
    for(int i=0; i < workerColNames.length; i++){
      workerTable.getColumnModel().getColumn(i).setCellRenderer(new WorkerCellRenderer());
    }

    // listener for double-clicks on the worker table
    // if the clicked worker is assigned a job, the corresponding job is highlighted in the job
    // status table
    workerTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if(e.getClickCount() == 2){
          String s = (String)workerTable.getValueAt(workerTable.getSelectedRow(),4);
          if(s.equals("None")){
            return;
          }
          owner.setSelectedJob(s);
        }
      }
    });

    // create resources table
    resourceTable = new JTable(generateResourceModel());
    for(int i=0; i < resourceColNames.length; i++){
      resourceTable.getColumnModel().getColumn(i).setCellRenderer(new ResourceCellRenderer());
    }

    // configure main panel
    border = createTitledBorder("Port " + currentPort.getName());
    border.setTitleFont(new Font(border.getTitleFont().getFontName(), Font.BOLD, 16));
    setBorder(border);
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // configure the resources panel
    resourcesPanel = new JPanel(new BorderLayout());
    resourcesPanel.setBorder(createTitledBorder("Resources"));
    c.insets = new Insets(10,10,5,0);
    c.weightx = 0.3;
    c.weighty = 1.0;
    c.fill = GridBagConstraints.BOTH;
    resourceScrollPane = new JScrollPane();
    resourceScrollPane.setViewportView(resourceTable);
    resourceScrollPane.setBorder(new EmptyBorder(0,0,0,0));
    resourcesPanel.add(resourceScrollPane,BorderLayout.CENTER);
    add(resourcesPanel,c);

    // configure the workers panel
    workersPanel = new JPanel(new BorderLayout());
    workersPanel.setBorder(createTitledBorder("Workers"));
    c.insets = new Insets(10,10,5,10);
    c.weightx = 0.7;
    c.gridx = 1;
    workerScrollPane = new JScrollPane();
    workerScrollPane.setViewportView(workerTable);
    workerScrollPane.setBorder(new EmptyBorder(0,0,0,0));
    workersPanel.add(workerScrollPane,BorderLayout.CENTER);
    add(workersPanel,c);

    // start updating table
    new Thread(this).start();
  }

  /**
   * Changes the panel to display data for the new port
   * @param portName name of port whose data should be displayed
   */
  public void setPort(String portName){
    currentPort = portmap.get(portName);
    border.setTitle("Port " + currentPort.getName());
    // set worker table model for the new port
    workerTable.setModel(generateWorkerModel());
    for(int i=0; i < workerColNames.length; i++){
      workerTable.getColumnModel().getColumn(i).setCellRenderer(new WorkerCellRenderer());
    }
    // set resource table model for the new port
    resourceTable.setModel(generateResourceModel());
    for(int i=0; i < resourceColNames.length; i++){
      resourceTable.getColumnModel().getColumn(i).setCellRenderer(new ResourceCellRenderer());
    }
    repaint();
  }

  // Generates a new table model for the worker table
  private DefaultTableModel generateWorkerModel(){
    ArrayList<Person> persons = currentPort.persons;
    Object[][] rowData = new Object[persons.size()][workerColNames.length];

    for (int i = 0; i < persons.size(); i++) {
      Person p = persons.get(i);
      rowData[i][0] = p.getName();
      rowData[i][1] = p.getSkill();
      if (p.location instanceof Port) {
        rowData[i][2] = "Available";
        rowData[i][3] = "(" + p.location.getClass().getSimpleName() + ") " + p.location.getName();
        rowData[i][4] = "None";
      } else {
        rowData[i][2] = "Busy";
        rowData[i][3] = "Ship " + p.location.getName() + " at Dock " + ((Ship)p.location).dock.getName();
        rowData[i][4] = p.currentjob.getName();
      }
    }
    // make cells non-editable
    DefaultTableModel model = new DefaultTableModel(rowData, workerColNames){
      @Override
      public boolean isCellEditable(int row, int column){
        return false;
      }
    };
    return model;
  }

  // updates the provided tablemodel with current worker data
  private void updateWorkerModel(TableModel model){
    ArrayList<Person> persons = currentPort.persons;
    for (int i = 0; i < persons.size(); i++) {
      // prevents a person completing a job while the data is being updated
      synchronized (persons.get(i)) {
        Person p = persons.get(i);
        model.setValueAt(p.getName(), i, 0);
        model.setValueAt(p.getSkill(), i, 1);
        if (p.location instanceof Port) {
          model.setValueAt("Available", i, 2);
          model.setValueAt(
                  "(" + p.location.getClass().getSimpleName() + ") " + p.location.getName(), i, 3);
          model.setValueAt("None", i, 4);
        } else {
          model.setValueAt("Busy", i, 2);
          model.setValueAt("Ship " + p.location.getName() + " at Dock " + ((Ship) p.location).dock.getName(),
                  i, 3);
          model.setValueAt(p.currentjob.getName(), i, 4);
        }
      }
    }
  }

  // generates a new table model for the resource table
  private DefaultTableModel generateResourceModel(){
    resourceAvailabilityList = new ArrayList<>();
    HashMap<String,ArrayList<Person>> map = currentPort.getResourceMap();
    Object[][] rowData = new Object[map.size()][resourceColNames.length];

    int counter = 0;
    for(Entry<String,ArrayList<Person>> pair : map.entrySet()){
      rowData[counter][0] = pair.getKey();
      ArrayList<Person> tmpList = pair.getValue();
      resourceAvailabilityList.add(tmpList.size());
      rowData[counter][1] =
              tmpList.size() + " out of " + currentPort.totalResourceList.get(pair.getKey());
      counter++;
    }
    // make cells non-editable
    DefaultTableModel model = new DefaultTableModel(rowData,resourceColNames){
      @Override
      public boolean isCellEditable(int row, int column){
        return false;
      }
    };
    return model;
  }

  // updates the current available workers displayed in the resource table
  private void updateResourceModel(TableModel model){
    int counter = 0;
    synchronized (resourceAvailabilityList) {
      resourceAvailabilityList.clear();
      for (Entry<String, ArrayList<Person>> pair : currentPort.resourceMap.entrySet()) {
        int size = pair.getValue().size();
        String s = size + " out of " + currentPort.totalResourceList.get(pair.getKey());
        resourceAvailabilityList.add(size);
        model.setValueAt(s,counter,1);
        counter++;
      }
    }
  }

  // creates a centered, below top titledborder using the given string
  private TitledBorder createTitledBorder(String title){
    TitledBorder b = new TitledBorder(title);
    b.setTitleJustification(TitledBorder.CENTER);
    b.setTitlePosition(TitledBorder.BELOW_TOP);
    return b;
  }

  /**
   * While there are jobs running, keep updating the tables
   */
  public void run(){
    // While jobs are still running
    while(!owner.isJobsComplete() && !killFlag){
      try{
        Thread.sleep(200);
      } catch(InterruptedException e){}

      updateWorkerModel(workerTable.getModel());
      updateResourceModel(resourceTable.getModel());
      resourceTable.repaint();
    }
  }

  public void kill(){
    killFlag = true;
  }

  // Custom Table cell renderer for the worker table
  private class WorkerCellRenderer extends DefaultTableCellRenderer{
    static final long serialVersionUID = 1L;
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column){
      Component comp = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,
              column);
      Person person = currentPort.persons.get(row);
      if(person.isAvailable()){
        // paint the row green if the person is available
        comp.setBackground(fadedGreen);
        if(table.getSelectedRow() == row){
          comp.setBackground(fadedGreenSelected);
        }
      } else {
        // paint the row red if the person is not available
        comp.setBackground(fadedRed);
        if(table.getSelectedRow() == row){
          comp.setBackground(fadedRedSelected);
        }
      }
      setBorder(noFocusBorder);
      comp.setForeground(Color.BLACK);
      return comp;
    }
  }

  // custom table cell renderer for resource table
  private class ResourceCellRenderer extends DefaultTableCellRenderer{
    static final long serialVersionUID = 1L;
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column){
      Component comp = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,
              column);
      if(resourceAvailabilityList.get(row) == 0){
        // paint row red if all workers with this skill are in use
        comp.setBackground(fadedRed);
        if(table.getSelectedRow() == row){
          comp.setBackground(fadedRedSelected);
        }
      } else {
        // paint row green if there is at least one worker with this skill available
        comp.setBackground(fadedGreen);
        if(table.getSelectedRow() == row){
          comp.setBackground(fadedGreenSelected);
        }
      }
      setBorder(noFocusBorder);
      comp.setForeground(Color.BLACK);
      return comp;
    }
  }


}
