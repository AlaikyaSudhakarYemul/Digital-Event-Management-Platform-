
package utils;
import java.util.Properties;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Base {
    public static  WebDriver driver;

    public void launchBrowser() {
        Properties prop = ReadProperty.readProperty();
        if (prop.getProperty("Browser").equalsIgnoreCase("chrome")) {
            driver = new ChromeDriver();
        } else if (prop.getProperty("Browser").equalsIgnoreCase("edge")) {
            driver = new EdgeDriver();
        } else if (prop.getProperty("Browser").equalsIgnoreCase("firefox")) {
            driver = new FirefoxDriver();
        } else {
            System.out.println("Enter either chrome or firefox or edge");
        }
        driver.manage().window().maximize();
        driver.get(prop.getProperty("URL"));
    }

    public static void sleep(int msec) {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
