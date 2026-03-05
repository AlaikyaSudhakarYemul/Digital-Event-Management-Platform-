Feature:book event
Scenario Outline: booking event is successful
Given user is on home page
When user enters name "<name>",select role "<role>", contact "<contact>", email "<email>",password "<password>",confirmPassword "<confirmPassword>" and click on SignUp button
And user select any event and clicks on register button
And user clicks on register button 
Then user gets event successfully registered alert window
And user clicks on ok button
Then user returns to home page