package pages;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import objectrepository.Locators;
import utils.ReadProperty;
import utils.Reporter;

import java.time.Duration;
import java.util.Properties;


public class HomePage {
	private WebDriver driver;
	private WebDriverWait wait;
	ExtentTest test;
	
	public HomePage(WebDriver driver,ExtentTest test) {
		super();
		this.driver = driver;
		this.test=test;
		this.wait=new WebDriverWait(driver,Duration.ofSeconds(10));
	}
// verify whether it is home page
	public boolean verifyHomePage(){
		String currentUrl=driver.getCurrentUrl();
		boolean actResult;
		Properties prop = ReadProperty.readProperty();
		if(currentUrl.contains("localhost")){
			actResult=true;
			Reporter.generateReport(driver, test, Status.PASS, "Home page launch is successful");				
				
			}
		else {
			actResult=false;
			Reporter.generateReport(driver, test, Status.FAIL, "Home page launch is failed");
			
		}
		return actResult;		
	}
//verify the signup link and button on home page
	public boolean verifySignUp() {
		boolean actResult;
		driver.findElement(Locators.signUpBtn).click();
		driver.findElement(Locators.signUpLink).click();
		actResult=true;
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(Locators.verifySignUp));
			Reporter.generateReport(driver, test, Status.PASS, "opening Signup link is successful ");
			
		}catch(TimeoutException te) {
			actResult=false;
			Reporter.generateReport(driver, test, Status.FAIL, "opening Signup link failed");
			
		}
		return actResult;
	}
	
	
}
