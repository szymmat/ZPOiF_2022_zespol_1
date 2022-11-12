package com.example.currencyRateVisualizer.chartModels;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "no",
        "effectiveDate",
        "mid"
})
@Generated("jsonschema2pojo")
public class Rate implements Serializable {

    private final static long serialVersionUID = -5817760550743560894L;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonProperty("no")
    private String no;
    @JsonProperty("effectiveDate")
    private String effectiveDate;
    @JsonProperty("mid")
    private Double mid;

    @JsonProperty("no")
    public String getNo() {
        return no;
    }

    @JsonProperty("no")
    public void setNo(String no) {
        this.no = no;
    }

    @JsonProperty("effectiveDate")
    public String getEffectiveDate() {
        return effectiveDate;
    }

    @JsonProperty("effectiveDate")
    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    @JsonProperty("mid")
    public Double getMid() {
        return mid;
    }

    @JsonProperty("mid")
    public void setMid(Double mid) {
        this.mid = mid;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}