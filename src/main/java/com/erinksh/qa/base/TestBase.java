package com.erinksh.qa.base;

import org.openqa.selenium.WebDriver;

import java.sql.Driver;

public class TestBase {

    DriverManager driverManager;
    public  static WebDriver driver;

    public void initialize(){

         driverManager = DriverManagerFactory.getDriverManager(DriverManager.DriverType.CHROME);
         driver = driverManager.getWebDriver();
         driver.get("https://www.freecrm.com");

    }



}
