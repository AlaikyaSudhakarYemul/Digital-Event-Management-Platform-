    Feature: admin 
    Scenario Outline: Add details about event
    Given user is on admin home page
    When user enters email "<email>" , password "<password>" and click on login button
    And user click on Address button
    And user enters address "<address>",state "<state>",country "<country>",pincode "<pincode>" and clicks on Add adress button
    And user click on Speaker button
    And user enters speaker name "<speakerName>", speaker bio "<speakerBio>" and clicks on Add speaker button
    And user clicks on logout button
    Then user returns to admin home page successfully
