package com.erinksh.qa.base;

public class DriverManagerFactory {

    public static DriverManager getDriverManager(DriverManager.DriverType Type){

        DriverManager driverManager;

        switch (Type) {

            case CHROME:
                driverManager = new ChromeDriverManager();
                break;
            case FF:
                driverManager = new FireFoxDriverManager();
                break;
            default:
                driverManager = new ChromeDriverManager();
        }
        return  driverManager;
    }


}
