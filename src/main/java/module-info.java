module com.sm.stock_manager {
  requires javafx.controls;
  requires javafx.fxml;
  requires java.sql;
  requires java.sql.rowset;
  requires org.postgresql.jdbc;

  opens com.sm.stock_manager to javafx.fxml;

  exports com.sm.stock_manager;
}
