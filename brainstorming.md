Mobile App Dev - App Brainstorming
===


## New App Ideas - List
1. 2D Multiplayer PVP 
    - A 2D multiplayer game where 2 players battle each other in real time on a 2D map. Players can move, dodge attacks, and use different weapons to eliminate the oponent. The game emphasizes quick reflexes, strategy, and teamwork, allowing for 2 players to play locally or against a bot.
2. AI Crossword Puzzle Generator
    - A crossword puzzle app that generates puzzles based on user input. Users can select a topic or import their own set of words and definitions that the AI model will then use to try and create a fun crossword puzzle. Users can play for fun based on specific interest or to help study.
3. Grocery list 
    - A app that allows you to have a list of groceries that can have things like price, calories, product catergory, etc. Maybe could give total prices of items on list as well. would save it per trip and maybe give a total cost per month with things like average cost per trip, etc. I dont know how hard it would be but maybe show items that are on sale that you frequently buy or something like that. Maybe add a stock list of items you have at home so you know when you have to get more. auto list maker of low stock items maybe if not too hard.

4. NJIT StudyBuddyFinder
    - A app that allows NJIT students to find study partners. The app would webscrape the current NJIT courses offerred per semester. Courses would be saved to a db(ex: supabase, firebase, mongodb) from there users could opt to join each course they're in or a specific section. Users can post a thread or respond to existing threads looking for study partners. Along with posting a thread, Users can also post groups that can be set to invite-only or public. Users will be able to message others in groups, send announcments, or schedule meetup times.

5. Refrigerator Manager
    - An app that allows the user to scan barcodes on the products in their refrigerator, and save data about the item. Subsequent scans of the same barcode will populate the previously saved data, or if an API is available, information can be pulled from it. The user can store the location if they have multiple refrigerators/freezers, and the expiration date of the item. The app will notify the user if the expiration date is approaching. Optionally, on-device AI models can suggest meal ideas, or cloud models can research for recipes that use the saved items.
    
6. Music Log
    - Taking inspiration from Letterboxd, allow users to review new music (either individually or in the context of an album). Allow users to browse each other's reviews. Optionally, use the API and SDK of a music streaming provider for in-app playback or suggestions

7. Travel Companion
    - Let users save travel information to allow them to quickly glance at needed information while traveling. Allow information for flights, drives, and hotel stays to be saved and visualized on a map. Update the user on the status of their flight, or gas prices on their drive.

8. excersize tracker
    - An app where you can add weekly exercising goals and a daily check to see if you completed your goals. maybe add a stacked bar graph to see how much diffrent exercises you do daily. Add notifications to remind user to excersize. maybe add a calorie tracker and input for users body mass and height to say how much calories or water you should be drinking and eating.
    
9. weather app
    - Simple weather app with typical weather app features like current temp, 7 day forcast, search by city etc. we've used this API before so it shouldnt be too hard.
    
10. movie/show/anime tracker
    - An app to keep track of movie/show/anime or all of them at the same time. Have features like rating, notes, and helps users keep track of things they have watched.

11. cooking recipe finder
    - Find cooking recipes through API if there is one like that. View ingredients and instructions by searching for recipe name or maybe ingredient. 

12. Phone health app
    - Get phone health app statistics and show in graph form. Have things like battery usage per week and how much the phone is used. Add other things like phone temp over the week and whatever else we can think of.
    
13. Journaling application
    - an application that has various prompts that you should fill out everyday. The goal being to write about how your day is or schedule different events for the future. 
15. Idle game 
    - an idle game like cookie clicker or venture capitalist where you have to click on the main screen to get money and can upgrade different aspects of your character to make more money. 
16. Flow free clone
    - Make a puzzle game similar to flow free where you have a grid and dots that can connect to each other but cannot intersect. 
    
## Top 3 New App Ideas
1. Idle Game
2. Movie/Show/Anime Tracker
3. Refrigator Manager

## New App Ideas - Evaluate and Categorize
1. Idle Game
    - **Description:** an idle game like cookie clicker or venture capitalist where you have to click on the main screen to get money and can upgrade different aspects of your character to make more money. 
    - **Category:** Game
    - **Mobile:** Uses touch controls, lightweight mobile game. 
    - **Story:** Users interested in a low effort, relaxing game will play this idle game. Progression is steady and will engage the users as their account gains upgrades over time
    - **Market:** Players interested in relaxing games. People without a lot of time to invest in a game consistently. 
    - **Habit:** Users will use this application often when they have a little bit of downtime to progress and make upgrades before going back to what they were doing. 
    - **Scope:** This iteration of gameplay would have a single currency system although later versions can have more currencies and progression is simple. 
2. Movie/Show/Anime Tracker
    - **Description:** An app to keep track of movie/show/anime or all of them at the same time. Have features like rating, notes, and helps users keep track of things they have watched.
    - **Category:** Organization
    - **Mobile:** Uses android apis to get upcoming data on films release schedule, notifications on upcoming releases and utilizes mobile data to update user at any time
    - **Story:** Users that would like to keep track of the latest in film and tv releases will use this app to get an idea of when the media they are interested in will release. 
    - **Market:** The market for this are people who are avid tv and movie consumers who would like to keep track of the series that they've watched and have things to say about them. 
    - **Habit:** Users would often use this application whenever they learn about a new film or tv show they are interested in or if they are talking to friends about what media they are interested in and how they feel about those titles. 
    - **Scope:** The scope of the tracking would remain limited to releasing movies and tv shows however there is a feature that will allow the users to create a list of shows previously watched as well as a space to write how they feel about each title. 
3. Refrigerator Manager
    - **Description:** An app that allows the user to scan barcodes on the products in their refrigerator, and save data about the item. Subsequent scans of the same barcode will populate the previously saved data, or if an API is available, information can be pulled from it. The user can store the location if they have multiple refrigerators/freezers, and the expiration date of the item. The app will notify the user if the expiration date is approaching. Optionally, on-device AI models can suggest meal ideas, or cloud models can research for recipes that use the saved items.
    - **Category:** Organizational
    - **Mobile:** Mobile is important since the app utilizes the camera for scanning the barcode and for sending notifications for food that is expiring soon. Mobile is necessary for the instant notifcations received on their device about the condition of their food.
    - **Story:** Helps users keep track of what's in their refrigerator(s) and manage their shopping list and meals that can be made.
    - **Market:** This is for anyone that has a place that they keep food stored. They will also have had to have purchased items that have barcode labels still attached to them. Therefore this does not work for fully homecooked meals but rather the ingredients that the meals were purchased with. This also works for people that have multiple storage containers for each item they purchased.
    - **Habit:** Users will want this application for everyday use as most people access their fridges every time they need to eat. They will be able to know when an item is going to expire soon or what their grocery list will look like for the future. 
    - **Scope:** The final release will allow for users to use their cameras to scan virtual barcodes and gain information about a particular item and make a inventory of items in their refrigerator.

Final app idea: Refrigerator Manager 
