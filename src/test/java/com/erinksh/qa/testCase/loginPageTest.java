package com.erinksh.qa.testCase;

import com.erinksh.qa.base.TestBase;
import com.erinksh.qa.pages.loginPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

public class loginPageTest extends TestBase {

    TestBase t1;
    WebDriver driver;

    @Test
    public void test(){

        initialize();
        loginPage lp = new loginPage();
        lp.login();
    }

    @Test
    public void  test1(){
        WebDriverManager.chromedriver().setup();
        WebDriver dr = new ChromeDriver();
        dr.get("http://msn.com");

    }


}
