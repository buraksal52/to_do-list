package com.todolist.ui;

import com.todolist.dao.TaskDAO;
import com.todolist.model.Task;
import com.todolist.ui.TimeSelectionDialog.TimeResult;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MainAppUI extends JFrame {
    // Mevcut alanlar aynı kalıyor...
    private final TaskDAO taskDAO;
    private JTextField newTaskField;
    private DefaultListModel<Task> uncompletedTasksModel;
    private JList<Task> uncompletedTasksList;
    private DefaultListModel<Task> completedThisWeekModel;
    private JList<Task> completedThisWeekList;
    private final Map<String, JList<Task>> dailyLists;
    private final Map<String, DefaultListModel<Task>> dailyListModels;
    private final String[] DAYS = {"Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar"};

    // CardLayout alanları aynı kalıyor...
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private ArchivePanel archivePanel;

    private static final String WEEKLY_VIEW = "WEEKLY_VIEW";
    private static final String ARCHIVE_VIEW = "ARCHIVE_VIEW";

    // --- YENİ: Durum yönetimi için alanlar ---
    private JButton archiveToggleButton; // Butona artık her yerden erişeceğiz.
    private String currentView; // Hangi görünümün aktif olduğunu tutacak.
    // --- YENİLİK SONU ---


    public MainAppUI() {
        taskDAO = new TaskDAO();
        dailyLists = new HashMap<>();
        dailyListModels = new HashMap<>();
        initializeUI();
        loadTasks();
    }

    private void initializeUI() {
        setTitle("Gelişmiş Haftalık To-Do Listesi");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(createTopPanel(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        mainContentPanel.add(createCenterPanel(), WEEKLY_VIEW);

        // DEĞİŞİKLİK: ArchivePanel oluşturulurken 'this' referansı kaldırıldı.
        archivePanel = new ArchivePanel(taskDAO);
        mainContentPanel.add(archivePanel, ARCHIVE_VIEW);

        add(mainContentPanel, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        // Başlangıç durumunu ayarla
        currentView = WEEKLY_VIEW;
    }

    // --- DEĞİŞİKLİK: show metotları artık durum ve buton metnini de güncelliyor ---
    public void showWeeklyView() {
        cardLayout.show(mainContentPanel, WEEKLY_VIEW);
        archiveToggleButton.setText("Görev Arşivi");
        currentView = WEEKLY_VIEW;
    }

    public void showArchiveView() {
        archivePanel.loadArchivedTasks();
        cardLayout.show(mainContentPanel, ARCHIVE_VIEW);
        archiveToggleButton.setText("Haftalık Plana Dön");
        currentView = ARCHIVE_VIEW;
    }
    // --- DEĞİŞİKLİK SONU ---

    // --- YENİ: Butonun tıklama olayını yöneten tek bir metot ---
    private void toggleArchiveView() {
        if (currentView.equals(WEEKLY_VIEW)) {
            showArchiveView();
        } else {
            showWeeklyView();
        }
    }
    // --- YENİLİK SONU ---

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton deleteButton = new JButton("Seçili Görevi Sil");
        JButton finishWeekButton = new JButton("Haftayı Bitir");

        // DEĞİŞİKLİK: Buton artık bir sınıf alanı ve tek bir eylemi var.
        archiveToggleButton = new JButton("Görev Arşivi");
        archiveToggleButton.addActionListener(e -> toggleArchiveView());
        // --- DEĞİŞİKLİK SONU ---

        deleteButton.addActionListener(e -> deleteTask());
        finishWeekButton.addActionListener(e -> finishWeek());

        bottomPanel.add(deleteButton);
        bottomPanel.add(finishWeekButton);
        bottomPanel.add(archiveToggleButton); // Sınıf alanını panele ekle
        return bottomPanel;
    }

    // MainAppUI sınıfının geri kalan tüm metotları (createTopPanel, createCenterPanel, vs.)
    // HİÇBİR DEĞİŞİKLİK OLMADAN AYNI KALACAK.
    // Sadece yukarıdaki değişiklikleri kendi kodunuza uygulamanız yeterlidir.
    // (createTopPanel, createCenterPanel, createDailyPlanScrollPane, createDayPanel,
    // createSidePanel, createListPanel, addTask, planTask, deleteTask, finishWeek,
    // toggleTaskStatusFromList, loadTasks, findSelectedTask)

    // Referans olması için bu metotları tekrar ekliyorum:
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        newTaskField = new JTextField();
        JButton addTaskButton = new JButton("Yeni Görev Ekle");
        addTaskButton.addActionListener(e -> addTask());
        newTaskField.addActionListener(e -> addTask());
        topPanel.add(new JLabel(" Yeni Görev:"), BorderLayout.WEST);
        topPanel.add(newTaskField, BorderLayout.CENTER);
        topPanel.add(addTaskButton, BorderLayout.EAST);
        return topPanel;
    }

    private JSplitPane createCenterPanel() {
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, createDailyPlanScrollPane(), createSidePanel());
        mainSplitPane.setResizeWeight(0.65);
        return mainSplitPane;
    }

    private JScrollPane createDailyPlanScrollPane() {
        JPanel dailyPanelContainer = new JPanel(new GridLayout(0, 2, 5, 5));
        dailyPanelContainer.setBorder(BorderFactory.createTitledBorder("Haftalık Plan"));

        for (String day : DAYS) {
            dailyPanelContainer.add(createDayPanel(day));
        }

        JScrollPane scrollPane = new JScrollPane(dailyPanelContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);

        scrollPane.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int unitsToScroll = e.getUnitsToScroll() * 2;
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                verticalBar.setValue(verticalBar.getValue() + unitsToScroll);
            }
        });
        return scrollPane;
    }

    private JPanel createDayPanel(String day) {
        JPanel dayPanel = new JPanel(new BorderLayout(5, 5));
        dayPanel.setBorder(BorderFactory.createTitledBorder(day));

        DefaultListModel<Task> model = new DefaultListModel<>();
        JList<Task> list = new JList<>(model);
        list.setCellRenderer(new TaskCellRenderer());
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                toggleTaskStatusFromList(evt);
            }
        });
        dailyListModels.put(day, model);
        dailyLists.put(day, list);
        dayPanel.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton finishDayButton = new JButton("Günü Bitir");
        finishDayButton.addActionListener(e -> {
            taskDAO.finishDay(day);
            loadTasks();
        });
        dayPanel.add(finishDayButton, BorderLayout.SOUTH);
        return dayPanel;
    }

    private JSplitPane createSidePanel() {
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        rightSplitPane.setResizeWeight(0.5);

        uncompletedTasksModel = new DefaultListModel<>();
        uncompletedTasksList = new JList<>(uncompletedTasksModel);
        JButton planButton = new JButton("Seçili Görevi Planla");
        planButton.addActionListener(e -> planTask());
        rightSplitPane.setTopComponent(createListPanel("Bitmeyen Görevler", uncompletedTasksList, planButton));

        completedThisWeekModel = new DefaultListModel<>();
        completedThisWeekList = new JList<>(completedThisWeekModel);
        rightSplitPane.setBottomComponent(createListPanel("Bu Hafta Bitenler", completedThisWeekList, null));

        return rightSplitPane;
    }

    private JPanel createListPanel(String title, JList<Task> list, JButton button) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        list.setCellRenderer(new TaskCellRenderer());
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        if (button != null) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(button);
            panel.add(buttonPanel, BorderLayout.SOUTH);
        }
        return panel;
    }

    private void addTask() {
        String description = newTaskField.getText().trim();
        if (!description.isEmpty()) {
            taskDAO.addTask(new Task(description));
            newTaskField.setText("");
            loadTasks();
        }
    }

    private void planTask() {
        Task selectedTask = uncompletedTasksList.getSelectedValue();
        if (selectedTask == null) return;

        TimeResult result = TimeSelectionDialog.showTimeSelectionDialog(this, DAYS);
        if (!result.cancelled) {
            try {
                if (!taskDAO.updateTaskDayAndTime(selectedTask.getId(), result.day, result.time)) {
                    JOptionPane.showMessageDialog(this, "Bu tarih ve saatte zaten bir görev var!", "Zaman Çakışması", JOptionPane.WARNING_MESSAGE);
                }
                loadTasks();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void deleteTask() {
        Task taskToDelete = findSelectedTask();
        if (taskToDelete != null) {
            int choice = JOptionPane.showConfirmDialog(this, "Görevi kalıcı olarak silmek istediğinize emin misiniz?", "Silme Onayı", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                taskDAO.deleteTask(taskToDelete.getId());
                loadTasks();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Lütfen silmek için bir görev seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void finishWeek() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Haftayı bitirmek istediğinize emin misiniz?\n" +
                        "- Tamamlanan görevler arşive taşınacak.\n" +
                        "- Tamamlanmayanlar görev havuzuna geri dönecek.",
                "Haftayı Bitir Onayı", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            taskDAO.finishWeek();
            taskDAO.incrementWeekCounter();
            loadTasks();
            JOptionPane.showMessageDialog(this, "Hafta başarıyla sonlandırıldı ve görevler arşivlendi!");
        }
    }

    private void toggleTaskStatusFromList(MouseEvent evt) {
        JList<Task> sourceList = (JList<Task>) evt.getSource();
        int index = sourceList.locationToIndex(evt.getPoint());
        if (index >= 0) {
            taskDAO.toggleTaskStatus(sourceList.getModel().getElementAt(index).getId());
            loadTasks();
        }
    }

    private void loadTasks() {
        SwingUtilities.invokeLater(() -> {
            uncompletedTasksModel.clear();
            completedThisWeekModel.clear();
            for (String day : DAYS) dailyListModels.get(day).clear();

            taskDAO.getTasks("POOL", null).forEach(uncompletedTasksModel::addElement);
            taskDAO.getTasks("COMPLETED", null).forEach(completedThisWeekModel::addElement);
            for (String day : DAYS) {
                taskDAO.getTasks("PLANNED", day).forEach(dailyListModels.get(day)::addElement);
            }
        });
    }

    private Task findSelectedTask() {
        if (uncompletedTasksList.getSelectedValue() != null) return uncompletedTasksList.getSelectedValue();
        if (completedThisWeekList.getSelectedValue() != null) return completedThisWeekList.getSelectedValue();
        for (JList<Task> list : dailyLists.values()) {
            if (list.getSelectedValue() != null) return list.getSelectedValue();
        }
        return null;
    }
}