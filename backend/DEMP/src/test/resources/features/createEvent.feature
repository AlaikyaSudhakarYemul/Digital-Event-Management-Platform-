Feature: create event

  Scenario Outline: creating event is successful
    Given user is on home page
    When user clicks on SignUp button and clicks on SignUp link
    And user enters name "<name>", select role "<role>",enters contact "<contact>", email "<email>", password "<password>" , confirmPassword "<confirmPassword>" and click on SignUp button
    And user clicks on create event button
    And user enters eventName "<eventName>", enters eventDescription "<eventDescription>", select eventDate "<eventDate>",select eventTime "<eventTime>", enters maxAttendees "<maxAttendees>", select speaker "<speaker>", select address "<address>", select eventType "<eventType>" and click on create event button
    Then user gets event successfully created alert window
    And user clicks on ok button
    Then user returns to home page
 	Examples:
	|	 	name	 | 		role	| 	contact	|	email							 |	password		|	confirmPassword	|
	|organizerA|Organizer	|9876543210 |organizerA@gmail.com|Organizera@123|Organizera@123		|
   
