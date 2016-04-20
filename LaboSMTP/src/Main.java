/*--------------------------------------------------------------------------------
* Author: Samuel Darcey
* Date: 06.04.2016
* Goal: This program will initialize a telnet client, read a configuration file and use the telnet protocol to send
*       random fake message to a random list of people split in groups. The number of group can be set when the program
*       start.
* --------------------------------------------------------------------------------
* */
public class Main {
    public static void main(String args[]) {
        // Just starting the client!
        FakeEmailClient client = new FakeEmailClient();
        client.sendFakeMessages();
    }
}
