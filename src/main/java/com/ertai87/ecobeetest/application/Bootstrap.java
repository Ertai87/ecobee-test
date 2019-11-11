package com.ertai87.ecobeetest.application;

import com.ertai87.ecobeetest.application.tasks.DataLoader;
import com.ertai87.ecobeetest.application.tasks.QueryExecutor;
import com.ertai87.ecobeetest.application.tasks.SetupElasticSearch;
import lombok.AllArgsConstructor;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;

import java.util.Scanner;

import static java.lang.Thread.sleep;

@ComponentScan("com.ertai87")
@AllArgsConstructor
public class Bootstrap implements CommandLineRunner {
    private SetupElasticSearch setupElasticSearch;
    private DataLoader dataLoader;
    private QueryExecutor queryExecutor;

    public static void main (String args[]){
        new SpringApplicationBuilder(Bootstrap.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(String... argsList) throws Exception {
        Arguments args = new Arguments();
        CmdLineParser parser = new CmdLineParser(args);
        try {
            parser.parseArgument(argsList);
        } catch (CmdLineException e) {
            parser.printUsage(System.out);
            throw new IllegalArgumentException(e);
        }

        setupElasticSearch.SetupElasticSearch();
        try(Scanner input = new Scanner(new ClassPathResource(args.getFile()).getFile())) {
            dataLoader.loadData(input);
            sleep(1500);
            while (input.hasNextLine()){
                String line = input.nextLine();
                String[] queryData = line.split("\"");
                System.out.println(String.format("\"%s\" \"%s\" \"%d\"", queryData[1], queryData[3], queryExecutor.executeRankingQuery(queryData[1], queryData[3])));
            }
        }
    }
}
