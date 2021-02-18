package com.erinksh.qa.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;

public class ChromeDriverManager extends DriverManager {

    @Override
    protected void createWebDriver() {
        WebDriverManager.chromedriver().setup();
        this.driver = new ChromeDriver();
    }



}
