package pages;

import java.time.Duration;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import objectrepository.Locators;
import utils.ReadProperty;
import utils.Reporter;

public class AdminHomePage {

	
		private WebDriver driver;
		private WebDriverWait wait;
		ExtentTest test;
		public AdminHomePage(WebDriver driver, ExtentTest test) {			
			this.driver = driver;			
			this.test = test;
			wait=new WebDriverWait(driver, Duration.ofSeconds(5));
		}
// verify if its admin home page
		public boolean verifyAdminHomePage() {
			String currentUrl=driver.getCurrentUrl();
			boolean actResult;
			Properties prop = ReadProperty.readProperty();
			if(currentUrl.contains("admin")){
				actResult=true;
				Reporter.generateReport(driver, test, Status.PASS, "Admin page launch is success");				
					
				}
			else {
				actResult=false;
				Reporter.generateReport(driver, test, Status.FAIL, "Admin page launch is failed");
				
			}
			
			return actResult;
			
		}
//admin enters details and login into admin portal
		public boolean validateAdminLogin(String email,String password) {
			boolean actResult;
			driver.findElement(Locators.adminEmail).sendKeys(email);
			driver.findElement(Locators.adminPassword).sendKeys(password);
			driver.findElement(Locators.adminLoginBtn).click();
			actResult=true;
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(Locators.AdminPageVerify));
				Reporter.generateReport(driver, test, Status.PASS, "Login into admin portal is success");
			}catch(TimeoutException te) {
				actResult=false;
				Reporter.generateReport(driver, test, Status.FAIL, "Login into admin portal is failed");
			}
			return actResult;
			
		}		
}
