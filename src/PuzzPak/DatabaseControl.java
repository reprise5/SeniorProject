package PuzzPak;

import java.sql.Connection;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

/**
 * @author reprise
 */
public class DatabaseControl {
    String host = "jdbc:derby://localhost:1527/dbname"; 
    String userID = "user";
    String password = "pass";
    Date tempdate = new Date();
    String date= new SimpleDateFormat("yyyy-MM-dd").format(tempdate);
    
    
    //Main Constructor
    public DatabaseControl(String userID, String password, String host){
        this.userID = userID;
        this.password = password;
        this.host = host;
    } 
    
    // Try a connection.
    public boolean connect(){
        try {
            Connection con = DriverManager.getConnection(host, userID, password);
            return true;
        }
        catch (SQLException err) {
            System.out.println( err.getMessage());
            return false;
        }
    }
    
    //A game calls this to add user's name to the leaderboard database
    public boolean addScore(String user, double score, String game){
        boolean success = false;
        try {
            Connection con = DriverManager.getConnection(host, userID, password );
            Statement stmt = con.createStatement();
            
            switch (game) {
                case "hangman":
                    stmt.executeUpdate("INSERT INTO HANGMANALLTIME VALUES ('" + user + "', " + score + ", '" + date +"', 'hangman')");
                    System.out.println("INSERT INTO HANGMANALLTIME VALUES ('" + user + "', " + score + ", '" + date +"', 'hangman')");
                    success = true;
                    break;
                case "tiktak":
                    stmt.executeUpdate("INSERT INTO TIKTAKALLTIME VALUES ('" + user + "', " + score + ", '" + date +"', 'tiktak')");
                    System.out.println("INSERT INTO TIKTAKALLTIME VALUES ('" + user + "', " + score + ", '" + date +"', 'tiktak')");
                    success = true;
                    break;
                case "mg4x4":
                    stmt.executeUpdate("INSERT INTO MEMORYT4X4ALLTIME VALUES ('" + user + "', " + score + ", '" + date +"', 'mg4x4')");
                    System.out.println("INSERT INTO MEMORYT4X4ALLTIME VALUES ('" + user + "', " + score + ", '" + date +"', 'mg4x4')");
                    success = true;
                    break;
                case "mg6x6":
                    stmt.executeUpdate("INSERT INTO MEMORYT6X6ALLTIME VALUES ('" + user + "', " + score + ", '" + date +"', 'mg6x6')");
                    System.out.println("INSERT INTO MEMORYT6X6ALLTIME VALUES ('" + user + "', " + score + ", '" + date +"', 'mg6x6')");
                    success = true;
                    break;
                default:
                    success = false;
                    break;
            }
        }
        catch (SQLException err) {
            System.out.println( err.getMessage());
        }
        return success;
    }
    
    //Adding a record to a table in the database (called from adminform)
    public String addRecord(String table, String user, double score, String game){
        String message = "";
        try {
            Connection con = DriverManager.getConnection(host, userID, password );
            Statement stmt = con.createStatement();
            
            stmt.executeUpdate("INSERT INTO " + table + " VALUES ('" + user + "', " + score + ", '" + date +"', '" + game + "')");
            System.out.println("INSERT INTO " + table + " VALUES ('" + user + "', " + score + ", '" + date +"', '" + game + "')");
            message = "SQL query: \"INSERT INTO " + table + " VALUES ('" + user + "', " + score + ", '" + date +"', '" + game + "')\" was successful";  
        }
        catch (SQLException err) {
            System.out.println( err.getMessage());
            message = err.getMessage();
        }
        return message;
    }
    
    //Deleting record(s) from a table in the database    
    public String deleteRecord(String table, String attributeType, String attribute, int attrCode){
        String message = "";
        try {
            Connection con = DriverManager.getConnection(host, userID, password );
            Statement stmt = con.createStatement();
            
            
            //AttributeCode == 0 means attributeType is followed by a String or item between ' quotes.
            if (attrCode == 0){
                stmt.executeUpdate("DELETE FROM " + table + " WHERE " + attributeType + " = '" + attribute + "'");
                System.out.println("DELETE FROM " + table + " WHERE " + attributeType + " = '" + attribute + "'");
                message = "SQL query: \"DELETE FROM " + table + " WHERE " + attributeType + " = '" + attribute + "'\" was successful.";
            }
            
            //AtttributeCode == 1 means attributeType is followed by a numeric attribute.
            else if (attrCode == 1){
                int intAttribute = Integer.parseInt(attribute);
                
                stmt.executeUpdate("DELETE FROM " + table + " WHERE " + attributeType + " = " + intAttribute);
                System.out.println("DELETE FROM " + table + " WHERE " + attributeType + " = " + intAttribute);
                message = "SQL query: \"DELETE FROM " + table + " WHERE " + attributeType + " = " + intAttribute + "\" was successful.";
            
            }
            //if the user doesn't exist in any table, remove them from players.
            //
            //    
        }
        catch (SQLException err) {
            System.out.println(err.getMessage());
            message = err.getMessage();
        }
        return message;
    }
    
