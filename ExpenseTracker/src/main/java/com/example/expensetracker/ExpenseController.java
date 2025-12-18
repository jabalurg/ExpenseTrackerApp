package com.example.expensetracker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.time.Month;

public class ExpenseController {

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private DatePicker datePicker;
    @FXML private TableView<ExpenseTrackerApp.Expense> tableView;
    @FXML private TableColumn<ExpenseTrackerApp.Expense, String> nameColumn;
    @FXML private TableColumn<ExpenseTrackerApp.Expense, Double> priceColumn;
    @FXML private TableColumn<ExpenseTrackerApp.Expense, LocalDate> dateColumn;
    @FXML private ComboBox<Month> monthFilter;
    @FXML private Label totalLabel;

    private ObservableList<ExpenseTrackerApp.Expense> masterData = FXCollections.observableArrayList();
    private FilteredList<ExpenseTrackerApp.Expense> filteredData;

    @FXML
    public void initialize() {
        // Настройка колонок
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        // Настройка фильтра
        filteredData = new FilteredList<>(masterData, p -> true);
        tableView.setItems(filteredData);
        monthFilter.getItems().setAll(Month.values());

        datePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleAddExpense() {
        try {
            String name = nameField.getText();
            double price = Double.parseDouble(priceField.getText());
            LocalDate date = datePicker.getValue();

            masterData.add(new ExpenseTrackerApp.Expense(name, price, date));

            nameField.clear();
            priceField.clear();
            updateTotal();
        } catch (NumberFormatException e) {
            System.out.println("Ошибка ввода цены");
        }
    }

    @FXML
    private void handleFilter() {
        Month selected = monthFilter.getValue();
        if (selected != null) {
            filteredData.setPredicate(expense -> expense.getDate().getMonth() == selected);
        }
        updateTotal();
    }

    @FXML
    private void handleResetFilter() {
        monthFilter.getSelectionModel().clearSelection();
        filteredData.setPredicate(p -> true);
        updateTotal();
    }

    private void updateTotal() {
        double total = filteredData.stream().mapToDouble(ExpenseTrackerApp.Expense::getAmount).sum();
        totalLabel.setText(String.format("Итого: %.2f", total));
    }
}