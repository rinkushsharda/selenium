package com.erinksh.qa.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class FireFoxDriverManager extends DriverManager{

    @Override
    protected void createWebDriver(){
        WebDriverManager.firefoxdriver().setup();
        this.driver = new FirefoxDriver();

    }


}
