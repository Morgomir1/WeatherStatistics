package weatherStatistics.util;

import org.springframework.boot.SpringApplication;
import weatherStatistics.WeatherStatisticsApplication;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

public class IdGenerator {

    public static void main(String[] args) {
        BufferedReader reader;
        BufferedWriter writer;
        try {
            reader = new BufferedReader(new FileReader("D:\\ВГУ\\ТП\\WeatherStatistics\\src\\main\\resources\\stats.csv"));
            writer = new BufferedWriter(new FileWriter("D:\\ВГУ\\ТП\\WeatherStatistics\\src\\main\\resources\\stats.csv"));
            String line = reader.readLine();
            int number = 52642;
            while (line != null) {
                System.out.println(line);
                // read next line
                line = reader.readLine();
                if (line != null) {
                    number--;
                    line = number + line + "\n";
                    writer.write(line);
                }
            }
            writer.flush();
            writer.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
