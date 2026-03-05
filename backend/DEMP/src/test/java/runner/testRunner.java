package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features/bookEvent.feature",
    glue = "testdefinitions",
    dryRun = false,                           
    plugin = {
        "pretty",
        "html:reports/HTMLReports.html",
        "json:reports/json_report.json",
        "junit:reports/junit_report.xml"
    },
    monochrome = true
)
public class testRunner extends AbstractTestNGCucumberTests { }