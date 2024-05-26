package com.sm.stock_manager;

public class Credentials {
  // JDBC connection
  public static final String USERNAME = "james";
  public static final String PASSWORD = "password";
  public static final String URL = "jdbc:postgresql://localhost:5432/Records";

  // Client-server connection
  public static final String HOST = "127.0.0.1";
  // INFO: Port at which the RecordsDatabaseServer is listening
  public static final int PORT = 9994;
}
