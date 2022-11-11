package com.example.currencyRateVisualizer;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "table",
        "no",
        "effectiveDate",
        "rates"
})
@Generated("jsonschema2pojo")
public class CurrencyRate implements Serializable {

    private final static long serialVersionUID = -551079459466539993L;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonProperty("table")
    public String table;
    @JsonProperty("no")
    public String no;
    @JsonProperty("effectiveDate")
    public String effectiveDate;
    @JsonProperty("rates")
    public List<Rate> rates = null;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}