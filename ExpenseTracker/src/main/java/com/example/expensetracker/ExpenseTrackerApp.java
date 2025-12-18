package com.example.expensetracker;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;

public class ExpenseTrackerApp extends Application {

    // Модель данных (список всех трат)
    private ObservableList<Expense> expenseData = FXCollections.observableArrayList();
    // Обертка для фильтрации списка
    private FilteredList<Expense> filteredData = new FilteredList<>(expenseData, p -> true);

    private Label totalLabel;
    private ComboBox<Month> monthFilter;
    private ComboBox<String> yearFilter; // Простой фильтр года

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Мой Expense Tracker");

        // 1. ПАНЕЛЬ ВВОДА (Верхняя часть)
        TextField nameInput = new TextField();
        nameInput.setPromptText("Название (напр. Продукты)");
        nameInput.setMinWidth(150);

        TextField priceInput = new TextField();
        priceInput.setPromptText("Цена");
        priceInput.setMinWidth(100);

        DatePicker dateInput = new DatePicker(LocalDate.now());
        dateInput.setPromptText("Дата");

        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> addExpense(nameInput, priceInput, dateInput));

        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10));
        inputBox.getChildren().addAll(nameInput, priceInput, dateInput, addButton);

        // 2. ТАБЛИЦА (Центральная часть)
        TableView<Expense> table = new TableView<>();
        table.setItems(filteredData); // Связываем таблицу с отфильтрованным списком

        TableColumn<Expense, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        nameColumn.setMinWidth(150);

        TableColumn<Expense, Double> priceColumn = new TableColumn<>("Цена");
        priceColumn.setCellValueFactory(data -> data.getValue().amountProperty().asObject());
        priceColumn.setMinWidth(100);

        TableColumn<Expense, LocalDate> dateColumn = new TableColumn<>("Дата");
        dateColumn.setCellValueFactory(data -> data.getValue().dateProperty());
        dateColumn.setMinWidth(100);
        // Форматирование даты в таблице
        dateColumn.setCellFactory(column -> new TableCell<Expense, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                }
            }
        });

        table.getColumns().addAll(nameColumn, priceColumn, dateColumn);

        // 3. ПАНЕЛЬ ФИЛЬТРА И ИТОГОВ (Нижняя часть)
        monthFilter = new ComboBox<>();
        monthFilter.getItems().addAll(Month.values());
        monthFilter.setPromptText("Выберите месяц");

        // Добавляем кнопку "Сброс фильтра"
        Button resetFilterBtn = new Button("Показать всё");

        totalLabel = new Label("Итого: 0.00");
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Логика фильтрации
        monthFilter.setOnAction(e -> updateFilter());
        resetFilterBtn.setOnAction(e -> {
            monthFilter.getSelectionModel().clearSelection();
            filteredData.setPredicate(p -> true);
            updateTotal();
        });

        HBox footerBox = new HBox(15);
        footerBox.setPadding(new Insets(10));
        footerBox.setAlignment(Pos.CENTER_LEFT);
        footerBox.getChildren().addAll(new Label("Фильтр по месяцу:"), monthFilter, resetFilterBtn, new Region(), totalLabel);
        // Region и HBox.setHgrow нужны, чтобы прижать "Итого" к правому краю
        HBox.setHgrow(footerBox.getChildren().get(3), Priority.ALWAYS);

        // 4. ОСНОВНОЙ МАКЕТ
        BorderPane layout = new BorderPane();
        layout.setTop(inputBox);
        layout.setCenter(table);
        layout.setBottom(footerBox);

        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ЛОГИКА ДОБАВЛЕНИЯ
    private void addExpense(TextField nameInput, TextField priceInput, DatePicker dateInput) {
        try {
            String name = nameInput.getText();
            double price = Double.parseDouble(priceInput.getText());
            LocalDate date = dateInput.getValue();

            if (name.isEmpty() || date == null) {
                showAlert("Ошибка", "Заполните название и дату!");
                return;
            }

            Expense newExpense = new Expense(name, price, date);
            expenseData.add(newExpense);

            // Очистка полей
            nameInput.clear();
            priceInput.clear();
            dateInput.setValue(LocalDate.now());

            updateTotal(); // Обновляем сумму

        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Цена должна быть числом!");
        }
    }

    // ЛОГИКА ОБНОВЛЕНИЯ ФИЛЬТРА
    private void updateFilter() {
        Month selectedMonth = monthFilter.getValue();

        if (selectedMonth == null) {
            filteredData.setPredicate(p -> true);
        } else {
            filteredData.setPredicate(expense -> {
                // Сравниваем месяц траты с выбранным месяцем
                return expense.getDate().getMonth() == selectedMonth;
            });
        }
        updateTotal();
    }

    // ЛОГИКА ПОДСЧЕТА СУММЫ (считает только то, что видно в таблице)
    private void updateTotal() {
        double total = 0;
        for (Expense expense : filteredData) {
            total += expense.getAmount();
        }
        totalLabel.setText(String.format("Итого: %.2f", total));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- КЛАСС МОДЕЛИ ДАННЫХ (Внутренний класс для простоты) ---
    public static class Expense {
        private final SimpleStringProperty name;
        private final SimpleDoubleProperty amount;
        private final SimpleObjectProperty<LocalDate> date;

        public Expense(String name, double amount, LocalDate date) {
            this.name = new SimpleStringProperty(name);
            this.amount = new SimpleDoubleProperty(amount);
            this.date = new SimpleObjectProperty<>(date);
        }

        public String getName() { return name.get(); }
        public SimpleStringProperty nameProperty() { return name; }

        public double getAmount() { return amount.get(); }
        public SimpleDoubleProperty amountProperty() { return amount; }

        public LocalDate getDate() { return date.get(); }
        public SimpleObjectProperty<LocalDate> dateProperty() { return date; }
    }
}