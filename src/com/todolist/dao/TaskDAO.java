package com.todolist.dao;

import com.todolist.model.Task;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private final Connection conn;

    public TaskDAO() {
        this.conn = DataBaseConnection.getConnection();
        // Uygulama her başladığında haftalık sayacı kontrol et
        checkWeekCounterTable();
    }

    public void addTask(Task task) {
        String sql = "INSERT INTO tasks (description, status) VALUES (?, 'POOL')";
        executeUpdate(sql, task.getDescription());
    }

    public List<Task> getTasks(String status, String dayOfWeek) {
        List<Task> tasks = new ArrayList<>();
        String sql;
        switch (status) {
            case "POOL": sql = "SELECT * FROM tasks WHERE status = 'POOL' ORDER BY id DESC"; break;
            case "PLANNED": sql = "SELECT * FROM tasks WHERE status = 'PLANNED' AND day_of_week = ? ORDER BY time_of_day ASC, id DESC"; break;
            case "COMPLETED": sql = "SELECT * FROM tasks WHERE status = 'COMPLETED' ORDER BY id DESC"; break;
            default: return tasks;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if ("PLANNED".equals(status)) {
                pstmt.setString(1, dayOfWeek);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    /**
     * Yeni: Arşivlenmiş görevleri hafta numarasına göre gruplayarak getirir.
     */
    public List<Task> getArchivedTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE status = 'ARCHIVED' ORDER BY completion_date DESC, week_number DESC, id DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public boolean updateTaskDayAndTime(int taskId, String day, LocalTime time) throws SQLException {
        if (hasTimeConflict(day, time)) {
            return false;
        }
        String sql = "UPDATE tasks SET day_of_week = ?, time_of_day = ?, status = 'PLANNED' WHERE id = ?";
        executeUpdate(sql, day, time != null ? Time.valueOf(time) : null, taskId);
        return true;
    }

    public void finishDay(String day) {
        executeUpdate("UPDATE tasks SET status = 'COMPLETED' WHERE day_of_week = ? AND is_done = true AND status = 'PLANNED'", day);
        executeUpdate("UPDATE tasks SET status = 'POOL', day_of_week = NULL, time_of_day = NULL WHERE day_of_week = ? AND is_done = false AND status = 'PLANNED'", day);
    }

    /**
     * Yeni: Haftayı bitirme fonksiyonu güncellendi.
     * Tamamlanmış görevlere hafta numarasını ekler.
     */
    public void finishWeek() {
        int currentWeek = getWeekCounter();
        executeUpdate("UPDATE tasks SET status = 'ARCHIVED', completion_date = CURDATE(), week_number = ? WHERE status IN ('PLANNED', 'COMPLETED') AND is_done = true", currentWeek);
        executeUpdate("UPDATE tasks SET status = 'POOL', day_of_week = NULL, time_of_day = NULL WHERE status = 'PLANNED' AND is_done = false");
    }

    public void toggleTaskStatus(int taskId) {
        executeUpdate("UPDATE tasks SET is_done = NOT is_done WHERE id = ?", taskId);
    }

    public void deleteTask(int taskId) {
        executeUpdate("DELETE FROM tasks WHERE id = ?", taskId);
    }

    private boolean hasTimeConflict(String day, LocalTime time) throws SQLException {
        if (day == null) {
            return false;
        }
        String sql;
        if (time != null) {
            sql = "SELECT COUNT(*) FROM tasks WHERE day_of_week = ? AND time_of_day = ? AND status = 'PLANNED'";
        } else {
            sql = "SELECT COUNT(*) FROM tasks WHERE day_of_week = ? AND time_of_day IS NULL AND status = 'PLANNED'";
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, day);
            if (time != null) {
                pstmt.setTime(2, Time.valueOf(time));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private void executeUpdate(String sql, Object... params) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("completion_date");
        LocalDate completionDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;
        Time sqlTime = rs.getTime("time_of_day");
        LocalTime localTime = (sqlTime != null) ? sqlTime.toLocalTime() : null;

        return new Task(
                rs.getInt("id"),
                rs.getString("description"),
                rs.getString("day_of_week"),
                localTime,
                rs.getBoolean("is_done"),
                rs.getString("status"),
                completionDate,
                rs.getInt("week_number") // Yeni eklenen sütun
        );
    }

    /**
     * Yeni: Haftalık sayacı veritabanından okur.
     */
    public int getWeekCounter() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT counter_value FROM week_counter WHERE id = 1")) {
            if (rs.next()) {
                return rs.getInt("counter_value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // Başlangıç değeri
    }

    /**
     * Yeni: Haftalık sayacı veritabanında bir artırır.
     */
    public void incrementWeekCounter() {
        executeUpdate("UPDATE week_counter SET counter_value = counter_value + 1 WHERE id = 1");
    }

    /**
     * Yeni: week_counter tablosunun varlığını kontrol eder ve yoksa oluşturur.
     */
    private void checkWeekCounterTable() {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "week_counter", null);
            if (!tables.next()) {
                // Tablo yoksa oluştur
                executeSQL("CREATE TABLE week_counter (id INT PRIMARY KEY, counter_value INT NOT NULL)");
                // Başlangıç değerini ekle
                executeSQL("INSERT INTO week_counter (id, counter_value) VALUES (1, 1)");
                System.out.println("week_counter tablosu oluşturuldu ve başlangıç değeri eklendi.");
            }
        } catch (SQLException e) {
            System.err.println("week_counter tablosu kontrol hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // DataBaseConnection'daki executeSQL metodu buraya taşındı, çünkü TaskDAO'dan çağrılıyor.
    private void executeSQL(String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Veritabanı güncellendi! -> " + sql);
        }
    }
}