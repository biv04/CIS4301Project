import java.io.IOException;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class HW11 {
  /**
   * Main method that creates the SQLProcessor assuming the Input is always legit.
   * InputMismatchException will be thrown if wrong input entered.
   *
   * @param args
   */
  public static void main(String[] args) {

    DBConnection dbc = new DBConnection();

    dbc.openConnection();
    boolean bIsRunning = true;
    SQLProcessor processor = new SQLProcessor(dbc);
    while (bIsRunning) {
      try {
        processor.selectionMenu();
      } catch (InputMismatchException e) {
        System.out.println("Input Mismatch Exception Occurred, please double check your input\n");
      }
    }

    dbc.closeConnection();
  }

  private static void reset(Exception e) {
    System.out.println("----------------------------------------");
    System.out.println("-Invalid input, please check your input-");
    System.out.println("----------------------------------------");
    e.printStackTrace();
    main(null);
  }

  private static class DBConnection {

    private static final String JDBC_DRIVER = "org.mysql.jdbc.Driver";
    private static final String LOCAL_HOST = "jdbc:sqlite:C://Users/Karth-Red/Documents/Codes/CIS4301HW10/src/flowers.db";

    private String db;
    private String username;
    private String password;
    // private Statement statement;
    private PreparedStatement preparedStatement;
    private Connection connection;

    private DBConnection() {
      this.db = "flowers";
      this.username = "root";
      this.password = "root";
    }

    public DBConnection(String db, String username, String password) {
      this.db = db;
      this.username = username;
      this.password = password;
    }

    private void openConnection() {
      boolean result = false;

      try {
        connection = DriverManager.getConnection(LOCAL_HOST);
        //System.out.println(db + " connected.");
        System.out.println();
        // statement = connection.createStatement();
        result = true;
      } catch (SQLException sqle) {
        reset(sqle);
      }
    }

    public ResultSet query(String sql, String[] args) {
      ResultSet rs = null;
      int i;
      try {
        preparedStatement = connection.prepareStatement(sql);

        if (args != null) {
          for (i = 0; i < args.length; i++) {
            preparedStatement.setString((i + 1), args[i]);
          }
        }

        rs = preparedStatement.executeQuery();
      } catch (SQLException sqle) {
        reset(sqle);
      }
      return rs;
    }

    private void closeConnection() {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (SQLException sqle) {
        reset(sqle);
      }
    }
  }

  private static class SQLProcessor {

    DBConnection dbc;
    ResultSet rs;
    Scanner in;

    private SQLProcessor(DBConnection dbc) {
      this.dbc = dbc;
      in = new Scanner(System.in);
    }

    private void selectionMenu() {
      String menuMessage =
          "\nPlease Enter a code (1,2,3...) to do the following Command:\n"
              + "0. Show flowers:\n"
              + "1. Select flower: \n"
              + "2. Update\n"
              + "3. Insert\n"
              + "4. Exit Program";

      System.out.println(menuMessage);

      Scanner scanner = new Scanner(System.in);
      int userInput = scanner.nextInt();

      switch (userInput) {
        case 0:
          showFlowers();
        case 1:
          selectFlowers();
          break;
        case 2:
          //updateFlowers()
          break;
        case 3:
          //Insert
          break;
        case 4:
          exitProgram();
          break;
        default:
          System.out.println("Invalid Input");
          break;
      }
    }

    private void showFlowers() {

      String sql = "SELECT COMNAME FROM 'FLOWERS'";
      rs = dbc.query(sql, null);
      print(rs);
    }

    private void selectFlowers() {
      //UI PART
      String flowerName = in.nextLine();
      String sql = "SELECT SIGHTED, LOCATION, PERSON \n"
          + "FROM 'SIGHTINGS'\n"
          + " WHERE NAME = '"
          + flowerName
          + "'\n ORDER BY SIGHTED DESC LIMIT(10);";
      rs = dbc.query(sql, null);
      print(rs);
      System.out.println("----------------------------------------------------------------------");
    }

    public void displayAttributes(String input, String tableName) {
      String[] splited = input.split("\\s+");
      String attributes = "";
      for (int i = 0; i < splited.length; i++) {
        attributes += splited[i];
        if (i < splited.length - 1) {
          attributes += ", ";
        }
      }

      String sql = "select " + attributes + " from " + tableName;

      System.out.println("Do you want to refine your search?\n1. Yes \n2. No");

      int userInput = Integer.parseInt(in.nextLine());
      if (userInput == 1) {
        refineColumns(sql, attributes, tableName);
      } else if (userInput == 2) {
        rs = dbc.query(sql, null);
        print(rs);
      } else {
        reset(new IOException());
      }
    }

    public void refineColumns(String sql, String attributes, String tableName) {
      System.out.println(
          "Please enter a condition of refinement (Example: aid > 50, name = 'Sam')"
              + "\nYou can use AND/OR to separate conditions"
              + "\nExample: aid > 50 AND type = 'checking'");
      String condition = in.nextLine();


      System.out.println(
          "Do you want to Display the current query or Update the data in the current range?"
              + "\n1. Display\n2. Update");
      String input2 = in.nextLine();
      sql = sql + " where " + condition;
      if (Integer.parseInt(input2) == 1) {
        rs = dbc.query(sql, null);
        print(rs);
      } else if (Integer.parseInt(input2) == 2) {
        updateData(sql, attributes, tableName, condition);
      } else {
        reset(new IOException());
      }
    }

    private void updateData(String sql, String attributes, String tableName, String condition) {
      System.out.println("Please enter the Update syntax (Example: aid = 300)"
          + "\nYou can update multiple columns with comma. Example: aid = 300, type = 'checking'");
      String input = in.nextLine();
      String updateSql = "update " + tableName + " Set " + input + " where " + condition;

      rs = dbc.query(updateSql, null);
      rs = dbc.query(sql, null);
      print(rs);
    }

    private void exitProgram() {
      System.exit(0);
    }

    private void print(ResultSet rs) {
      try {
        int columnCount = rs.getMetaData().getColumnCount();
        while (rs.next()) {
          String output = "";
          for (int i = 1; i <= columnCount; i++) {
            if (i == columnCount) {
              output += String.format("%s", rs.getString(i));
            } else {
              output += String.format("%30s %30s", rs.getString(i), "|");
            }
            //System.out.print("\t" + rs.getString(i) + "\t|");
          }
          System.out.print(output);
          System.out.println();
        }
      } catch (SQLException e) {
        reset(e);
      }
    }
  }
}
