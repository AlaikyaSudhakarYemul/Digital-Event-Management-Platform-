package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "C:\\Users\\PR20586952\\Documents\\GitHub\\Digital-Event-Management-Platform-\\backend\\DEMP\\src\\test\\resources\\features\\createEvent.feature",
    glue = "testdefinitions",
                              
    plugin = {
        "pretty",
        "html:reports/HTMLReports.html",
        "json:reports/json_report.json",
        "junit:reports/junit_report.xml"
    },
    monochrome = true
)
public class testRunner extends AbstractTestNGCucumberTests { }