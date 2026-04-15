package testdefinitions;

import org.junit.Assert;
import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.ExtentTest;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.HomePage;
import pages.RegisterPage;

public class CreateEventStepDef {
	private WebDriver driver = Hooks.driver;
	private ExtentTest test = Hooks.test;
	HomePage homePage;
	RegisterPage registerPage;

	@Given("user is on home page")
	public void user_is_on_home_page() {
		homePage = new HomePage(driver, test);
		Assert.assertTrue(homePage.verifyHomePage());
	}

	@When("user clicks on SignUp button and clicks on SignUp link")
	public void user_clicks_on_sign_up_button_and_clicks_on_sign_up_link() {
		Assert.assertTrue(homePage.verifySignUp());
	}

	@When("user enters name {string}, select role {string},enters contact {string}, email {string}, password {string} , confirmPassword {string} and click on SignUp button")
	public void user_enters_name_select_role_enters_contact_email_password_confirm_password_and_click_on_sign_up_button(
			String name, String role, String contact, String email, String password, String confirmPassword) {
		registerPage = new RegisterPage(driver, null, test);
		Assert.assertTrue(registerPage.verifyAccountCreation(name, role, contact, email, password, confirmPassword));
		
	}

	

	@When("user clicks on create event button")
	public void user_clicks_on_create_event_button() {
		// Write code here that turns the phrase above into concrete actions
		throw new io.cucumber.java.PendingException();
	}

	@When("user enters eventName {string}, enters eventDescription {string}, select eventDate {string},select eventTime {string}, enters maxAttendees {string}, select speaker {string}, select address {string}, select eventType {string} and click on create event button")
	public void user_enters_event_name_enters_event_description_select_event_date_select_event_time_enters_max_attendees_select_speaker_select_address_select_event_type_and_click_on_create_event_button(
			String string, String string2, String string3, String string4, String string5, String string6,
			String string7, String string8) {
		// Write code here that turns the phrase above into concrete actions
		throw new io.cucumber.java.PendingException();
	}

	@Then("user gets event successfully created alert window")
	public void user_gets_event_successfully_created_alert_window() {
		// Write code here that turns the phrase above into concrete actions
		throw new io.cucumber.java.PendingException();
	}

	@Then("user clicks on ok button")
	public void user_clicks_on_ok_button() {
		// Write code here that turns the phrase above into concrete actions
		throw new io.cucumber.java.PendingException();
	}

	@Then("user returns to home page")
	public void user_returns_to_home_page() {
		// Write code here that turns the phrase above into concrete actions
		throw new io.cucumber.java.PendingException();
	}

}
