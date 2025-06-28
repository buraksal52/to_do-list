package com.todolist.ui;

import com.todolist.dao.TaskDAO;
import com.todolist.model.Task;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel artık tamamen pasif. İçinde navigasyon butonu bulunmuyor.
 * MainAppUI referansına ihtiyacı kalmadı.
 */
public class ArchivePanel extends JPanel {
    private final TaskDAO taskDAO;
    private final DefaultTableModel tableModel;

    // DEĞİŞİKLİK: MainAppUI referansı constructor'dan kaldırıldı.
    public ArchivePanel(TaskDAO taskDAO) {
        super(new BorderLayout(10, 10));
        this.taskDAO = taskDAO;

        this.tableModel = new DefaultTableModel(new Object[]{"Açıklama", "Tamamlanma Tarihi", "Hafta"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        initializeUI();
        loadArchivedTasks();
    }

    private void initializeUI() {
        setBorder(BorderFactory.createTitledBorder("Görev Arşivi"));

        JTable archiveTable = new JTable(tableModel);
        archiveTable.setRowHeight(25);
        archiveTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        archiveTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        archiveTable.getColumnModel().getColumn(0).setPreferredWidth(500);
        archiveTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        archiveTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(archiveTable);
        add(scrollPane, BorderLayout.CENTER);

        // DEĞİŞİKLİK: Geri butonu ve alt panel buradan tamamen kaldırıldı.
    }

    public void loadArchivedTasks() {
        tableModel.setRowCount(0);

        List<Task> archivedTasks = taskDAO.getArchivedTasks();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, EEEE");


        int lastWeekNumber = -1;
        for (Task task : archivedTasks) {
            int currentWeekNumber = task.getWeekNumber();

            if (currentWeekNumber != lastWeekNumber) {
                if (lastWeekNumber != -1) {
                    tableModel.addRow(new Object[]{"", "", ""});
                }
                tableModel.addRow(new Object[]{"--- HAFTA " + currentWeekNumber + " ---", "", ""});
                lastWeekNumber = currentWeekNumber;
            }

            String formattedDate = (task.getCompletionDate() != null)
                    ? task.getCompletionDate().format(formatter)
                    : "Tarih Yok";

            tableModel.addRow(new Object[]{task.getDescription(), formattedDate, "Hafta " + task.getWeekNumber()});
        }
    }
}