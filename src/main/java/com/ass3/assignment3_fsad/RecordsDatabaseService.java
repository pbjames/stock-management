package com.ass3.assignment3_fsad;/*
 * RecordsDatabaseService.java
 *
 * The service threads for the records database server.
 * This class implements the database access service, i.e. opens a JDBC connection
 * to the database, makes and retrieves the query, and sends back the result.
 *
 * author: 2559653
 *
 */

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
//import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.net.Socket;

import java.util.StringTokenizer;

import java.sql.*;
import javax.sql.rowset.*;
    //Direct import of the classes CachedRowSet and CachedRowSetImpl will fail becuase
    //these clasess are not exported by the module. Instead, one needs to impor
    //javax.sql.rowset.* as above.



public class RecordsDatabaseService extends Thread{

    private Socket serviceSocket = null;
    private String[] requestStr  = new String[2]; //One slot for artist's name and one for recordshop's name.
    private ResultSet outcome   = null;

	//JDBC connection
    private String USERNAME = Credentials.USERNAME;
    private String PASSWORD = Credentials.PASSWORD;
    private String URL      = Credentials.URL;



    //Class constructor
    public RecordsDatabaseService(Socket aSocket){
        // DONE
        serviceSocket = aSocket;
        this.start();
    }


    //Retrieve the request from the socket
    public String[] retrieveRequest()
    {
        this.requestStr[0] = ""; //For artist
        this.requestStr[1] = ""; //For recordshop
		
        String tmp = "";
        try {
			//TO BE COMPLETED
            // Accept server service requests
            // Remove # , split by ; and return two elements
            InputStream stream = this.serviceSocket.getInputStream();
            InputStreamReader reader = new InputStreamReader(stream);

            char tChar;
            int currentBuffer = 0;
            StringBuffer[] combinedSb = new StringBuffer[2];
            while (true) {
                tChar = (char) reader.read();
                if (tChar == '#') {
                    // Removes it ?
                    break;
                } else if (tChar == ';') {
                    currentBuffer = 1;
                } else {
                    combinedSb[currentBuffer].append(tChar);
                }
            }
            this.requestStr[0] = combinedSb[0].toString();
            this.requestStr[1] = combinedSb[1].toString();

        } catch(IOException e) {
            System.out.println("Service thread " + this.getId() + ": " + e);
        }
        return this.requestStr;
    }


    //Parse the request command and execute the query
    public boolean attendRequest()
    {
        boolean flagRequestAttended = true;
		this.outcome = null;
		
		String sql = "SELECT DISTINCT record.title, record.label, record.genre, record.rrp, COUNT(recordcopy) OVER (PARTITION BY record.title) FROM record INNER JOIN artist ON artist.lastname = ? and record.artistid = artist.artistid INNER JOIN recordshop ON recordshop.city = ? INNER JOIN recordcopy ON recordcopy.recordshopid = recordshop.recordshopid and recordcopy.recordid = record.recordid";

		try {
			// DONE
            DriverManager.registerDriver(new org.postgresql.Driver());
            Connection con = DriverManager.getConnection(this.URL, this.USERNAME, this.PASSWORD);

            // DONE
            PreparedStatement ppstmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ppstmt.setString(0, this.requestStr[0]);
            ppstmt.setString(1, this.requestStr[1]);

			// DONE -  Watch out! You may need to reset the iterator of the row set using rs.beforeFirst()
            ResultSet rs = ppstmt.executeQuery();
            RowSetFactory aFactory = RowSetProvider.newFactory();
            CachedRowSet crs = aFactory.createCachedRowSet();
            crs.populate(rs);

            // DONE
            ppstmt.close();
            rs.close();
            con.close();

            this.outcome = crs;

		} catch (Exception e) {
            System.out.println(e);
        }

        return flagRequestAttended;
    }



    public void returnServiceOutcome(){
        try {
			// DONE
            ObjectOutputStream outcomeStreamWriter = (ObjectOutputStream) this.serviceSocket.getOutputStream();
            outcomeStreamWriter.flush();
            outcomeStreamWriter.writeObject(this.outcome);

            System.out.println("Service thread " + this.getId() + ": Service outcome returned; " + this.outcome);

            // DONE
            outcomeStreamWriter.close();
            this.serviceSocket.close();

        }catch (IOException e){
            System.out.println("Service thread " + this.getId() + ": " + e);
        }
    }


    //The service thread run() method
    public void run()
    {
		try {
			System.out.println("\n============================================\n");
            //Retrieve the service request from the socket
            this.retrieveRequest();
            System.out.println("Service thread " + this.getId() + ": Request retrieved: "
						+ "artist->" + this.requestStr[0] + "; recordshop->" + this.requestStr[1]);

            //Attend the request
            boolean tmp = this.attendRequest();

            //Send back the outcome of the request
            if (!tmp)
                System.out.println("Service thread " + this.getId() + ": Unable to provide service.");
            this.returnServiceOutcome();

        }catch (Exception e){
            System.out.println("Service thread " + this.getId() + ": " + e);
        }
        //Terminate service thread (by exiting run() method)
        System.out.println("Service thread " + this.getId() + ": Finished service.");
    }

}
