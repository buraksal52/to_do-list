package com.todolist.ui;

import com.todolist.model.Task;
import javax.swing.*;
import java.awt.*;

public class TaskCellRenderer extends JCheckBox implements ListCellRenderer<Task> {

    @Override
    public Component getListCellRendererComponent(JList<? extends Task> list, Task task, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        setEnabled(list.isEnabled());
        setSelected(task.isDone());
        setFont(list.getFont());
        setOpaque(true);

        String text = task.toString().replace("✓ ", "");

        if (task.isDone()) {
            // Tamamlanmış görevlerin üzerini çiz
            setText("<html><strike>" + text + "</strike></html>");
            setForeground(Color.GRAY);
        } else {
            setText(text);
            setForeground(list.getForeground());
        }

        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

        return this;
    }
}