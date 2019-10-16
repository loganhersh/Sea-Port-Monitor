package monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * File: ShipDetailsFrame.java
 * Date: 21 June 2019
 * @author Logan Hershberger
 * Purpose: Small frame that displays the details of a Ship
 */
public class ShipDetailsFrame extends JFrame {

  static final long serialVersionUID = 1L;

  // constructs and configures the JFrame
  ShipDetailsFrame(Ship ship){
    super("Ship Details");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setSize(300,280);
    setLocationByPlatform(true);
    setResizable(false);
    setLayout(new BorderLayout());

    JLabel header;
    if(ship instanceof CargoShip){
      header = new JLabel("Cargo Ship");
    } else if(ship instanceof PassengerShip){
      header = new JLabel("Passenger Ship");
    } else {
      header = new JLabel("Ship");
    }

    header.setFont(new Font(header.getFont().getFontName(), Font.BOLD, 20));
    header.setHorizontalAlignment(JLabel.CENTER);
    add(header, BorderLayout.NORTH);
    add(getShipDetailsPanel(ship),BorderLayout.CENTER);
    setVisible(true);
  } // end constructor


  // Retrieves ship details and
  private JPanel getShipDetailsPanel(Ship ship){
    JPanel detailsPanel = new JPanel(new BorderLayout());
    detailsPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0,10,10,10),
            new TitledBorder(" ")));

    JTextArea detailsArea = new JTextArea();
    Color c = new Color(detailsArea.getBackground().getRGB());
    detailsArea.setBackground(c);
    detailsArea.setEditable(false);
    detailsArea.setBorder(null);
    detailsArea.setFont(new Font("Consolas",Font.PLAIN,12));

    detailsArea.setText(String.format("%15s:   %-15s\n", "Name",ship.getName()));
    detailsArea.append(String.format("%15s:   %-15s\n","Port",ship.port.getName()));
    String s;
    if(ship.dock != null){
      s = ship.dock.getName();
    } else {
      s = (ship.port.queue.contains(ship)) ? "Queued" : "Departed";
    }
    detailsArea.append(String.format("%15s:   %-15s\n","Location",s));
    detailsArea.append(String.format("%15s:   %-15s\n","Length",ship.getLength()));
    detailsArea.append(String.format("%15s:   %-15s\n","Width",ship.getWidth()));
    detailsArea.append(String.format("%15s:   %-15s\n","Weight",ship.getWeight()));
    detailsArea.append(String.format("%15s:   %-15s\n","Draft",ship.getDraft()));
    if(ship instanceof CargoShip){
      detailsArea.append(String.format("%15s:   %-15s\n","Cargo Weight",
              ((CargoShip) ship).cargoWeight));
      detailsArea.append(String.format("%15s:   %-15s\n","Cargo Volume",
              ((CargoShip) ship).cargoVolume));
      detailsArea.append(String.format("%15s:   %-15s\n","Cargo Value",
              ((CargoShip) ship).cargoValue));
    } else if(ship instanceof PassengerShip){
      detailsArea.append(String.format("%15s:   %-15s\n","Passengers",
              ((PassengerShip) ship).numberOfPassengers));
      detailsArea.append(String.format("%15s:   %-15s\n","Total Rooms",
              ((PassengerShip) ship).numberOfRooms));
      detailsArea.append(String.format("%15s:   %-15s\n","Occupied Rooms",
              ((PassengerShip) ship).numberOfOccupiedRooms));
    }
    detailsArea.append(String.format("%15s:   %-15s\n","Jobs remaining",
            (ship.jobs.isEmpty()) ? 0 : ship.jobs.size()));

    detailsPanel.add(detailsArea, BorderLayout.CENTER);

    return detailsPanel;
  } // end getShipDetailsPanel

} // end class
