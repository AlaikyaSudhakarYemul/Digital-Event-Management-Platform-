package testdefinitions;

import org.junit.Assert;
import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.ExtentTest;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.AdminPage;
import utils.ReadExcelData;
import pages.AdminHomePage;

public class AdminStepDef {
	private WebDriver driver = Hooks.driver;
	private ExtentTest test = Hooks.test;
	AdminHomePage adminhomepage;
	AdminPage adminpage;

	@Given("user is on admin home page")
	public void user_is_on_admin_home_page() {
		adminhomepage = new AdminHomePage(driver, test);
		Assert.assertTrue(adminhomepage.verifyAdminHomePage());

	}

	@When("user enters email {string} , password {string} and click on login button")
	public void user_enters_email_password_and_click_on_login_button(String email, String password) {

		String[][] inputdata = ReadExcelData.readExcelData();
		boolean actResult = adminhomepage.validateAdminLogin(inputdata[0][0], inputdata[0][1]);
		Assert.assertTrue(actResult);

	}

	@When("user click on Address button")
	public void user_click_on_address_button() {
		boolean actResult = adminpage.validateAddressTab();
		Assert.assertTrue(actResult);

	}

	@When("user enters address {string}, city {string},state {string},country {string},pincode {string} and clicks on Add adress button")
	public void user_enters_address_city_state_country_pincode_and_clicks_on_add_adress_button(String address,
			String city, String state, String country, String pincode) {
		String[][] inputdata = ReadExcelData.readExcelData();
		boolean actResult = adminpage.validateAddAddress(inputdata[0][2], inputdata[0][3], inputdata[0][4],
				inputdata[0][5], inputdata[0][6]);
		Assert.assertTrue(actResult);
	}

	@When("user click on Speaker button")
	public void user_click_on_speaker_button() {
		adminpage.clickSpeakerTab();

	}

	@When("user enters speaker name {string}, speaker bio {string} and clicks on Add speaker button")
	public void user_enters_speaker_name_speaker_bio_and_clicks_on_add_speaker_button(String speakerName,
			String speakerBio) {
		String[][] inputdata = ReadExcelData.readExcelData();
		boolean actResult = adminpage.validateAddSpeaker(inputdata[0][7], inputdata[0][8]);
		Assert.assertTrue(actResult);

	}

	@When("user clicks on logout button")
	public void user_clicks_on_logout_button() {
		adminpage.clickLogOutBtn();

	}

	@Then("user returns to admin home page successfully")
	public void user_returns_to_admin_home_page_successfully() {
		boolean actResult = adminpage.validateAdminHomePage();
		Assert.assertTrue(actResult);

	}

}
