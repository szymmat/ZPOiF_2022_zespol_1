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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.WorldMapView;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

enum Interval {
    WEEK, MONTH, THREE_MONTHS, YEAR
}

public class HelloController implements Initializable {
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @FXML
    private Button summaryStartWeekAgoButton;
    @FXML
    private Button summaryStartMonthAgoButton;
    @FXML
    private Button summaryStartThreeMonthsAgoButton;
    @FXML
    private Button summaryStartYearAgoButton;
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
    @FXML
    private WorldMapView worldMapView;
    @FXML
    private Label countryLabel;
    @FXML
    private Label currencyMapLabel;
    @FXML
    private Label currencyMapValueLabel;
    @FXML
    private ImageView countryFlag;
    @FXML
    private ChoiceBox<Rate> currencyToCompareChoiceBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        CurrencyRate[] currencyRatesA = fetchCurrencyRates("http://api.nbp.pl/api/exchangerates/tables/A/last/5/");
        ObservableList<TableData> tableData = FXCollections.observableArrayList();
        if (currencyRatesA != null) getTableData(currencyRatesA, tableData);
        CurrencyRate[] currencyRatesB = fetchCurrencyRates("http://api.nbp.pl/api/exchangerates/tables/B/last/5/");
        if (currencyRatesB != null) getTableData(currencyRatesB, tableData);
        setChoiceBoxProperties(currencyRatesA, currencyRatesB);
        setTabsEqualWidth();
        setTableViewProperties(tableData);
        setHelperButtonsActions();
        generateButton.setOnAction(actionEvent -> {
            ObservableList<Rate> rates = currencyChoiceBox.getCheckModel().getCheckedItems();
            if (!validateRates(rates)) return;
            LocalDate endDate = endDatePicker.getValue();
            LocalDate startDate = datePicker.getValue();
            if (!validateDates(startDate, endDate)) return;
            currencyChart.getData().clear();
            currencyChart.setTitle("Kursy walut pomiędzy " + startDate.format(dateTimeFormatter) + " a "
                    + endDate.format(dateTimeFormatter));
            for (Rate selectedRate : rates) {
                ChartData chartData = fetchChartData(startDate, endDate, selectedRate, true);
                if (chartData == null) return;
                XYChart.Series<String, Number> series = processChartData(chartData);
                currencyChart.getData().add(series);
            }
            currencyChart.setVisible(true);
            generateButton.setText("Generuj wykres");
        });
        generateAreaChartButton.setOnAction(actionEvent -> {
            Rate selectedRate = currencyChoiceBox1.getValue();
            Rate rateToCompare = currencyToCompareChoiceBox.getValue();
            if (!validateRates(FXCollections.observableArrayList(selectedRate))) return;
            LocalDate endDate = endDatePicker1.getValue();
            LocalDate startDate = datePicker1.getValue();
            if (!validateDates(startDate, endDate)) return;
            currencyAreaChart.getData().clear();
            currencyAreaChart.setTitle(selectedRate.getCurrency() + " do " + rateToCompare.getCurrency() + " pomiędzy " + startDate.format(dateTimeFormatter) + " a "
                    + endDate.format(dateTimeFormatter));
            boolean isARate = checkARate(currencyRatesA, selectedRate);
            ChartData[] chartData = fetchAreaChartData(startDate, endDate, selectedRate, isARate, rateToCompare);
            if (chartData[0] == null) return;
            XYChart.Series<String, Number> series = processAreaChartData(chartData);
            currencyAreaChart.getData().add(series);
            currencyAreaChart.setVisible(true);
            generateAreaChartButton.setText("Generuj wykres");
            setSummaryLabels(series);
        });


