package com.erinksh.qa.base;

import org.openqa.selenium.WebDriver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class TestBase {

    DriverManager driverManager;
    public  static WebDriver driver;
    public Properties prop;

    public TestBase(){
        try {
            prop = new Properties();
            FileInputStream fp = new FileInputStream(System.getProperty("user.dir") + "/src/main/java/com/erinksh/qa/config/config.properties");
            prop.load(fp);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void initialize(){

        System.out.println(prop.getProperty("user"));

        driverManager = DriverManagerFactory.getDriverManager(DriverManager.DriverType.CHROME);
         driver = driverManager.getWebDriver();
         driver.get(prop.getProperty("URL"));

    }



}
