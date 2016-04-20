/*--------------------------------------------------------------------------------
* Author: Samuel Darcey
* Date: 06.04.2016
* Goal: Creating a protocol that is able to communicate with a telnet server and send emails through it. This class will
*       simply open a socket with the specify adresse and port and will send the data to the server as the SMTP
*       communication protocol (HELO -> MAIL FROM -> RCPT TO -> DATA) when the sendMessage() function is called.
* --------------------------------------------------------------------------------
* */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Protocol {

    private static final Logger LOG = Logger.getLogger(Protocol.class.getName());

    private Socket sock;
    private BufferedReader in;
    private PrintWriter out;

    public Protocol(String adresse, int port) {
        try {
            // Open the socket
            String response;
            sock = new Socket(adresse, port);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter(sock.getOutputStream());

            // Receiving the Welcome message
            response = in.readLine();
            if (!response.substring(0, 3).equals("220")) {
                throw new RuntimeException("Bad WELCOME response! Got: " + response);
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Couldn't open the socket! Error: " + e.getMessage());
        }

    }

    // Send a messsage through the socket that has been set in the constructor
    public void sendMessage(String helo,
                            String realFrom,
                            ArrayList<String> to,
                            String fakeFrom,
                            String data){

        if (helo == null || realFrom == null || to == null || fakeFrom == null || data == null) {
            throw new RuntimeException("Bad argument in the call of sendMessage");
        }
        try {
            String response;

            // Sending a message through the socket by doing a "ping-pong" protocol (send-received)

            // HELO
            out.println("HELO " + helo);
            out.flush();
            response = in.readLine();
            if (!response.substring(0, 3).equals("250")) {
                throw new RuntimeException("Bad HELO response! Got: " + response);
            }

            // MAIL FROM:
            out.println("MAIL FROM: " + realFrom);
            out.flush();
            response = in.readLine();
            if (!response.substring(0, 3).equals("250")) {
                throw new RuntimeException("Bad MAIL FROM response! Got: " + response);
            }

            // RCPT TO:
            for (String rcpt : to) {
                out.println("RCPT TO: " + rcpt);
                out.flush();
                response = in.readLine();
                if (!response.substring(0, 3).equals("250")) {
                    throw new RuntimeException("Bad RCPT TO response! Got: " + response);
                }
            }


            // DATA
            out.println("DATA");
            out.flush();
            response = in.readLine();
            if (!response.substring(0, 3).equals("354")) {
                throw new RuntimeException("Bad DATA response! Got: " + response);
            }

            // From
            out.println("From: " + fakeFrom);
            out.flush();

            // To
            out.print("To: ");
            for (int i = 0; i < to.size(); i++) {
                out.print(to.get(i) + (i == to.size() - 1 ? "" : ", "));
            }
            out.println();
            out.flush();

            // Data
            out.println(data);
            out.flush();

            // Close connection
            out.println(".");
            out.flush();

            response = in.readLine();
            if (!response.substring(0, 3).equals("250")) {
                throw new RuntimeException("Bad END DATA response! Got: " + response);
            }

        } catch (java.io.IOException e) {
            throw new RuntimeException("Couldn't send a message! Error: " + e.getMessage());
        }
    }

}