        worldMapView.setOnMouseClicked(mouseEvent -> {
            ObservableList<WorldMapView.Country> countries = worldMapView.getSelectedCountries();
            if (countries.isEmpty()) {
                countryLabel.setText("Wybierz kraj z mapy");
                currencyMapLabel.setText("");
                currencyMapValueLabel.setText("");
                countryFlag.setImage(null);
            } else {
                Locale locale = new Locale("", countries.get(0).name());
                countryLabel.setText(countries.get(0).getLocale().getDisplayCountry());
                currencyMapLabel.setText(Currency.getInstance(locale).getDisplayName(Locale.getDefault()));
                String flagURL = String.format("https://countryflagsapi.com/png/%s", countries.get(0).name());
                countryFlag.setImage(new Image(flagURL));
                String currencyCode = Currency.getInstance(locale).getCurrencyCode();
                Optional<Double> rateValue = Optional.empty();
                if (currencyRatesA != null && currencyRatesB != null) {
                    List<Rate> aRates = currencyRatesA[4].getRates().stream().filter(r -> Objects.equals(r.getCode(), currencyCode)).toList();
                    if (!aRates.isEmpty()) rateValue = Optional.ofNullable(aRates.get(0).getMid());
                    List<Rate> bRates = currencyRatesB[4].getRates().stream().filter(r -> Objects.equals(r.getCode(), currencyCode)).toList();
                    if (!bRates.isEmpty()) rateValue = Optional.ofNullable(bRates.get(0).getMid());
                }
                if (rateValue.isEmpty()) {
                    currencyMapValueLabel.setText("Brak danych");
                } else {
                    currencyMapValueLabel.setText(String.format("%.2f", rateValue.get()));
                }
            }
        });
        worldMapView.setOnScroll(scrollEvent -> {
            double delta = scrollEvent.getDeltaY();
            if (delta < 0) {
                worldMapView.setZoomFactor(worldMapView.getZoomFactor() - 0.5);
            } else {
                worldMapView.setZoomFactor(worldMapView.getZoomFactor() + 0.5);
            }
            scrollEvent.consume();
        });
    }

    private void getTableData(CurrencyRate[] currencyRates, ObservableList<TableData> tableData) {
        List<Rate> rates = currencyRates[4].getRates();
        for (int i = 0; i < rates.size(); i++) {
            Rate rate = currencyRates[0].getRates().get(i);
            double earlyRate = round(currencyRates[4].getRates().get(i).getMid());
            String name = rate.getCode() + " " + rate.getCurrency();
            double currentRate = round(rate.getMid());
            double increase = round(((currentRate - earlyRate) / earlyRate) * 100);
            tableData.add(new TableData(name, earlyRate, currentRate, increase));
        }
    }

    private double round(double d) {
        return Math.round(d * 10000.0) / 10000.0;
    }

    private void setDatePickersValue(Interval interval) {
        LocalDate now = LocalDate.now();
        switch (interval) {
            case WEEK -> datePicker1.setValue(now.minusWeeks(1));
            case MONTH -> datePicker1.setValue(now.minusMonths(1));
            case THREE_MONTHS -> datePicker1.setValue(now.minusMonths(3));
            case YEAR -> datePicker1.setValue(now.minusYears(1));
        }
        endDatePicker1.setValue(now);
    }

    private CurrencyRate[] fetchCurrencyRates(String url) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .uri(URI.create(url))
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
            return null;
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
        return currencyRates;
    }

    private void setTabsEqualWidth() {
        tabPane.tabMinWidthProperty().bind(tableView.widthProperty().multiply(0.25));
    }

    private void setTableViewProperties(ObservableList<TableData> tableData) {
        firstColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        secondColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        thirdColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        fourthColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        firstColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        secondColumn.setCellValueFactory(new PropertyValueFactory<>("earlyRate"));
        thirdColumn.setCellValueFactory(new PropertyValueFactory<>("currentRate"));
        fourthColumn.setCellValueFactory(new PropertyValueFactory<>("increase"));
        tableView.setItems(tableData);
    }

    private void setChoiceBoxProperties(CurrencyRate[] currencyRatesA, CurrencyRate[] currencyRatesB) {
        if (currencyRatesA != null && currencyRatesB != null) {
            currencyChoiceBox.getItems().addAll(FXCollections.observableArrayList(currencyRatesA[0].getRates()));
            currencyChoiceBox1.getItems().addAll(FXCollections.observableArrayList(currencyRatesA[0].getRates()));
            currencyChoiceBox1.getItems().addAll(FXCollections.observableArrayList(currencyRatesB[0].getRates()));
            Rate defaultRate = new Rate();
            defaultRate.setCode("PLN");
            defaultRate.setCurrency("złoty polski");
            defaultRate.setMid(1.00);
            currencyToCompareChoiceBox.getItems().add(defaultRate);
            currencyToCompareChoiceBox.setValue(defaultRate);
            currencyToCompareChoiceBox.getItems().addAll(FXCollections.observableArrayList(currencyRatesA[0].getRates()));
        }
    }

    private boolean validateRates(ObservableList<Rate> rates) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Błąd");
        if (rates == null || rates.isEmpty() || rates.stream().allMatch(Objects::isNull)) {
            alert.setHeaderText("Podaj walutę");
            alert.setContentText("Podaj walutę dla wykresu");
            alert.showAndWait();
            return false;
        }
        return true;
    }

    private boolean validateDates(LocalDate startDate, LocalDate endDate) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Błąd");
        LocalDate currentDate = LocalDate.now();
        LocalDate earliestDate = LocalDate.of(2002, 1, 2);
        if (startDate == null || endDate == null || startDate.isBefore(earliestDate) || endDate.isBefore(earliestDate)
                || startDate.isAfter(currentDate) || endDate.isAfter(currentDate)
                || startDate.isAfter(endDate) || DAYS.between(startDate, endDate) > 366) {
            alert.setHeaderText("Podaj poprawną datę");
            alert.setContentText("Daty mogą się różnić co najwyżej o rok (ograniczenie API NBP). " +
                    "Data musi być z przedziału od 2 stycznia 2002 r. do dzisiaj.");
            alert.showAndWait();
            return false;
        }
        return true;
    }

    private ChartData fetchChartData(LocalDate startDate, LocalDate endDate, Rate selectedRate, boolean isARate) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Błąd");
        HttpRequest chartRequest = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .uri(URI.create(String.format("http://api.nbp.pl/api/exchangerates/rates/%s/%s/%s/%s/",
                        isARate ? "A" : "B",
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
            return null;
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
        return chartData;
    }

    private ChartData[] fetchAreaChartData(LocalDate startDate, LocalDate endDate, Rate selectedRate, boolean isARate, Rate rateToCompare) {
        ChartData selectedRateData = fetchChartData(startDate, endDate, selectedRate, isARate);
        ChartData rateToCompareData = rateToCompare.getCode().equals("PLN")
                ? null
                : fetchChartData(startDate, endDate, rateToCompare, true);
        ChartData[] chartData = new ChartData[2];
        chartData[0] = selectedRateData;
        chartData[1] = rateToCompareData;
        return chartData;
    }

    private XYChart.Series<String, Number> processChartData(ChartData chartData) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(String.format("%s (%s)", chartData.getCurrency(), chartData.getCode()));
        for (com.example.currencyRateVisualizer.chartModels.Rate rate : chartData.getRates()) {
            series.getData().add(new XYChart.Data<>(rate.getEffectiveDate(), rate.getMid()));
        }
        series.getData().sort(Comparator.comparing(XYChart.Data::getXValue));
        return series;
    }

    private XYChart.Series<String, Number> processAreaChartData(ChartData[] chartData) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(String.format("%s (%s)", chartData[0].getCurrency(), chartData[0].getCode()));
        for (com.example.currencyRateVisualizer.chartModels.Rate rate : chartData[0].getRates()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(rate.getEffectiveDate(), rate.getMid());
            if (chartData[1] != null) {
                double divider = chartData[1].getRates().get(chartData[0].getRates().indexOf(rate)).getMid();
                data.setYValue(data.getYValue().doubleValue() / divider);
            }
            series.getData().add(data);
        }
        series.getData().sort(Comparator.comparing(XYChart.Data::getXValue));
        return series;
    }

    private boolean checkARate(CurrencyRate[] currencyRatesA, Rate selectedRate) {
        if (currencyRatesA != null) {
            return currencyRatesA[0].getRates().contains(selectedRate);
        }
        return false;
    }

    private void setSummaryLabels(XYChart.Series<String, Number> series) {
        ObservableList<XYChart.Data<String, Number>> data = series.getData();
        XYChart.Data<String, Number> minRate = data.stream()
                .min(Comparator.comparing(d -> d.getYValue().doubleValue())).get();
        XYChart.Data<String, Number> maxRate = data.stream()
                .max(Comparator.comparing(d -> d.getYValue().doubleValue())).get();
        XYChart.Data<String, Number> firstRate = data.get(0);
        XYChart.Data<String, Number> lastRate = data.get(data.size() - 1);
        double increase = ((lastRate.getYValue().doubleValue() - firstRate.getYValue().doubleValue())
                / firstRate.getYValue().doubleValue()) * 100;
        minLabel.setText(String.format("%.2f (%s)", minRate.getYValue().doubleValue(), minRate.getXValue()));
        maxLabel.setText(String.format("%.2f (%s)", maxRate.getYValue().doubleValue(), maxRate.getXValue()));
        startLabel.setText(String.format("%.2f (%s)", firstRate.getYValue().doubleValue(), firstRate.getXValue()));
        endLabel.setText(String.format("%.2f (%s)", lastRate.getYValue().doubleValue(), lastRate.getXValue()));
        changeLabel.setText(String.format("%.2f%%", increase));
        currencyDataGrid.setVisible(true);
    }

    private void setHelperButtonsActions() {
        summaryStartWeekAgoButton.setOnAction(actionEvent -> setDatePickersValue(Interval.WEEK));
        summaryStartMonthAgoButton.setOnAction(actionEvent -> setDatePickersValue(Interval.MONTH));
        summaryStartThreeMonthsAgoButton.setOnAction(actionEvent -> setDatePickersValue(Interval.THREE_MONTHS));
        summaryStartYearAgoButton.setOnAction(actionEvent -> setDatePickersValue(Interval.YEAR));
    }
}
