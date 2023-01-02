package il.ac.kinneret.mjmay.multicastchat;

import javafx.concurrent.Task;

import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Calendar;

public class ListeningTask extends Task<Void> {

    /**
     * The socket that we use to listen for incoming multicast messages
     */
    private final MulticastSocket socket;

    /**
     * Builds a task to listen for incoming messages
     * @param socket The multicast socket to listen on
     */
    public ListeningTask(MulticastSocket socket) {
        this.socket = socket;
    }
    /**
     * Runs when the task starts.
     * @return Nothing
     * @throws Exception If something is wrong while listening
     */



    @Override
    protected Void call() throws Exception {

        try {
            while (!isCancelled()) {
                /// TODO: Listen for incoming messages and process them
                byte[] buffer = new byte[1024];

                // create a DatagramPacket to receive messages
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while(true){
                    socket.receive(packet);
                    // get the message from the packet
                    String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
        updateMessage(message);
                }
            }
        }
        catch (Exception ex) {
            updateMessage("Error receiving message: " + ex.getMessage() + "\n");
        }
        return null;
    }
}
