package com.utility.utility_middleware.model;

import lombok.Data;

@Data
public class WorkdayResponse {
    private String code;
    private String message;
    private String WorkdayID;
    private String AddressType;
    private String AddressLine1;
    private String AddressLine2;
    private String City;
    private String State;
    private String Zip;
    private String CountyName;
    private String ErrorString;
    private String SuggestionList;
}
