package il.ac.kinneret.mjmay.multicastchat;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

public class MulticastChatController implements Initializable {

    public TextField tfAddress, tfPort, tfMessage;
    public Spinner spLocalAddress;
    public Button bJoin, bLeave, bSend;
    public TextArea taLog;
    public ComboBox cbLocalAddresses;
    MulticastSocket multicastSocket;
    ListeningTask task;
    Thread listeningThread;

    /**
     * Validate that the address in the multicast address box is valid
     * @return True if the address is a valid mutlicast address.  False otherwise
     */
    private boolean validateMulticastIP()
    {
        try {
            InetAddress multicastAddress = InetAddress.getByName(tfAddress.getText());
            if ( multicastAddress.isMulticastAddress()) {
                setValidColor(tfAddress);
                return true;
            }
            else
            {
                setInvalidColor(tfAddress);
                return false;
            }
        }
        catch ( Exception ex)
        {
            setInvalidColor(tfAddress);
            return false;
        }
    }

    /**
     * Sets the field to a background that shows it's valid
     * @param tf The textfield to update
     */
    private void setValidColor (TextField tf)
    {
        tf.setStyle("-fx-background-color: white");
    }

    /**
     * Sets the field to a background that shows it's invalid
     * @param tf The textfield to update
     */
    private void setInvalidColor (TextField tf)
    {
        tf.setStyle("-fx-background-color: salmon");
    }

    /**
     * Validate that the value in the port field is valid
     * @return True if the value is a valid port. False otherwise.
     */
    private boolean validatePort()
    {
        int port = 0;
        try {
            port = Integer.parseInt(tfPort.getText());
            setValidColor(tfPort);
            if ( port > 0 && port < 65535)
            {

                return true;
            }
        }
        catch (Exception ex)
        {
            setInvalidColor(tfPort);
        }
        return false;
    }

