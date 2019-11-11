package com.ertai87.ecobeetest.application;

import lombok.Data;
import org.kohsuke.args4j.Option;

@Data
public class Arguments {
    @Option(name = "-file", usage = "Location of input file in classpath", required = true)
    private String file;
}
