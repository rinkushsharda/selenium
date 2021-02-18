package com.erinksh.qa.pages;


import com.erinksh.qa.base.TestBase;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class loginPage extends TestBase {

    @FindBy(xpath="//span[contains(text(),\"Log In\")]")
    WebElement login;

    @FindBy(xpath="//input[@name='email']")
    WebElement username;

    @FindBy(xpath="//input[@name='password']")
    WebElement password;

    @FindBy(xpath="//div[text()=\"Login\"]")
    WebElement loginBtn;

    public loginPage(){
        PageFactory.initElements(driver, this);
    }

    public void login(){

        login.click();
        username.sendKeys(prop.getProperty("user"));
        password.sendKeys(prop.getProperty("password"));
        loginBtn.click();
    }
}