    /**
     * Runs when the user selects to join a multicast group.
     * @param event Ignored
     */
    public void joinGroup(ActionEvent event) throws IOException {
        // check that things are doable
        if ( validatePort() && validateMulticastIP()) {
            /// TODO: Fill in the logic to join a group
            /// TODO: Use the taLog area to record when you have joined the group or failed to do so
            int port = Integer.parseInt(tfPort.getText());
            multicastSocket=new MulticastSocket(port);
            NetworkInterface interfaceName = (NetworkInterface) cbLocalAddresses.getSelectionModel().getSelectedItem();
            //NetworkInterface ni = NetworkInterface.getByName(interfaceName);
            InetAddress groupAddress=InetAddress.getByName(tfAddress.getText());
            InetSocketAddress inetSocketAddress = new InetSocketAddress(groupAddress,port);
            multicastSocket.joinGroup(inetSocketAddress,interfaceName);
            taLog.appendText("Joined the group\n");
             task = new ListeningTask(multicastSocket);
            listeningThread = new Thread(task);
          task.messageProperty().addListener(new ChangeListener<String>() {
              @Override
              public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                  taLog.appendText(t1 + "\n");
              }
          });
listeningThread.start();
        }
    }

    /**
     * Sends a message to the multicast group
     * @param event Ignored
     */
    public void sendMessage(ActionEvent event) throws IOException {
        if ( tfMessage.getText().length() > 0 && multicastSocket != null && validateMulticastIP() && validatePort())
        {   if(multicastSocket==null)
        {
            joinGroup(event);

        }
            /// TODO: Fill in the logic to send a message. Add a note to the taLog area when it's been sent.
            String message=tfMessage.getText();
            byte[] data=message.getBytes();


            InetAddress groupAddress = InetAddress.getByName(tfAddress.getText());
            int port = Integer.parseInt(tfPort.getText());
            LocalDateTime now = LocalDateTime.now();
            //DatagramPacket
            DatagramPacket datagramPacket= new DatagramPacket(data,data.length,groupAddress,port);
            InetAddress senderIP = InetAddress.getLocalHost();
            // send the packet
            multicastSocket.send(datagramPacket);

            // add a message to the log TextArea to indicate that the message has been sent
            tfMessage.setText("");
            taLog.appendText(now.toString() + " - " + senderIP.toString()+ ": " + tfMessage.getText()+"\n");

        }
    }


    /**
     * Leaves the multicast group
     * @param event Ignored
     */
    public void leaveGroup (ActionEvent event)  throws IOException {
        // check that things are doable
        if ( validatePort() && validateMulticastIP() && multicastSocket != null)
        {
            System.out.println("We are inside leave group");
            // TODO: Fill in the logic to leave the group.  Add a note to the taLog area when it's done.\\
            // check if we are connected
            //multicastSocket.isConnected()?
            if( multicastSocket!=null)
            {
                System.out.println(multicastSocket.isConnected());
                //create an inetAddress object representing multicast group address
                InetAddress groupAddress=InetAddress.getByName(tfAddress.getText());
                NetworkInterface interfaceName = (NetworkInterface) cbLocalAddresses.getSelectionModel().getSelectedItem();
                //NetworkInterface ni = NetworkInterface.getByName(interfaceName);
                //create an inetsocketAddress representing multicast group address and port,
                InetSocketAddress inetSocketAddress = new InetSocketAddress(groupAddress,multicastSocket.getLocalPort());
                //calls leavegroup method on the multicast socket,we give it the inetsocketaddress
                //and the name of network interface we chose
                multicastSocket.leaveGroup(inetSocketAddress,interfaceName);
                //close multicast socket
                multicastSocket.close();
                //change to null , indicates we are no longer member of multicast group
                multicastSocket=null;
                taLog.appendText(("Left The Group \n"));
                //stop our task and thread from listening
                task.cancel();
                listeningThread.interrupt();
            }
        }
    }


    /**
     * Gets a list of the network interfaces on the computer.  Puts the list in a Vector.
     * @return  A Vector with the network interfaces for the computer.
     */
    public static Vector<NetworkInterface> getInterfaces()
    {
        // make a list of addresses to choose from
        // add in the usual ones
        Vector<NetworkInterface> interfaces = new Vector<NetworkInterface>();
        try {
            // get the local IP addresses from the network interface listing
            Enumeration<NetworkInterface> inf = NetworkInterface.getNetworkInterfaces();

            while (inf.hasMoreElements()) {
                NetworkInterface ni = inf.nextElement();
                interfaces.add(ni);

            }
        }
        catch (SocketException ex)
        {
            // can't get local addresses, something's wrong
            System.out.println("Can't get network interface information: " + ex.getLocalizedMessage());
        }
        return interfaces;
    }

    /**
     * Initializes the GUI with some useful values
     * @param url Ignored
     * @param resourceBundle Ignored
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ObservableList<NetworkInterface> interfaces = FXCollections.observableArrayList(getInterfaces());
        cbLocalAddresses.setItems(interfaces);

        tfPort.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                validatePort();
            }
        });

        tfAddress.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                validateMulticastIP();
            }
        });
    }
    /**
     * Gets the local IP addresses for the computer and puts them in a Vector.  This method is not used in the project.
     * @return  A Vector with the local IP addresses of the computer.
     * @deprecated Not used in the project.
     */
    public static Vector<InetAddress> getLocalIPs()
    {
        // make a list of addresses to choose from
        // add in the usual ones
        Vector<InetAddress> adds = new Vector<InetAddress>();
        try {
            adds.add(InetAddress.getByAddress(new byte[] {0, 0, 0, 0}));
            adds.add(InetAddress.getByAddress(new byte[] {127, 0, 0, 1}));
        } catch (UnknownHostException ex) {
            // something is really weird - this should never fail
            System.out.println("Can't find IP address 0.0.0.0: " + ex.getMessage());
            ex.printStackTrace();
            return adds;
        }

        try {
            // get the local IP addresses from the network interface listing
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while ( interfaces.hasMoreElements() )
            {
                NetworkInterface ni = interfaces.nextElement();
                // see if it has an IPv4 address
                Enumeration<InetAddress> addresses =  ni.getInetAddresses();
                while ( addresses.hasMoreElements())
                {
                    // go over the addresses and add them
                    InetAddress add = addresses.nextElement();
                    // make sure it's an IPv4 address
                    if (!add.isLoopbackAddress() && add.getClass() == Inet4Address.class)
                    {
                        adds.addElement(add);
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            // can't get local addresses, something's wrong
            System.out.println("Can't get network interface information: " + ex.getLocalizedMessage());
        }
        return adds;
    }
}
