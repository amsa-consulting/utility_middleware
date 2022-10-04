package com.utility.utility_middleware.model;

import lombok.Data;

@Data
public class UnzipParams {
    private String pathSource;
    private String pathDestination;
    private String hostSource;
    private String userSource;
    private String passwordSource;
    private String hostDestination;
    private String userDestination;
    private String passwordDestination;
}
