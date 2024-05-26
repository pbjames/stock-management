package com.sm.stock_manager;
/*
 * RecordsDatabaseService.java
 *
 * The service threads for the records database server.
 * This class implements the database access service, i.e. opens a JDBC connection
 * to the database, makes and retrieves the query, and sends back the result.
 *
 */

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.net.Socket;

import java.util.StringTokenizer;

import java.sql.*;
import javax.sql.rowset.*;

public class RecordsDatabaseService extends Thread {

  private Socket serviceSocket = null;
  private String[] requestStr = new String[2];
  private ResultSet outcome = null;

  private String USERNAME = Credentials.USERNAME;
  private String PASSWORD = Credentials.PASSWORD;
  private String URL = Credentials.URL;

  public RecordsDatabaseService(Socket aSocket) {
    serviceSocket = aSocket;
    this.start();
  }

  public String[] retrieveRequest() {
    this.requestStr[0] = ""; // For artist
    this.requestStr[1] = ""; // For recordshop
    try {
      InputStream stream = this.serviceSocket.getInputStream();
      InputStreamReader reader = new InputStreamReader(stream);

      char tChar;
      int currentBuffer = 0;
      StringBuffer[] combinedSb = new StringBuffer[2];
      combinedSb[0] = new StringBuffer();
      combinedSb[1] = new StringBuffer();

      while (true) {
        tChar = (char) reader.read();
        if (tChar == '#') {
          break;
        } else if (tChar == ';') {
          currentBuffer = 1;
        } else {
          combinedSb[currentBuffer].append(tChar);
        }
      }
      this.requestStr[0] = combinedSb[0].toString();
      this.requestStr[1] = combinedSb[1].toString();

    } catch (IOException e) {
      System.out.println("Service thread " + this.getId() + ": " + e);
    }
    return this.requestStr;
  }

  public boolean attendRequest() {
    boolean flagRequestAttended = true;
    this.outcome = null;

    String sql = "SELECT DISTINCT record.title, record.label, record.genre, record.rrp, COUNT(recordcopy) OVER (PARTITION BY record.title) FROM record INNER JOIN artist ON artist.lastname = ? and record.artistid = artist.artistid INNER JOIN recordshop ON recordshop.city = ? INNER JOIN recordcopy ON recordcopy.recordshopid = recordshop.recordshopid and recordcopy.recordid = record.recordid";

    try {
      DriverManager.registerDriver(new org.postgresql.Driver());
      Connection con = DriverManager.getConnection(this.URL, this.USERNAME, this.PASSWORD);

      PreparedStatement ppstmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
          ResultSet.CONCUR_READ_ONLY);
      ppstmt.setString(1, this.requestStr[0]);
      ppstmt.setString(2, this.requestStr[1]);

      ResultSet rs = ppstmt.executeQuery();
      rs.beforeFirst();
      RowSetFactory aFactory = RowSetProvider.newFactory();
      CachedRowSet crs = aFactory.createCachedRowSet();
      crs.populate(rs);

      String title, label, genre, rrp, copyID;
      while (crs.next()) {
        title = crs.getString(1);
        label = crs.getString(2);
        genre = crs.getString(3);
        rrp = crs.getString(4);
        copyID = crs.getString(5);
        System.out.println(title + " | " + label + " | " + genre + " | " + rrp + " | " + copyID);
      }
      crs.beforeFirst();

      ppstmt.close();
      rs.close();
      con.close();

      this.outcome = crs;

    } catch (Exception e) {
      System.out.println(e);
    }

    return flagRequestAttended;
  }

  public void returnServiceOutcome() {
    try {
      ObjectOutputStream outcomeStreamWriter = new ObjectOutputStream(this.serviceSocket.getOutputStream());
      outcomeStreamWriter.flush();
      outcomeStreamWriter.writeObject(this.outcome);

      System.out.println("Service thread " + this.getId() + ": Service outcome returned; " + this.outcome);

      outcomeStreamWriter.close();
      this.serviceSocket.close();

    } catch (IOException e) {
      System.out.println("Service thread " + this.getId() + ": " + e);
    }
  }

  public void run() {
    try {
      System.out.println("\n============================================\n");
      this.retrieveRequest();
      System.out.println("Service thread " + this.getId() + ": Request retrieved: "
          + "artist->" + this.requestStr[0] + "; recordshop->" + this.requestStr[1]);

      boolean tmp = this.attendRequest();

      if (!tmp)
        System.out.println("Service thread " + this.getId() + ": Unable to provide service.");
      this.returnServiceOutcome();

    } catch (Exception e) {
      System.out.println("Service thread " + this.getId() + ": " + e);
    }
    System.out.println("Service thread " + this.getId() + ": Finished service.");
  }

}
