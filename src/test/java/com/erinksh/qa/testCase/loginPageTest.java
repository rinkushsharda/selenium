package com.erinksh.qa.testCase;

import com.erinksh.qa.base.TestBase;
import com.erinksh.qa.pages.loginPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;
import java.io.FileInputStream;
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


        XSSFWorkbook wb = new XSSFWorkbook(inputSream);
        XSSFSheet sheet = wb.getSheetAt(0);


        int rows = sheet.getLastRowNum();
        int column = sheet.getRow(1).getLastCellNum();

         for(int r=0;r<rows;r++){
             XSSFRow row = sheet.getRow(r);

                 for(int c=0;c<column;c++){

                    XSSFCell cell = row.getCell(c);

                    switch(cell.getCellType()){
                        case XSSFCell.CELL_TYPE_NUMERIC:
                            System.out.print(cell.getNumericCellValue());
                            break;
                        case XSSFCell.CELL_TYPE_STRING:
                            System.out.println(cell.getStringCellValue());
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + cell.getCellType());
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
