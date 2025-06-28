package com.todolist;

import com.todolist.dao.DataBaseConnection;
import com.todolist.ui.MainAppUI;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        // Uygulama kapanırken veritabanı bağlantısını güvenli bir şekilde kapatır.
        Runtime.getRuntime().addShutdownHook(new Thread(DataBaseConnection::closeConnection));

        // Swing arayüzünü güvenli bir şekilde başlatır.
        SwingUtilities.invokeLater(() -> {
            try {
                MainAppUI app = new MainAppUI();
                app.setVisible(true);
            } catch (Exception e) {
                System.err.println("Uygulama başlatma hatası: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}