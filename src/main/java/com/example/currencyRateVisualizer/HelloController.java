package com.example.currencyRateVisualizer;

import com.example.currencyRateVisualizer.chartModels.ChartData;
import com.example.currencyRateVisualizer.tableModels.CurrencyRate;
import com.example.currencyRateVisualizer.tableModels.Rate;
import com.example.currencyRateVisualizer.tableModels.TableData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.CheckComboBox;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import static java.time.temporal.ChronoUnit.DAYS;

enum Interval {
    WEEK, MONTH, THREE_MONTHS, YEAR
}

public class HelloController implements Initializable {
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @FXML
    private Button comparisonStartWeekAgoButton;
    @FXML
    private Button comparisonStartMonthAgoButton;
    @FXML
    private Button comparisonStartThreeMonthsAgoButton;
    @FXML
    private Button comparisonStartYearAgoButton;
    @FXML
    private Button comparisonEndWeekAgoButton;
    @FXML
    private Button comparisonEndMonthAgoButton;
    @FXML
    private Button comparisonEndThreeMonthsAgoButton;
    @FXML
    private Button comparisonEndYearAgoButton;
    @FXML
    private Button summaryStartWeekAgoButton;
    @FXML
    private Button summaryStartMonthAgoButton;
    @FXML
    private Button summaryStartThreeMonthsAgoButton;
    @FXML
    private Button summaryStartYearAgoButton;
    @FXML
    private Button summaryEndWeekAgoButton;
    @FXML
    private Button summaryEndMonthAgoButton;
    @FXML
    private Button summaryEndThreeMonthsAgoButton;
    @FXML
    private Button summaryEndYearAgoButton;
    @FXML
    private TableView<TableData> tableView;
    @FXML
    private TableColumn<TableData, String> firstColumn;
    @FXML
    private TableColumn<TableData, Double> secondColumn;
    @FXML
    private TableColumn<TableData, Double> thirdColumn;
    @FXML
    private TableColumn<TableData, Double> fourthColumn;
    @FXML
    private CheckComboBox<Rate> currencyChoiceBox;
    @FXML
    private ChoiceBox<Rate> currencyChoiceBox1;
    @FXML
    private Button generateButton;
    @FXML
    private DatePicker datePicker;
    @FXML
    private DatePicker datePicker1;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private DatePicker endDatePicker1;
    @FXML
    private LineChart<String, Number> currencyChart;
    @FXML
    private AreaChart<String, Number> currencyAreaChart;
    @FXML
    private TabPane tabPane;
    @FXML
    private Button generateAreaChartButton;
    @FXML
    private GridPane currencyDataGrid;
    @FXML
    private Label minLabel;
    @FXML
    private Label maxLabel;
    @FXML
    private Label startLabel;
    @FXML
    private Label endLabel;
    @FXML
    private Label changeLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .uri(URI.create("http://api.nbp.pl/api/exchangerates/tables/A/last/5/"))
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException ex) {
            Alert noConnectionAlert = new Alert(Alert.AlertType.ERROR);
            noConnectionAlert.setHeaderText("Brak internetu");
            noConnectionAlert.setContentText("Sprawdź połączenie internetowe i uruchom ponownie aplikację.");
            noConnectionAlert.showAndWait();
            Platform.exit();
            return;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        ObjectMapper mapper = new ObjectMapper();
        CurrencyRate[] currencyRates;
        try {
            currencyRates = mapper.readValue(response.body(), CurrencyRate[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        firstColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        secondColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        thirdColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        fourthColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        tabPane.tabMinWidthProperty().bind(tableView.widthProperty().multiply(0.333));

        firstColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        secondColumn.setCellValueFactory(new PropertyValueFactory<>("earlyRate"));
        thirdColumn.setCellValueFactory(new PropertyValueFactory<>("currentRate"));
        fourthColumn.setCellValueFactory(new PropertyValueFactory<>("increase"));

        tableView.setItems(getTableData(currencyRates));
        currencyChoiceBox.getItems().addAll(FXCollections.observableArrayList(currencyRates[0].getRates()));
        currencyChoiceBox1.setItems(FXCollections.observableArrayList(currencyRates[0].getRates()));
        generateButton.setOnAction(actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Błąd");
            ObservableList<Rate> rates = currencyChoiceBox.getCheckModel().getCheckedItems();
            if (rates == null || rates.isEmpty()) {
                alert.setHeaderText("Podaj walutę");
                alert.setContentText("Podaj walutę dla wykresu");
                alert.showAndWait();
                return;
            }
            LocalDate endDate = endDatePicker.getValue();
            LocalDate startDate = datePicker.getValue();
            LocalDate currentDate = LocalDate.now();
            LocalDate earliestDate = LocalDate.of(2002, 1, 2);
            if (startDate == null || endDate == null || startDate.isBefore(earliestDate) || endDate.isBefore(earliestDate)
                    || startDate.isAfter(currentDate) || endDate.isAfter(currentDate)
                    || startDate.isAfter(endDate) || DAYS.between(startDate, endDate) > 366) {
                alert.setHeaderText("Podaj poprawną datę");
                alert.setContentText("Daty mogą się różnić co najwyżej o rok (ograniczenie API NBP). " +
                        "Data musi być z przedziału od 2 stycznia 2002 r. do dzisiaj.");
                alert.showAndWait();
                return;
            }
            currencyChart.getData().clear();
            currencyChart.setTitle("Kursy walut pomiędzy " + startDate.format(dateTimeFormatter) + " a "
                    + endDate.format(dateTimeFormatter));
            for (Rate selectedRate : rates) {
                HttpRequest chartRequest = HttpRequest.newBuilder()
                        .header("Accept", "application/json")
                        .uri(URI.create(String.format("http://api.nbp.pl/api/exchangerates/rates/A/%s/%s/%s/",
                                selectedRate.getCode(),
                                startDate.format(dateTimeFormatter),
                                endDate.format(dateTimeFormatter))))
                        .build();
                HttpResponse<String> chartResponse;
                generateButton.setText("Ładowanie...");
                try {
                    chartResponse = httpClient.send(chartRequest, HttpResponse.BodyHandlers.ofString());
                } catch (ConnectException ex) {
                    alert.setHeaderText("Brak internetu");
                    alert.setContentText("Sprawdź połączenie internetowe");
                    alert.showAndWait();
                    generateButton.setText("Generuj wykres");
                    return;
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

                ObjectMapper chartMapper = new ObjectMapper();
                ChartData chartData;
                try {
                    chartData = chartMapper.readValue(chartResponse.body(), ChartData.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(String.format("%s (%s)", chartData.getCurrency(), chartData.getCode()));
                for (com.example.currencyRateVisualizer.chartModels.Rate rate : chartData.getRates()) {
                    series.getData().add(new XYChart.Data<>(rate.getEffectiveDate(), rate.getMid()));
                }
                series.getData().sort(Comparator.comparing(XYChart.Data::getXValue));
                currencyChart.getData().add(series);
            }
            currencyChart.setVisible(true);
            generateButton.setText("Generuj wykres");
        });
        generateAreaChartButton.setOnAction(actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Błąd");
            Rate selectedRate = currencyChoiceBox1.getValue();
            if (selectedRate == null) {
                alert.setHeaderText("Podaj walutę");
                alert.setContentText("Podaj walutę dla wykresu");
                alert.showAndWait();
                return;
            }
            LocalDate endDate = endDatePicker1.getValue();
            LocalDate startDate = datePicker1.getValue();
            LocalDate currentDate = LocalDate.now();
            LocalDate earliestDate = LocalDate.of(2002, 1, 2);
            if (startDate == null || endDate == null || startDate.isBefore(earliestDate) || endDate.isBefore(earliestDate)
                    || startDate.isAfter(currentDate) || endDate.isAfter(currentDate)
                    || startDate.isAfter(endDate) || DAYS.between(startDate, endDate) > 366) {
                alert.setHeaderText("Podaj poprawną datę");
                alert.setContentText("Daty mogą się różnić co najwyżej o rok (ograniczenie API NBP). " +
                        "Data musi być z przedziału od 2 stycznia 2002 r. do dzisiaj.");
                alert.showAndWait();
                return;
            }
            currencyAreaChart.getData().clear();
            currencyAreaChart.setTitle(selectedRate.getCurrency() + " pomiędzy " + startDate.format(dateTimeFormatter) + " a "
                    + endDate.format(dateTimeFormatter));
            HttpRequest chartRequest = HttpRequest.newBuilder()
                    .header("Accept", "application/json")
                    .uri(URI.create(String.format("http://api.nbp.pl/api/exchangerates/rates/A/%s/%s/%s/",
                            selectedRate.getCode(),
                            startDate.format(dateTimeFormatter),
                            endDate.format(dateTimeFormatter))))
                    .build();
            HttpResponse<String> chartResponse;
            generateAreaChartButton.setText("Ładowanie...");
            try {
                chartResponse = httpClient.send(chartRequest, HttpResponse.BodyHandlers.ofString());
            } catch (ConnectException ex) {
                alert.setHeaderText("Brak internetu");
                alert.setContentText("Sprawdź połączenie internetowe");
                alert.showAndWait();
                generateAreaChartButton.setText("Generuj wykres");
                return;
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            ObjectMapper chartMapper = new ObjectMapper();
            ChartData chartData;
            try {
                chartData = chartMapper.readValue(chartResponse.body(), ChartData.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(String.format("%s (%s)", chartData.getCurrency(), chartData.getCode()));
            for (com.example.currencyRateVisualizer.chartModels.Rate rate : chartData.getRates()) {
                series.getData().add(new XYChart.Data<>(rate.getEffectiveDate(), rate.getMid()));
            }
            series.getData().sort(Comparator.comparing(XYChart.Data::getXValue));
            currencyAreaChart.getData().add(series);

            currencyAreaChart.setVisible(true);
            List<com.example.currencyRateVisualizer.chartModels.Rate> rateList = chartData.getRates();
            generateAreaChartButton.setText("Generuj wykres");
            com.example.currencyRateVisualizer.chartModels.Rate minRate = rateList.stream()
                    .min(Comparator.comparing(com.example.currencyRateVisualizer.chartModels.Rate::getMid)).get();
            com.example.currencyRateVisualizer.chartModels.Rate maxRate = rateList.stream()
                    .max(Comparator.comparing(com.example.currencyRateVisualizer.chartModels.Rate::getMid)).get();
            com.example.currencyRateVisualizer.chartModels.Rate firstRate = rateList.get(0);
            com.example.currencyRateVisualizer.chartModels.Rate lastRate = rateList.get(rateList.size() - 1);
            double increase = ((lastRate.getMid() - firstRate.getMid()) / firstRate.getMid()) * 100;
            minLabel.setText(String.format("%.2f (%s)", minRate.getMid(), minRate.getEffectiveDate()));
            maxLabel.setText(String.format("%.2f (%s)", maxRate.getMid(), maxRate.getEffectiveDate()));
            startLabel.setText(String.format("%.2f (%s)", firstRate.getMid(), firstRate.getEffectiveDate()));
            endLabel.setText(String.format("%.2f (%s)", lastRate.getMid(), lastRate.getEffectiveDate()));
            changeLabel.setText(String.format("%.2f%%", increase));
            currencyDataGrid.setVisible(true);
        });
        comparisonStartWeekAgoButton.setOnAction(actionEvent -> setDatePickerValue(datePicker, Interval.WEEK));
        comparisonEndWeekAgoButton.setOnAction(actionEvent -> setDatePickerValue(endDatePicker, Interval.WEEK));
        summaryStartWeekAgoButton.setOnAction(actionEvent -> setDatePickerValue(datePicker1, Interval.WEEK));
        summaryEndWeekAgoButton.setOnAction(actionEvent -> setDatePickerValue(endDatePicker1, Interval.WEEK));

        comparisonStartMonthAgoButton.setOnAction(actionEvent -> setDatePickerValue(datePicker1, Interval.MONTH));
        comparisonEndMonthAgoButton.setOnAction(actionEvent -> setDatePickerValue(endDatePicker, Interval.MONTH));
        summaryStartMonthAgoButton.setOnAction(actionEvent -> setDatePickerValue(datePicker1, Interval.MONTH));
        summaryEndMonthAgoButton.setOnAction(actionEvent -> setDatePickerValue(endDatePicker1, Interval.MONTH));

        comparisonStartThreeMonthsAgoButton.setOnAction(actionEvent -> setDatePickerValue(datePicker, Interval.THREE_MONTHS));
        comparisonEndThreeMonthsAgoButton.setOnAction(actionEvent -> setDatePickerValue(endDatePicker, Interval.THREE_MONTHS));
        summaryStartThreeMonthsAgoButton.setOnAction(actionEvent -> setDatePickerValue(datePicker1, Interval.THREE_MONTHS));
        summaryEndThreeMonthsAgoButton.setOnAction(actionEvent -> setDatePickerValue(endDatePicker1, Interval.THREE_MONTHS));

        comparisonStartYearAgoButton.setOnAction(actionEvent -> setDatePickerValue(datePicker, Interval.YEAR));
        comparisonEndYearAgoButton.setOnAction(actionEvent -> setDatePickerValue(endDatePicker, Interval.YEAR));
        summaryStartYearAgoButton.setOnAction(actionEvent -> setDatePickerValue(datePicker1, Interval.YEAR));
        summaryEndYearAgoButton.setOnAction(actionEvent -> setDatePickerValue(endDatePicker1, Interval.YEAR));
    }

    private ObservableList<TableData> getTableData(CurrencyRate[] currencyRates) {
        ObservableList<TableData> tableData = FXCollections.observableArrayList();
        List<Rate> rates = currencyRates[0].getRates();
        for (int i = 0; i < rates.size(); i++) {
            Rate rate = rates.get(i);
            double earlyRate = round(currencyRates[4].getRates().get(i).getMid());
            String name = rate.getCode() + " " + rate.getCurrency();
            double currentRate = round(rate.getMid());
            double increase = round(((currentRate - earlyRate) / earlyRate) * 100);
            tableData.add(new TableData(name, earlyRate, currentRate, increase));
        }
        return tableData;
    }

    private double round(double d) {
        return Math.round(d * 10000.0) / 10000.0;
    }

    private void setDatePickerValue(DatePicker datePicker, Interval interval) {
        LocalDate now = LocalDate.now();
        switch (interval) {
            case WEEK -> datePicker.setValue(now.minusWeeks(1));
            case MONTH -> datePicker.setValue(now.minusMonths(1));
            case THREE_MONTHS -> datePicker.setValue(now.minusMonths(3));
            case YEAR -> datePicker.setValue(now.minusYears(1));
        }
    }
}