package com.todolist.dao;

import java.sql.*;

public class DataBaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/todolist_app";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234"; // Lütfen kendi şifrenizle değiştirin

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Veritabanı bağlantısı başarılı!");
                // Uygulama her başladığında veritabanı şemasını kontrol et/güncelle
                checkAndUpdateDatabase();
            } catch (SQLException | ClassNotFoundException e) {
                System.err.println("Veritabanı bağlantı hatası: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return connection;
    }

    // Veritabanı tablosunun gerekli sütunlara sahip olup olmadığını kontrol eder, yoksa ekler.
    private static void checkAndUpdateDatabase() {
        try {
            if (!columnExists("tasks", "time_of_day")) {
                executeSQL("ALTER TABLE tasks ADD COLUMN time_of_day TIME NULL AFTER day_of_week");
            }
            if (!columnExists("tasks", "status")) {
                executeSQL("ALTER TABLE tasks ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'POOL' AFTER is_done");
            }
            if (!columnExists("tasks", "completion_date")) {
                executeSQL("ALTER TABLE tasks ADD COLUMN completion_date DATE NULL AFTER status");
            }
            // --- YENİLİK: week_number sütununu ekle ---
            if (!columnExists("tasks", "week_number")) {
                executeSQL("ALTER TABLE tasks ADD COLUMN week_number INT NULL AFTER completion_date");
            }
            // --- YENİLİK SONU ---
            System.out.println("Veritabanı şeması güncel.");
        } catch (SQLException e) {
            System.err.println("Veritabanı güncelleme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean columnExists(String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
            return columns.next();
        }
    }

    private static void executeSQL(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Veritabanı güncellendi! -> " + sql);
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Veritabanı bağlantısı kapatıldı.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}