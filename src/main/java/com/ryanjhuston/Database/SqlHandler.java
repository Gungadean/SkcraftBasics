package com.ryanjhuston.Database;

import com.ryanjhuston.SkcraftBasics;
import net.md_5.bungee.api.ChatColor;

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

    public SqlHandler (String username, String password, String address, int port, String database, SkcraftBasics main) {
        this.username = username;
        this.password = password;
        this.address = address;
        this.port = port;
        this.database = database;

        this.main = main;
        this.useMysql = true;

        openConnection();
    }

    public SqlHandler (SkcraftBasics main) {
        this.main = main;
        this.useMysql = false;

        openConnection();
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
            if(main.debug) {
                e.printStackTrace();
            }
        }
        return con;
    }

    public void reloadConnection(String username, String password, String address, int port, String database) {

        closeConnection();

        con = null;

        this.useMysql = true;
        this.username = username;
        this.password = password;
        this.address = address;
        this.port = port;
        this.database = database;

        openConnection();
        initializeDatabase();

        if(con != null) {
            main.logger.info("Database connection successfully reloaded.");
        }
    }

    public void reloadConnection() {

        closeConnection();

        con = null;

        this.useMysql = false;

        openConnection();
        initializeDatabase();

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
            if (main.debug) {
                e.printStackTrace();
            }
        }
    }

    private void initializeDriver(String driver) {
        try {
            Class.forName(driver);
        } catch (Exception e) {
            main.logger.severe(ChatColor.RED + "Failed to initialize " + driver.split(".")[1] + " driver.");
            if (main.debug) {
                e.printStackTrace();
            }
        }
    }

    public void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS 'whitelist';";

        try {
            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.executeUpdate();
            main.logger.info("Database successfully initialized.");
        } catch (SQLException e) {
            main.logger.severe(ChatColor.RED + "Failed to initialize database.");
            if(main.debug) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        if(con == null) {
            openConnection();
        }
        return con;
    }
}