package com.sm.stock_manager;
/*
 * RecordsDatabaseServer.java
 *
 * The server main class.
 * This server provides a service to access the Records database.
 *
 */

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;

public class RecordsDatabaseServer {

  private int thePort = 0;
  private String theIPAddress = null;
  private ServerSocket serverSocket = null;

  // Support for closing the server
  // private boolean keypressedFlag = false;

  public RecordsDatabaseServer() {
    thePort = Credentials.PORT;
    theIPAddress = Credentials.HOST;

    System.out.println("Server: Initializing server socket at " + theIPAddress + " with listening port " + thePort);
    System.out.println(
        "Server: Exit server application by pressing Ctrl+C (Windows or Linux) or Opt-Cmd-Shift-Esc (Mac OSX).");
    try {
      int maxConnQ = 3;
      serverSocket = new ServerSocket(thePort, maxConnQ, InetAddress.getByName(theIPAddress));
      System.out.println("Server: Server at " + theIPAddress + " is listening on port : " + thePort);
    } catch (Exception e) {
      System.out.println(e);
      System.exit(1);
    }
  }

  public void executeServiceLoop() {
    System.out.println("Server: Start service loop.");
    try {
      while (true) {
        Socket aSocket = this.serverSocket.accept();
        RecordsDatabaseService tmpServiceThread = new RecordsDatabaseService(aSocket);
      }
    } catch (Exception e) {
      System.out.println(e);
    }
    System.out.println("Server: Finished service loop.");
  }

  /*
   * @Override
   * protected void finalize() {
   * System.exit(0);
   * }
   */

  public static void main(String[] args) {
    RecordsDatabaseServer server = new RecordsDatabaseServer();
    server.executeServiceLoop();
    System.out.println("Server: Finished.");
    System.exit(0);
  }

}