    //Add a new player to the database when they log in.  if they're already here, leave it be.
    public boolean addNewAccount(String username){ 
        boolean success;
        
        try {
            Connection con = DriverManager.getConnection(host, userID, password );
            Statement stmt = con.createStatement();
            
            stmt.executeUpdate("INSERT INTO PLAYER (USERNAME, HANGMAN, TIKTAK, MT4x4, MT6x6) VALUES ('" + username + "', 0.0 ,0.0 ,0.0 ,0.0 )");
            System.out.println("INSERT INTO PLAYER (USERNAME, HANGMAN, TIKTAK, MT4x4, MT6x6) VALUES ('" + username + "', 0.0 ,0.0 ,0.0 ,0.0 )");
            
            success = true;
        }
        catch(SQLException err){
            success = false;
            //System.out.println(err.getMessage());
            System.out.println("Account already exists");
        }
        return success;
    }
    
    //will update player's score in the player's table for a gertain game.  Only saves the player's top score for a game.
    public boolean updatePlayerAccount(String player, double score, String game){
        boolean success = false;
        try {
            Connection con = DriverManager.getConnection(host, userID, password );
            Statement stmt = con.createStatement();

            //Update player table.  Set the value in the attribute (the game name) equal to the new score if the score for 
            //the player in that game is greater than the score saved already.
            System.out.println("\nUPDATE PLAYER\n" +
                            "SET " + game + " = " + score + "\n" +
                            "WHERE " + game + " IN (\n" +
                            "    SELECT " + game + "\n" +
                            "    FROM PLAYER\n" +
                            "    WHERE USERNAME = '" + player + "' AND " + game + " < " + score + "\n" +
                            ")");
            stmt.executeUpdate("UPDATE PLAYER " +
                            "SET " + game + " = " + score +
                            "WHERE " + game + " IN (" +
                            "    SELECT " + game +
                            "    FROM PLAYER" +
                            "    WHERE USERNAME = '" + player + "' AND " + game + " < " + score + ")");
        }
        catch (SQLException err) {
            System.out.println( err.getMessage());
            success = false;
        }
        return success;
    }
    
    //wipes the table passed.
    public boolean wipeTable(String table){
        boolean succ = false;
        try {
            Connection con = DriverManager.getConnection(host, userID, password );
            Statement stmt = con.createStatement();

            stmt.executeUpdate("DELETE FROM " + table);
            succ = true;
        }
        catch (SQLException err) {
            System.out.println( err.getMessage());
            succ = false;
        }
        return succ;  
    }
    
    //Executing a freehand SQL query.
    public String executeQuery(String query){
        String message = "";
        try {
            Connection con = DriverManager.getConnection(host, userID, password );
            Statement stmt = con.createStatement();
            
            stmt.executeUpdate(query);
            System.out.println("SQL Query entered: " + query);
            message = "SQL query: \"" + query + "\" was successful";
        }
        catch (SQLException err) {
            System.out.println( err.getMessage());
            message = err.getMessage();
        }
        
        return message;
    }
    
    //Set new database Hostname
    public void setDBhost(String host){
        if (host != null)
            this.host = host;
    }
    
    //Set new database Username
    public void setDBusername(String userID){
        if (userID != null)
            this.userID = userID;
    }
    
    //Set new database Password
    public void setDBpassword(String password){
        if (password != null)
            this.password = password;
    }
    
    //Get current database Hostname
    public String getDBhost(){
        return host;
    }
    
    //Get current database Username
    public String getDBusername(){
        return userID;
    }
    
    //Get current database Password
    public String getDBpassword(){
        return password;
    }
}