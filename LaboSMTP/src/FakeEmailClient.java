/*--------------------------------------------------------------------------------
* Author: Samuel Darcey
* Date: 06.04.2016
* Goal: This is a telnet client that is able to send fake emails with random message to random people. This will use
*       the Protocol class but can be used with any telnet protocol that is able to send message! This class require a
*       configuration file named "config.txt" at the root of the working directory.
* --------------------------------------------------------------------------------
* */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
* This is a Client for a telnet connection.
* */
public class FakeEmailClient {
    private static final Logger LOG = Logger.getLogger(FakeEmailClient.class.getName());

    private Protocol telnet;
    private ArrayList<String> emails;
    private ArrayList<String> messages;
    private String helo;
    private String mailFrom;
    private String address;
    private int port;
    private String emailsFilename = "contact.txt";
    private String messagesFilename = "messages.txt";


    // The constructor open a socket for a new communication with the address and port in the config.txt file
    public FakeEmailClient() {
        LOG.log(Level.INFO, "Welcome in this telnet client for sending dummy mail to dummy people!");

        setConfig("config.txt");

        telnet = new Protocol(address, port);

        // Getting the emails
        emails = readLinesFromFile(emailsFilename);
        if (emails.isEmpty()) {
            throw new RuntimeException("No emails in the file!! Aborting...");
        }
        LOG.log(Level.INFO, "Reading Email done!");

        // Getting the messages
        messages = readLinesFromFile(messagesFilename);
        if (messages.isEmpty()) {
            throw new RuntimeException("No messages in the file!! Aborting...");
        }
        LOG.log(Level.INFO, "Reading Messages done!");

        // Shuffle the lists for random generation
        Collections.shuffle(emails);
        Collections.shuffle(messages);

    }

    // Read the configuration file and set the variable in the application
    private void setConfig(String filename){
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            String line = input.readLine();

            while (line != null) {
                // Skip the line that are commented with '#'
                if(line.equals("") || line.charAt(0) == '#'){
                    line = input.readLine();
                    continue;
                }

                // Get the parameter and the value of the config in this line
                int index = line.indexOf('=');
                if(index == -1){
                    throw new RuntimeException("Couldn't split a line in the configuration file. '=' not found!" );
                }
                String param = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();

                // Set the variable for the good parameter
                switch (param){
                    case "HELO":
                        helo = value;
                        break;
                    case "Mail from":
                        mailFrom = value;
                        break;
                    case "Emails filename":
                        emailsFilename = value;
                        break;
                    case "Messages filename":
                        messagesFilename = value;
                        break;
                    case "Address":
                        address = value;
                        break;
                    case "Port":
                        port = Integer.valueOf(value);
                        break;
                    default:
                        throw new RuntimeException("Unknown parameter in configuration file! Found: " + param);
                }

                line = input.readLine();
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("The input file couldn't be read! " + e.getMessage());
        }
    }

    // Send all the fake message with the options set in the configuration file
    public void sendFakeMessages() {

        // Asking for the number of groups
        Scanner scan = new Scanner(System.in);
        int numGroup = 0;

        while (numGroup <= 0){
            System.out.print("How many group do you want? (> 0): ");
            if (scan.hasNextInt()){
                numGroup = scan.nextInt();
            } else {
                scan.next();
            }
        }

        // Check if the number of groups is possible with the number of emails
        if (emails.size() < numGroup * 3) {
            throw new RuntimeException("Not enough email for creating " + numGroup + " groups! Aborting");
        }
        int nbEmailPerMail = (emails.size() + numGroup - 1) / numGroup;

        // Loop on the number of groups and send the messages
        int message = 0;
        for (int nbAdresse = 0; nbAdresse < emails.size(); ) {
            // Set the fake sender
            String fakeFrom = emails.get(nbAdresse++);

            // Choose a message
            ArrayList<String> rcpt = new ArrayList<>();
            if (message >= messages.size()) {
                message = 0;
            }

            // Set all the receiver
            int i = nbAdresse;
            while (i < nbAdresse + nbEmailPerMail - 1 && i < emails.size()) {
                rcpt.add(emails.get(i++));
            }
            nbAdresse = i;
            // Send the message
            telnet.sendMessage(helo, mailFrom, rcpt, fakeFrom, readData(messages.get(message++)));
        }

        LOG.log(Level.INFO, "Sended! Quiting...");
    }

    // Reading all the lines from a file and put them in a array
    private ArrayList<String> readLinesFromFile(String filename) {
        ArrayList<String> ret = new ArrayList<>();

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            String line = input.readLine();
            while (line != null) {
                ret.add(line);
                line = input.readLine();
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("The file" + filename + "couldn't be read! " + e.getMessage());
        }
        return ret;
    }

    // Read all the data inside a file (This is for getting the message from the array of files)
    private String readData(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)), Charset.forName("utf8"));
        } catch (java.io.IOException e) {
            throw new RuntimeException("The file " + filename + " couldn't be read! " + e.getMessage());
        }
    }
}
