package com.jbmo60927.thread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jbmo60927.App;
import com.jbmo60927.entities.Player;

public class ServiceThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(ServiceThread.class.getName()); //logger for this class
    private final int clientNumber; //id of the client
    private final Socket socketOfServer; //socket of the client
    private final App app; //link to the data
    private final BufferedReader is; //input stream
    private final BufferedWriter os; //output stream

    /**
     * thread to communicate with a unique client
     * @param socketOfServer socket of the client
     * @param clientNumber id of the client
     * @param app link to the data
     * @throws IOException exception that could occure when the communication is closed badly 
     */
    public ServiceThread(final Socket socketOfServer, final int clientNumber, final App app) throws IOException {
        LOGGER.setLevel(Level.INFO);
        this.clientNumber = clientNumber;
        this.socketOfServer = socketOfServer;
        this.app = app;

        //open input and output streams
        is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
        os = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));

        
        //receive data from client
        String line;
        do {
            line = is.readLine();
        } while (line.split(" ")[0].compareTo("INITPLAYER") != 0);
        final Float xPos = Float.parseFloat(line.split(" ")[1]);
        final Float yPos = Float.parseFloat(line.split(" ")[2]);
        final String name = line.split(" ")[3];

        //add player
        app.getPlayers().put(this, new Player(xPos, yPos, name));

        //send client parameters to all other clients
        broadcast(String.format("NEWPLAYER %d %s", clientNumber, clientNumber+line.replace("INITPLAYER ", "")));

        for (final ServiceThread thread : app.getPlayers().keySet()) {
            if(thread != this) {
                os.write(String.format("NEWPLAYER %d %s %s %s", thread.getClientNumber(), app.getPlayers().get(thread).getX(), app.getPlayers().get(thread).getY(), app.getPlayers().get(thread).getName()));
                os.newLine();
                os.flush();
            }
        }

        //send data to client
        os.write("INITDATA ");
        os.newLine();
        os.flush();

        //log connection
        LOGGER.log(Level.INFO, () -> String.format("New connection with client# %d at %s named %s", this.clientNumber, this.socketOfServer.getInetAddress().toString(), name));
    }

    /**
     * thread to receive and send data to the client
     */
    @Override
    public void run() {
        try {
            while (true) {

                //read data to the server (sent from client).
                final String line = is.readLine();

                //log the paquet
                LOGGER.log(Level.FINEST, () -> String.format("paquet received: %s", line));

                //new position for a player
                if (line.split(" ")[0].compareTo("PLAYER") == 0) {
                    app.getPlayers().get(this).setX(Float.parseFloat(line.split(" ")[1]));
                    app.getPlayers().get(this).setY(Float.parseFloat(line.split(" ")[2]));
                    app.getPlayers().get(this).setPlayerAction(Integer.parseInt(line.split(" ")[3]));
                    broadcast(String.format("PLAYER %d %s", clientNumber, line.replace("PLAYER ", "")));

                //if users send QUIT (To end conversation).
                } else if (line.equals("QUIT")) {
                    app.getPlayers().remove(this);
                    broadcast(String.format("REMOVEPLAYER %d", clientNumber));

                    LOGGER.log(Level.INFO, () -> String.format("Connection stop with client# %d at %s", this.clientNumber, this.socketOfServer.getInetAddress().toString()));
                    os.write("OK");
                    os.newLine();
                    os.flush();
                    break;
                
                //else display command
                } else {
                    LOGGER.log(Level.INFO, line);
                }
            }

        //error
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "error during the service thread", e);
        }
    }

    /**
     * allow to write output from outside of the thread
     * @return the buffer to write
     */
    public BufferedWriter getOs() {
        return os;
    }

    /**
     * get the id of the client
     * @return the id
     */
    public int getClientNumber() {
        return clientNumber;
    }

    /**
     * broadcast a paquet to every client
     * @param trame the paquet to broadcast
     * @throws IOException exception that could occure if something is wrong with the connection
     */
    private void broadcast(final String trame) throws IOException {
        //send data to every other clients
        for (final ServiceThread thread : app.getPlayers().keySet()) {
            if(thread != this) {
                thread.getOs().write(trame);
                thread.getOs().newLine();
                thread.getOs().flush();
            }
        }
    }
}