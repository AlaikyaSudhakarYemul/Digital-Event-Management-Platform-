package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features/admin.feature",
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