package objectrepository;

import org.openqa.selenium.By;

public class Locators {
    //Locators for home page
    public static By signUpBtn=By.xpath("//button[text()=\"Sign up\"]");
    public static By signUpLink=By.xpath("//button[text()=\"Sign Up\"]");
    public static By verifySignUp=By.xpath("//h2[text()=\"Create a New Account\"]");
    //Locators for Register page
    public static By name=By.xpath("//input[@name=\"name\"]");
    public static By role=By.xpath("//select[@name=\"role\"]");
    public static By contact = By.xpath("//input[@name=\"contact\"]");
    public static By email=By.xpath("//input[@name=\"email\"]");
    public static By password=By.xpath("//input[@name=\"password\"]");
    public static By confirmPassword=By.xpath("//input[@name=\"confirmPassword\"]");    
    public static By registerBtn=By.tagName("button");
    //Locators for create event page
    public static By createEventBtn=By.xpath("//button[text()=\"Create Event\"]");
    public static By createEventTxt=By.xpath("//h2[text()=\"Create New Event\"]");
    		
    //Locators in user page
    public static By registerEventBtn=By.xpath("//button[text()=\"Register\"]");
    //Locators in book event page
    public static By bookEventBtn=By.tagName("button");
    public static By successBtn=By.xpath("//button[text()=\"OK\"]");
    public static By successMsg=By.xpath("//h2[text()=\"Success!\"]");
    // Locators for admin page
    public static By adminEmail=By.xpath("//input[@placeholder=\"Enter email\"]");
    public static By adminPassword=By.xpath("//input[@placeholder=\"Enter password\"]");
    public static By adminLoginBtn=By.xpath("//button[text()=\"Login\"]");
    public static By adminAdressTab=By.xpath("//*[@id=\"root\"]/div/div[1]/button[1]");
    public static By verifyAddressTab=By.xpath("//h2[text()=\"Add Address\"]");
    public static By adressInput=By.xpath("//input[@name=\"address\"]");
    public static By cityInput=By.xpath("//input[@name=\"city\"]");
    public static By stateInput=By.xpath("//input[@name=\"state\"]");
    public static By countryInput=By.xpath("//input[@name=\"country\"]");
    public static By pincodeInput=By.xpath("//input[@name=\"pincode\"]");
    public static By addAdressBtn=By.xpath("//button[text()=\"Add Address\"]");
    public static By verifyAddressAdded=By.tagName("ul");
    public static By speakerTab=By.xpath("//button[text()=\"Speaker\"]");
    public static By speakerNameInput=By.xpath("//input[@placeholder=\"Enter speaker name\"]");
    public static By speakerBioInput=By.xpath("//input[@placeholder=\"Enter speaker bio\"]");
    public static By addSpeakerBtn=By.xpath("//button[text()=\"Add Speaker\"]");
    public static By speakerAddedText=By.xpath("//p[text()=\"Speaker added!\"]");
    public static By AdminLogoutBtn=By.xpath("//button[text()=\"Logout\"]");
    public static By AdminPageVerify=By.tagName("h2");
    
    
    		


}
