package pages;

import java.time.Duration;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import objectrepository.Locators;
import utils.Reporter;

public class CreateEventPage {
	private WebDriver driver;
	private WebDriverWait wait;
	ExtentTest test;
	public CreateEventPage(WebDriver driver, WebDriverWait wait, ExtentTest test) {		
		this.driver = driver;		
		this.test = test;
		this.wait=new WebDriverWait(driver,Duration.ofSeconds(10));
	}
	public boolean verifyCreateEvent() {
		boolean actResult;
		driver.findElement(Locators.createEventBtn).click();
		try {
			actResult=true;
			wait.until(ExpectedConditions.visibilityOfElementLocated(Locators.createEventTxt));
			Reporter.generateReport(driver, test, Status.PASS, "create event button working successfully ");
		}catch(TimeoutException t) {
			actResult=false;
			Reporter.generateReport(driver, test, Status.FAIL,"create event button failed" );
		}
		return actResult;
		
	}
	public boolean eventCreation() {
		boolean actResult=false;
		 driver.findElement(Locators.eventName);
		 return actResult;
	}
	

}
