package com.utility.utility_middleware.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Utility_middleware {
    @JsonProperty("WorkdayID")
    private String WorkdayID;
    @JsonProperty("AddressType")
    private String AddressType;
    @JsonProperty("AddressLine1")
    private String AddressLine1;
    @JsonProperty("AddressLine2")
    private String AddressLine2;
    @JsonProperty("City")
    private String City;
    @JsonProperty("State")
    private String State;
    @JsonProperty("Zip")
    private String Zip;
    @JsonProperty("Country")
    private String Country;
}
