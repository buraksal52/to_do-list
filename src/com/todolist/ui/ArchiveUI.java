package com.todolist.ui;

import com.todolist.dao.TaskDAO;
import com.todolist.model.Task;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ArchiveUI extends JDialog {
    private final TaskDAO taskDAO;
    private final DefaultTableModel tableModel;

    public ArchiveUI(Frame owner, TaskDAO taskDAO) {
        super(owner, "Görev Arşivi", true);
        this.taskDAO = taskDAO;

        tableModel = new DefaultTableModel(new Object[]{"Açıklama", "Tamamlanma Tarihi", "Hafta"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hücreler düzenlenemez
            }
        };

        initializeUI();
        loadArchivedTasks();
    }

    private void initializeUI() {
        setSize(900, 600); // Boyut artırıldı
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));

        JTable archiveTable = new JTable(tableModel);
        archiveTable.setRowHeight(25);
        archiveTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        archiveTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        archiveTable.getColumnModel().getColumn(0).setPreferredWidth(500);
        archiveTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        archiveTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Yeni sütun

        JScrollPane scrollPane = new JScrollPane(archiveTable);
        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Kapat");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Yeni: Haftalık arşivleri hafta numarasına göre yükler.
     */
    private void loadArchivedTasks() {
        tableModel.setRowCount(0);

        List<Task> archivedTasks = taskDAO.getArchivedTasks();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, EEEE");

        int lastWeekNumber = -1;
        for (Task task : archivedTasks) {
            int currentWeekNumber = task.getWeekNumber();

            // Yeni bir hafta başladığında araya ayırıcı ekle
            if (currentWeekNumber != lastWeekNumber) {
                // Boş bir satır ve başlık ekle
                tableModel.addRow(new Object[]{"", "", ""}); // Boş satır
                tableModel.addRow(new Object[]{"--- HAFTA " + currentWeekNumber + " ---", "---", "---"});
                tableModel.addRow(new Object[]{"", "", ""}); // Boş satır
                lastWeekNumber = currentWeekNumber;
            }

            String formattedDate = (task.getCompletionDate() != null)
                    ? task.getCompletionDate().format(formatter)
                    : "Tarih Yok";

            tableModel.addRow(new Object[]{task.getDescription(), formattedDate, "Hafta " + task.getWeekNumber()});
        }
    }
}