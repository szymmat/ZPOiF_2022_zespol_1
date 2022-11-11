package com.example.currencyRateVisualizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder().header("Accept", "application/json").uri(URI.create("http://api.nbp.pl/api/exchangerates/tables/A/last/5/")).build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
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

        firstColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        secondColumn.setCellValueFactory(new PropertyValueFactory<>("earlyRate"));
        thirdColumn.setCellValueFactory(new PropertyValueFactory<>("currentRate"));
        fourthColumn.setCellValueFactory(new PropertyValueFactory<>("increase"));

        tableView.setItems(getTableData(currencyRates));
    }

    private ObservableList<TableData> getTableData(CurrencyRate[] currencyRates) {
        ObservableList<TableData> tableData = FXCollections.observableArrayList();
        List<Rate> rates = currencyRates[0].rates;
        for (int i = 0; i < rates.size(); i++) {
            Rate rate = rates.get(i);
            double earlyRate = round(currencyRates[4].rates.get(i).mid);
            String name = rate.code + " " + rate.currency;
            double currentRate = round(rate.mid);
            double increase = round(((currentRate - earlyRate) / earlyRate) * 100);
            tableData.add(new TableData(name, earlyRate, currentRate, increase));
        }
        return tableData;
    }

    private double round(double d) {
        return Math.round(d * 10000.0) / 10000.0;
    }
}