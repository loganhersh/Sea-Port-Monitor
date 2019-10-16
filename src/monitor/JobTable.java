package monitor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import monitor.Job.Status;

/**
 * File: Port.java
 * Date: 24 June 2019
 * @author Logan Hershberger
 * Purpose: JTable subclass specifically to handle Job objects.
 *          Packaged with custom TableModel and Renderers/Editors
 */
public class JobTable extends JTable implements Runnable{

  static final long serialVersionUID = 1L;
  private ArrayList<Job> data = new ArrayList<>();
  private HashMap<Job,ArrayList<Object>> rowData = new HashMap<>();
  enum SortColumn {PORT, SHIP, JOB}
  enum SortOrder {ASCENDING, DESCENDING}
  private SortColumn currentCol = null;
  private SortOrder currentOrder = null;
  private boolean jobsComplete;
  private boolean killFlag = false;

  // Construct and configure table with default tablemodel
  JobTable(){
    setModel(new JobTableModel());
    getColumn("Progress").setCellRenderer((table, value, isSelected, hasFocus, row,
            column) -> (JProgressBar) value);
    ButtonCellRenderer renderer = new ButtonCellRenderer();
    ButtonCellEditor editor = new ButtonCellEditor();
    getColumn("Suspend/Resume").setCellRenderer(renderer);
    getColumn("Cancel").setCellRenderer(renderer);
    getColumn("Suspend/Resume").setCellEditor(editor);
    getColumn("Cancel").setCellEditor(editor);
    getColumn("Status").setCellRenderer(new StatusCellRenderer());
    setRowHeight(30);
    setIntercellSpacing(new Dimension(0, 5));

    getTableHeader().addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        int col = columnAtPoint(e.getPoint());
        switch(col){
          case 0:
            sort(SortColumn.PORT);
            return;
          case 1:
            sort(SortColumn.SHIP);
            return;
          case 2:
            sort(SortColumn.JOB);
            return;
        }
      }
    });
  }

  // add a job to the data set and build its table row
  void addJob(Job job){
    data.add(job);
    ArrayList<Object> row = new ArrayList<>();
    row.add(job.getPort().getName());
    row.add(job.getShip().getName());
    row.add(job.getName());

    if(job.getShip().dock == null)
      row.add("Port queue");
    else
      row.add(job.getShip().dock.getName());

    String s = "";
    for(int i=0; i<job.requirements.size(); i++){
      s += job.requirements.get(i);
      if((i + 1) != job.requirements.size())
        s += ", ";
    }
    row.add(s);

    row.add(new JProgressBar(0));
    row.add("Waiting...");
    JButton suspendBtn = new JButton("Suspend");
    suspendBtn.setEnabled(false);
    suspendBtn.addActionListener(e -> job.toggleGoFlag());
    row.add(suspendBtn);

    JButton cancelBtn = new JButton("Cancel");
    cancelBtn.setEnabled(false);
    cancelBtn.addActionListener(e -> job.kill());
    row.add(cancelBtn);
    rowData.put(job,row);
  }


  /**
   * Sorts the job table depending on the given order.
   * If the order is the same as the currentCol, the sorting is reversed
   * @param col Column to sort by
   */
  void sort(SortColumn col){
    if(col == currentCol && col != SortColumn.PORT){
      Collections.reverse(data);
      return;
    }

    Comparator<Job> comp = null;
    SortOrder newOrder = null;
    switch(col){
      case PORT:
        // reverse port sorting but keep ships in alphabetical order
        if(col == currentCol) {
          if(currentOrder == SortOrder.ASCENDING){
            comp = Comparator.comparing(Job::getPort).reversed().thenComparing(Job::getShip);
            newOrder = SortOrder.DESCENDING;
            break;
          }
        }
        comp = Comparator.comparing(Job::getPort).thenComparing(Job::getShip);
        newOrder = SortOrder.ASCENDING;
        break;
      case SHIP:
        comp = Comparator.comparing(Job::getShip);
        break;
      case JOB:
        comp = Comparator.comparing(Job::getName);
        break;
    }

    if(comp != null){
      data.sort(comp);
      currentCol = col;
      currentOrder = newOrder;
    }
  }


  /*
   * Updates the job table every 100ms (or 10ms for test mode)
   * Stops running when all jobs are done or cancelled
   */
  public void run(){
    int numDone;

    while(true && !killFlag) {
      numDone = 0;

      int delay = SeaPortMonitor.testMode ? 10 : 100;
      try{
        Thread.sleep(delay);
      } catch(InterruptedException e){}

      for (Entry<Job, ArrayList<Object>> p : rowData.entrySet()) {
        Job job = p.getKey();
        ArrayList<Object> row = p.getValue();

        // Update ship locations
        if(job.ship.port == null)
          row.set(3,"Departed");
        else if (job.getShip().dock == null)
          row.set(3, "Port queue");
        else
          row.set(3, job.getShip().dock.getName());

        // update progress bar
        ((JProgressBar) row.get(5)).setValue(job.getProgress());

        // update job status
        Status st = job.getStatus();
        JButton suspendBtn = (JButton) row.get(7);
        JButton cancelBtn = (JButton) row.get(8);
        switch (st) {
          case RUNNING:
            row.set(6, "Running...");
            suspendBtn.setEnabled(true);
            cancelBtn.setEnabled(true);
            suspendBtn.setText("Suspend");
            break;
          case SUSPENDED:
            row.set(6, "Suspended");
            suspendBtn.setText("Resume");
            break;
          case WAITINGTODOCK:
            row.set(6, "Waiting for ship to dock");
            break;
          case WAITINGFORRESOURCES:
            row.set(6,"Waiting for resources");
            break;
          case DONE:
            row.set(6, "Done");
            suspendBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            numDone++;
            break;
          case CANCELLED:
            row.set(6, "CANCELLED");
            suspendBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            numDone++;
            break;
          case LACKOFRESOURCES:
            row.set(6,"CANCELLED (resources)");
            suspendBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            numDone++;
        }
      }
      resizeAndRepaint();

      if(numDone == data.size()){
        // All jobs are done, set stop flag
        // loop one more time to update the last ship, then stop updating the table
        if(jobsComplete)
          return;
        jobsComplete = true;
      }
    }
  }

  boolean isJobsComplete(){
    return jobsComplete;
  }

  /**
   * sets kill flag to kill the table's update thread
   */
  void kill(){
    killFlag = true;
  }

  /**
   * Checks for the given job and then selects and scrolls to the job's row in the table
   * @param jobname
   */
  void setSelectedJob(String jobname){
    int index = -1;
    int i = 0;
    // find the job
    for(Job j: data){
      if(j.getName().equals(jobname)){
        index = i;
        break;
      }
      i++;
    }
    // if job was found, set it selected and scroll to it
    if(index >= 0){
      Rectangle r = getCellRect(index,3,true);
      scrollRectToVisible(r);
      setRowSelectionInterval(index,index);
    }
  }

  @Override
  public Component prepareRenderer(TableCellRenderer renderer, int row, int col){
    Component comp = super.prepareRenderer(renderer,row,col);
    // Color the suspend/cancel buttons
    if(col >= 7){
      comp.setBackground(Color.lightGray);
    }
    return comp;
  }


  // Custom TableModel for JobTable
  private class JobTableModel extends AbstractTableModel {

    static final long serialVersionUID = 1L;

    private String[] cols = {"Port", "Ship", "Job Name", "Ship Location", "Needed Skills",
            "Progress", "Status","Suspend/Resume", "Cancel"};

    @Override
    public int getRowCount() {
      return rowData.size();
    }

    @Override
    public int getColumnCount() {
      return cols.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
      return rowData.get(data.get(row)).get(col);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
       return (col >= 7);
    }

    @Override
    public String getColumnName(int col) {
      return cols[col];
    }
  }

  // Custom TableCellRenderer for JButton cells
  private class ButtonCellRenderer extends JButton implements TableCellRenderer {
    static final long serialVersionUID = 1L;
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
      return (JButton) value;
    }
  }

  // Custom TableCellRenderer for the status field
  private class StatusCellRenderer implements TableCellRenderer {

    // Changes font color to red when the job is cancelled
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
      JTextField text = new JTextField((String) value);
      text.setBorder(new EmptyBorder(0,0,0,0));
      if(isSelected){
        text.setBackground(table.getSelectionBackground());
        text.setForeground(table.getSelectionForeground());
        text.setBorder(new LineBorder(table.getSelectionBackground(),2));
      }
      if(rowData.get(data.get(row)).get(6) == "CANCELLED" ||
              rowData.get(data.get(row)).get(6) == "CANCELLED (resources)"){
        text.setForeground(Color.RED);
      }
      return text;
    }

  }

  // Custom cell editor for JobTable buttons
  private class ButtonCellEditor extends AbstractCellEditor implements TableCellEditor {
    static final long serialVersionUID = 1L;
    @Override
    public Object getCellEditorValue() {
      return rowData.get(data.get(getSelectedRow())).get(getSelectedColumn());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
      return (JButton)value;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
      // Allow buttons to be clicked
      if (anEvent instanceof MouseEvent) {
        MouseEvent event = (MouseEvent) anEvent;
        return (event.getClickCount() == 1);
      } else {
        return false;
      }
    }
  }

}
