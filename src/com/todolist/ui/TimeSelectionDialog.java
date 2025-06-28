package com.todolist.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;

public class TimeSelectionDialog {
    public static class TimeResult {
        public final String day;
        public final LocalTime time;
        public final boolean cancelled;
        public TimeResult(String day, LocalTime time, boolean cancelled) { this.day = day; this.time = time; this.cancelled = cancelled; }
    }

    public static TimeResult showTimeSelectionDialog(Component parent, String[] days) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Görev Planlama", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(parent);

        // Ana panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Gün seçimi
        gbc.gridx=0; gbc.gridy=0; mainPanel.add(new JLabel("Gün:"), gbc);
        JComboBox<String> dayCombo = new JComboBox<>(days);
        gbc.gridx=1; gbc.fill = GridBagConstraints.HORIZONTAL; mainPanel.add(dayCombo, gbc);

        // Saat checkbox
        JCheckBox timeEnabledBox = new JCheckBox("Saat belirle", false);
        gbc.gridx=0; gbc.gridy=1; gbc.gridwidth=2; mainPanel.add(timeEnabledBox, gbc);

        // Saat seçimi
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(9, 0, 23, 1));
        JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 15));
        hourSpinner.setEditor(new JSpinner.NumberEditor(hourSpinner, "00"));
        minuteSpinner.setEditor(new JSpinner.NumberEditor(minuteSpinner, "00"));
        timePanel.add(new JLabel("Saat:")); timePanel.add(hourSpinner); timePanel.add(new JLabel(":")); timePanel.add(minuteSpinner);
        gbc.gridy=2; mainPanel.add(timePanel, gbc);

        // Başlangıç durumu ve listener
        setTimeComponentsEnabled(timePanel, false);
        timeEnabledBox.addActionListener(e -> setTimeComponentsEnabled(timePanel, timeEnabledBox.isSelected()));

        dialog.add(mainPanel, BorderLayout.CENTER);

        // Butonlar
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("Planla");
        JButton cancelButton = new JButton("İptal");
        buttonPanel.add(okButton); buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Sonuç için dizi
        final TimeResult[] result = {new TimeResult(null, null, true)};

        // Buton dinleyicileri
        okButton.addActionListener(e -> {
            LocalTime selectedTime = timeEnabledBox.isSelected() ? LocalTime.of((int)hourSpinner.getValue(), (int)minuteSpinner.getValue()) : null;
            result[0] = new TimeResult((String)dayCombo.getSelectedItem(), selectedTime, false);
            dialog.dispose();
        });
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.getRootPane().setDefaultButton(okButton);
        dialog.setVisible(true);
        return result[0];
    }

    private static void setTimeComponentsEnabled(Container container, boolean enabled) {
        for (Component c : container.getComponents()) c.setEnabled(enabled);
    }
}