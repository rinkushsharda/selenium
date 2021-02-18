package com.erinksh.qa.testCase;

import com.erinksh.qa.base.TestBase;
import com.erinksh.qa.pages.loginPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class loginPageTest extends TestBase {

    @Test
    public void test(){
        initialize();
        loginPage lp = new loginPage();
        lp.login();
    }

    @Test
    public void data() throws IOException {

        String excel = System.getProperty("user.dir") + "/src/main/java/com/erinksh/qa/testdata/testdata.xlsx";
        FileInputStream inputSream = new FileInputStream(excel);


        HSSFWorkbook wb = new HSSFWorkbook(inputSream);
        HSSFSheet sheet = wb.getSheetAt(0);


        int rows = sheet.getLastRowNum();
        int column = sheet.getRow(1).getLastCellNum();

         for(int r=0;r<rows;r++){
             HSSFRow row = sheet.getRow(r);

                 for(int c=0;c<column;c++){

                    HSSFCell cell = row.getCell(c);

                    switch(cell.getCellType()){

                        case STRING:
                            System.out.print(cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            System.out.println(cell.getNumericCellValue());
                            break;

                    }


             }

         }




    }

    @Test
    public void  test1(){
        WebDriverManager.chromedriver().setup();
        WebDriver dr = new ChromeDriver();
        dr.get("http://msn.com");

    }


}
