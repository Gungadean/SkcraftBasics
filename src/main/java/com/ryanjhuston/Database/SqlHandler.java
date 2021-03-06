package com.ryanjhuston.Database;

import com.ryanjhuston.SkcraftBasics;
import org.bukkit.ChatColor;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqlHandler {

    private SkcraftBasics main;

    private Connection con;

    private boolean useMysql;
    private String username;
    private String password;
    private String address;
    private int port;
    private String database;

    private static int INITIAL_POOL_SIZE = 3;

    public SqlHandler (String username, String password, String address, int port, String database, SkcraftBasics main) {
        this.username = username;
        this.password = password;
        this.address = address;
        this.port = port;
        this.database = database;

        this.main = main;
        this.useMysql = true;

        con = openConnection();
    }

    public SqlHandler (SkcraftBasics main) {
        this.main = main;
        this.useMysql = false;

        con = openConnection();
    }

    public Connection openConnection() {
        try {
            if(con == null || con.isClosed()) {
                if (useMysql) {
                    initializeDriver("com.mysql.jdbc.Driver");
                    con = DriverManager.getConnection("jdbc:mysql://" + address + ":" + port + "/" + database, username, password);
                    main.logger.info("Mysql connection successful.");
                } else {
                    initializeDriver("org.sqlite.JDBC");
                    con = DriverManager.getConnection("jdbc:sqlite:" + new File(main.getDataFolder() + File.separator + "SkcraftBasic.db").getAbsolutePath());
                    main.logger.info("Sqlite connection successful.");
                }
            }
        } catch (SQLException e) {
            main.logger.severe(ChatColor.RED + "Failed to initialize connection.");
        }

        initializeDatabases();

        return con;
    }

    public void reloadConnection(String username, String password, String address, int port, String database) {
        closeConnection();

        this.useMysql = true;
        this.username = username;
        this.password = password;
        this.address = address;
        this.port = port;
        this.database = database;

        con = openConnection();

        if(con != null) {
            main.logger.info("Database connection successfully reloaded.");
        }
    }

    public void reloadConnection() {

        closeConnection();

        this.useMysql = false;

        con = openConnection();

        if(con != null) {
            main.logger.info("Database connection successfully reloaded.");
        }
    }

    public void closeConnection() {
        try {
            con.close();
            main.logger.info("Database connection successfully closed.");
        } catch (SQLException e) {
            main.logger.severe(ChatColor.RED + "Failed to close database connection.");
        }
    }

    private void initializeDriver(String driver) {
        try {
            Class.forName(driver);
        } catch (Exception e) {
            main.logger.severe(ChatColor.RED + "Failed to initialize " + driver.split(".")[1] + " driver.");
        }
    }

    public void initializeDatabases() {
        String sql = "CREATE TABLE IF NOT EXISTS 'Skcraft_Players';";

        try {
            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.executeUpdate();
            main.logger.info("Database successfully initialized.");
        } catch (SQLException e) {
            main.logger.severe(ChatColor.RED + "Failed to initialize database.");
        }
    }

    public Connection getConnection() {
        if(con == null) {
            openConnection();
        }
        return con;
    }
}