package com.example.currencyRateVisualizer;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "currency",
        "code",
        "mid"
})
@Generated("jsonschema2pojo")
public class Rate implements Serializable {

    private final static long serialVersionUID = 3890479844162897626L;
    @JsonProperty("currency")
    public String currency;
    @JsonProperty("code")
    public String code;
    @JsonProperty("mid")
    public Double mid;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
    
}