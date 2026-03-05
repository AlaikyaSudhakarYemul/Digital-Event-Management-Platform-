package objectrepository;

import org.openqa.selenium.By;

public class Locators {
    //Locators for home page
    public static By signUpBtn=By.xpath("//button[text()=\"Sign up\"]");
    public static By signUpLink=By.xpath("//button[text()=\"Sign Up\"]");
    //Locators for Register page
    public static By name=By.xpath("//input[@name=\"name\"]");
    public static By role=By.xpath("//select[@name=\"role\"]");
    public static By contact = By.xpath("//input[@name=\"contact\"]");
    public static By email=By.xpath("//input[@name=\"email\"]");
    public static By password=By.xpath("//input[@name=\"password\"]");
    public static By confirmPassword=By.xpath("//input[@name=\"confirmPassword\"]");
    public static By registerBtn=By.tagName("button");
    //Locators in user page
    public static By registerEventBtn=By.xpath("//button[text()=\"Register\"]");
    //Locators in book event page
    public static By bookEventBtn=By.tagName("button");
    public static By successBtn=By.xpath("//button[text()=\"OK\"]");
    public static By successMsg=By.xpath("//h2[text()=\"Success!\"]");
    


}
