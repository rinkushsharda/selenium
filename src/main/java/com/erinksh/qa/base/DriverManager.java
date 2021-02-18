package com.erinksh.qa.base;

import org.openqa.selenium.WebDriver;

public abstract class DriverManager {

    protected WebDriver driver;

    protected abstract void createWebDriver();

    public WebDriver getWebDriver() {
        createWebDriver();
        return driver;
    }
    public enum DriverType{
        CHROME, FF, GECKO
    }


}
