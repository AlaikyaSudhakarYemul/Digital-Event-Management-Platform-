package pages;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HomePage {
	private WebDriver driver;
	private WebDriverWait wait;
	
	public HomePage(WebDriver driver) {
		super();
		this.driver = driver;
		wait=new WebDriverWait(driver,Duration.ofSeconds(5));
	}
}
