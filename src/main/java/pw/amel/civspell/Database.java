package pw.amel.civspell;


import java.nio.channels.NotYetConnectedException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class Database {
    Database(String username, String password, String database, String hostname, int port, Main mainPlugin) {
        this.username = username;
        this.password = password;
        this.database = database;
        this.hostname = hostname;
        this.port = port;
        this.mainPlugin = mainPlugin;
    }

    private String username;
    private String password;
    private String database;
    private String hostname;
    private int port;

    private Main mainPlugin;
    private Connection connection;

    public boolean connect() {
        String jdbc = "jdbc:mysql://" + hostname + ":" + port + "/" + database + "?user="
                + username + "&password=" + password;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (ClassNotFoundException|IllegalAccessException|InstantiationException ex) {
            mainPlugin.getLogger().log(Level.SEVERE, "Failed to initialize JDBC driver.", ex);
            return false;
        }
        try {
            connection = DriverManager.getConnection(jdbc);
            return true;
        } catch (SQLException e) {
            mainPlugin.getLogger().log(Level.SEVERE,
                    "Could not connnect to the database!", e);
            return false;
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            mainPlugin.getLogger().log(Level.SEVERE,
                    "An error occured while closing the connection.", e);
        }
    }

    public boolean isConnected() {
        if (connection == null) {
            return false;
        }
        try {
            return connection.isValid(5);
        } catch (SQLException e) {
            mainPlugin.getLogger().log(Level.SEVERE, "isConnected error!", e);
        }
        return false;
    }

    public PreparedStatement prepareStatement(String sqlStatement) {
        try {
            return connection.prepareStatement(sqlStatement);
        } catch (SQLException e) {
            mainPlugin.getLogger().log(Level.SEVERE, "Failed to prepare statement! "
                    + sqlStatement, e);
        }
        return null;
    }

    public void execute(String sql) {
        try {
            execute(sql, true);
        } catch (SQLException e) {
            mainPlugin.getLogger().log(Level.SEVERE, "Could not execute SQL statement!", e);
        }
    }

    public void execute(String sql, boolean x) throws SQLException {
        if (isConnected()) {
            connection.prepareStatement(sql).executeUpdate();
        } else {
            if (connect()) {
                execute(sql);
            } else {
                throw new NotYetConnectedException();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
