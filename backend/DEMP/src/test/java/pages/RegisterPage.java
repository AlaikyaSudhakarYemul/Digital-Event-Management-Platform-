package pages;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.ExtentTest;

import objectrepository.Locators;

public class RegisterPage {
	private WebDriver driver;
	private WebDriverWait wait;
	ExtentTest test;
	public RegisterPage(WebDriver driver, WebDriverWait wait, ExtentTest test) {		
		this.driver = driver;		
		this.test = test;
		this.wait=new WebDriverWait(driver,Duration.ofSeconds(5));
	}
//verify the new account creation process
	public boolean verifyAccountCreation(String name,String role,String contact,String email,String password,String confirmPassword) {
		boolean actResult;
		driver.findElement(Locators.name).sendKeys(name);
		Select roleDropdown=new Select(driver.findElement(Locators.role));
		roleDropdown.selectByContainsVisibleText(role);
		driver.findElement(Locators.contact).sendKeys(contact);
		driver.findElement(Locators.email).sendKeys(email);
		driver.findElement(Locators.password ).sendKeys(password);
		driver.findElement(Locators.confirmPassword).sendKeys(confirmPassword);
		Actions action=new Actions(driver);
		WebElement RegisterBtn=driver.findElement(Locators.registerBtn);
		action.moveToElement(RegisterBtn).click().perform();
		return actResult=true;
		
		
	}

}
