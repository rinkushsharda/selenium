package com.erinksh.qa.base;

public class DriverManagerFactory {

    public static DriverManager getDriverManager(DriverManager.DriverType Type){

        DriverManager driverManager;

        driverManager = new ChromeDriverManager();

        return  driverManager;
    }


}
