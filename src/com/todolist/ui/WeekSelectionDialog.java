package com.todolist.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class WeekSelectionDialog extends JDialog {
    private JList<Integer> weekList;
    private List<Integer> selectedWeeks;

    public WeekSelectionDialog(Frame owner, List<Integer> availableWeeks) {
        super(owner, "Dışa Aktarılacak Haftaları Seçin", true);

        // JList için veriyi hazırla
        DefaultListModel<Integer> listModel = new DefaultListModel<>();
        for (Integer week : availableWeeks) {
            listModel.addElement(week);
        }
        weekList = new JList<>(listModel);
        // Çoklu seçim modunu etkinleştir
        weekList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Arayüzü oluştur
        initializeUI();

        // Boyut ve konum
        pack(); // İçeriğe göre boyutlandır
        setSize(300, 400); // Minimum boyut
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));

        // Başlık
        JLabel titleLabel = new JLabel("Ctrl tuşuna basılı tutarak birden fazla hafta seçebilirsiniz.", SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(titleLabel, BorderLayout.NORTH);

        // Hafta listesi
        JScrollPane scrollPane = new JScrollPane(weekList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

        // --- ALTERNATİF BUTON PANELİ ---
        // Butonları düzenli bir ızgarada yerleştirir
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10)); // 2 satır, 2 sütun
        JButton selectAllButton = new JButton("Tümünü Seç");
        JButton clearSelectionButton = new JButton("Seçimi Temizle");
        JButton okButton = new JButton("Tamam");
        JButton cancelButton = new JButton("İptal");

        // Butonları panele ekleyin
        buttonPanel.add(selectAllButton);
        buttonPanel.add(clearSelectionButton);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        // Alt panel oluşturarak paneli pencerenin altına ekleyin
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.add(buttonPanel);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(bottomPanel, BorderLayout.SOUTH);
        // --- ALTERNATİF SONU ---

        // Buton eylemleri (Aynı kalır)
        selectAllButton.addActionListener(e -> {
            int start = 0;
            int end = weekList.getModel().getSize() - 1;
            if (end >= 0) {
                weekList.setSelectionInterval(start, end);
            }
        });

        clearSelectionButton.addActionListener(e -> weekList.clearSelection());

        okButton.addActionListener(e -> {
            selectedWeeks = weekList.getSelectedValuesList();
            dispose(); // Pencereyi kapat
        });

        cancelButton.addActionListener(e -> {
            selectedWeeks = Collections.emptyList(); // Boş liste ata
            dispose();
        });
    }

    /**
     * Kullanıcının seçtiği haftaların listesini döndürür.
     * Eğer kullanıcı "İptal" dediyse veya seçim yapmadıysa boş liste döner.
     */
    public List<Integer> getSelectedWeeks() {
        return selectedWeeks == null ? Collections.emptyList() : selectedWeeks;
    }
}