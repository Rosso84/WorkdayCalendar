# WorkdayCalendar
A small maven application written in Java using modern java.time API's to calculate workdays. 
The main focus on the application is clean coding, re-useable code, testing (using JUnit Maven dependency) and structure using inner-classes.   
The application provides:

1. registration of fixed amount working hours pr day

2. registration of holidays and recurring holidays 

3. and calculation of an end Date based on a given starting Date and a number of days to work. 

## requirements

-Jdk 1.8 minimum   

-Maven installed  see: https://www.javatpoint.com/how-to-install-maven

-IDE (Intellij highly recommended (community edition is free), allthough you dont need much complicate ide to run this)     see: https://www.jetbrains.com/idea/download/#section=windows 

-commandline interface such as Git bash or just your built in CMD if using Windows    see: https://git-scm.com/downloads


## How to run application

1. open up a terminal, navigate to root folder of application and type maven command 'mvn clean install'. 
  This should run all 10 tests successfully and install all the necessary packages.

2. run main inside src/main/java/WorkdayCalendar   by clicking on geen arrow on left side after you scrolled down to main. Or if you dont need IDE just open up a terminal and navigate to src/main/java/  and type 'javac WorkdayCalendar.java' and then 'java WorkdayCalendar'. this should simply print out the calculation from a startdate to and end-date.
