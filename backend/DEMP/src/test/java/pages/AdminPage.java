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

public class AdminPage {
	private WebDriver driver;
	private WebDriverWait wait;
	ExtentTest test;

	public AdminPage(WebDriver driver, WebDriverWait wait, ExtentTest test) {
		this.driver = driver;
		this.test = test;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
	}

	public boolean validateAddressTab() {
		boolean actResult;
        wait.until(ExpectedConditions.elementToBeClickable(Locators.adminAdressTab)).click();		
		driver.findElement(Locators.adminAdressTab).click();
		actResult = true;
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(Locators.verifyAddressTab));
			Reporter.generateReport(driver, test, Status.PASS, "address tab opened successfully");
		} catch (TimeoutException te) {
			actResult = false;
			Reporter.generateReport(driver, test, Status.FAIL, "opening address tab failed");

		}
		return actResult;
	}

	public boolean validateAddAddress(String address, String city, String state, String country, String pincode) {
		boolean actResult;

		driver.findElement(Locators.adressInput).sendKeys(address);
		driver.findElement(Locators.cityInput).sendKeys(city);
		driver.findElement(Locators.stateInput).sendKeys(state);
		driver.findElement(Locators.countryInput).sendKeys(country);
		driver.findElement(Locators.pincodeInput).sendKeys(pincode);
		driver.findElement(Locators.addAdressBtn).click();
		actResult = true;
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(Locators.verifyAddressAdded));
			Reporter.generateReport(driver, test, Status.PASS, "adress added successfully in admin panel");
		} catch (TimeoutException te) {
			actResult = false;
			Reporter.generateReport(driver, test, Status.FAIL, "adding address failed in admin panel");

		}
		return actResult;

	}

	public void clickSpeakerTab() {
		driver.findElement(Locators.speakerTab).click();
	}

	public boolean validateAddSpeaker(String speakerName, String speakerBio) {
		boolean actResult;
		driver.findElement(Locators.speakerNameInput).sendKeys(speakerName);
		driver.findElement(Locators.speakerBioInput).sendKeys(speakerBio);
		driver.findElement(Locators.addSpeakerBtn).click();
		actResult = true;
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(Locators.speakerAddedText));
			Reporter.generateReport(driver, test, Status.PASS, "adress added successfully in admin panel");
		} catch (TimeoutException te) {
			actResult = false;
			Reporter.generateReport(driver, test, Status.FAIL, "adding address failed in admin panel");

		}
		return actResult;
	}

	public void clickLogOutBtn() {
		driver.findElement(Locators.AdminLogoutBtn).click();
	}

	public boolean validateAdminHomePage() {
		boolean actResult = true;
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(Locators.AdminPageVerify));
			Reporter.generateReport(driver, test, Status.PASS, "admin page verification successful on logout");
		} catch (TimeoutException te) {
			actResult = false;
			Reporter.generateReport(driver, test, Status.FAIL, "admin page verification failed on logout");
		}
		return actResult;
	}

}
