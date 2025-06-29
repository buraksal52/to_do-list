package com.todolist.ui;

import com.todolist.dao.TaskDAO;
import com.todolist.model.Task;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ArchivePanel extends JPanel {
    private final TaskDAO taskDAO;
    private final DefaultTableModel tableModel;

    // Diğer kodlar aynı kalıyor...
    public ArchivePanel(TaskDAO taskDAO) {
        super(new BorderLayout(10, 10));
        this.taskDAO = taskDAO;
        this.tableModel = new DefaultTableModel(new Object[]{"Açıklama", "Tamamlanma Tarihi", "Hafta"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
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
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportButton = new JButton("Dosyaya Çıkart");
        exportButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        exportButton.addActionListener(e -> exportArchiveToFile());
        bottomPanel.add(exportButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    public void loadArchivedTasks() {
        tableModel.setRowCount(0);
        List<Task> archivedTasks = taskDAO.getArchivedTasks();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, EEEE");
        int lastWeekNumber = -1;
        for (Task task : archivedTasks) {
            int currentWeekNumber = task.getWeekNumber();
            if (currentWeekNumber != lastWeekNumber) {
                if (lastWeekNumber != -1) tableModel.addRow(new Object[]{"", "", ""});
                tableModel.addRow(new Object[]{"--- HAFTA " + currentWeekNumber + " ---", "", ""});
                lastWeekNumber = currentWeekNumber;
            }
            String formattedDate = (task.getCompletionDate() != null) ? task.getCompletionDate().format(formatter) : "Tarih Yok";
            tableModel.addRow(new Object[]{task.getDescription(), formattedDate, "Hafta " + task.getWeekNumber()});
        }
    }


    /**
     * GÜNCELLENDİ: Önce kullanıcıdan hafta seçmesini ister, sonra seçilen haftaları dosyaya aktarır.
     */
    private void exportArchiveToFile() {
        List<Task> allArchivedTasks = taskDAO.getArchivedTasks();

        if (allArchivedTasks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Arşivde dışa aktarılacak görev bulunmuyor.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- YENİ KOD: Hafta Seçim Ekranını Göster ---
        // Arşivdeki benzersiz hafta numaralarını al ve sırala
        List<Integer> availableWeeks = allArchivedTasks.stream()
                .map(Task::getWeekNumber)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Seçim dialogunu oluştur ve göster
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        WeekSelectionDialog dialog = new WeekSelectionDialog(owner, availableWeeks);
        dialog.setVisible(true);

        List<Integer> selectedWeeks = dialog.getSelectedWeeks();

        // Eğer kullanıcı bir hafta seçmediyse veya iptal ettiyse işlemden çık
        if (selectedWeeks.isEmpty()) {
            return;
        }

        // Sadece seçilen haftalara ait görevleri filtrele
        List<Task> tasksToExport = allArchivedTasks.stream()
                .filter(task -> selectedWeeks.contains(task.getWeekNumber()))
                .collect(Collectors.toList());
        // --- YENİ KOD SONU ---


        // Dosya içeriğini oluştur (artık filtrelenmiş görevleri kullanıyor)
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("SEÇİLEN HAFTALARIN GÖREV ARŞİVİ\n");
        contentBuilder.append("===================================\n\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, EEEE");
        int lastWeekNumber = -1;
        for (Task task : tasksToExport) {
            int currentWeekNumber = task.getWeekNumber();
            if (currentWeekNumber != lastWeekNumber) {
                if (lastWeekNumber != -1) contentBuilder.append("\n");
                contentBuilder.append("------------------------------------\n");
                contentBuilder.append("---          HAFTA ").append(currentWeekNumber).append("          ---\n");
                contentBuilder.append("------------------------------------\n\n");
                lastWeekNumber = currentWeekNumber;
            }
            String formattedDate = (task.getCompletionDate() != null) ? task.getCompletionDate().format(formatter) : "Tarih Yok";
            contentBuilder.append("Görev: ").append(task.getDescription()).append("\n");
            contentBuilder.append("Tamamlanma Tarihi: ").append(formattedDate).append("\n\n");
        }

        // JFileChooser ile konum ve isim al (Bu kısım aynı kaldı)
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Arşivi Farklı Kaydet");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Metin Dosyaları (*.txt)", "txt");
        fileChooser.setFileFilter(filter);
        String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
        fileChooser.setSelectedFile(new File("gorev_arsivi_secilen_haftalar_" + timeStamp + ".txt"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".txt")) {
                fileToSave = new File(filePath + ".txt");
            }
            if (fileToSave.exists()) {
                int result = JOptionPane.showConfirmDialog(this, "Bu dosya zaten mevcut. Üzerine yazılsın mı?", "Onay", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.NO_OPTION) return;
            }
            try {
                Files.write(fileToSave.toPath(), contentBuilder.toString().getBytes(StandardCharsets.UTF_8));
                JOptionPane.showMessageDialog(this, "Arşiv başarıyla kaydedildi:\n" + fileToSave.getAbsolutePath(), "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Dosya yazılırken bir hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}