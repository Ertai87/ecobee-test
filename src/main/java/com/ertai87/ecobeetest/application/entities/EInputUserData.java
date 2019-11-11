package com.ertai87.ecobeetest.application.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EInputUserData {
    private String name;
    private double rvalue;
    private String country;
    private String province;
    private String city;
}
