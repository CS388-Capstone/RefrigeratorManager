# Milestone 1 - RefrigeratorManager (Unit 7)

## Table of Contents

1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)

## Overview

### Description

An app that allows the user to scan barcodes on the products in their refrigerator, and save data about the item. Subsequent scans of the same barcode will populate the previously saved data, or if an API is available, information can be pulled from it. The user can store the location if they have multiple refrigerators/freezers, and the expiration date of the item. The app will notify the user if the expiration date is approaching. If the barcode is unknown then users can manual enter data to be saved and other items with that bar code will refer to that data. Optionally, on-device AI models can suggest meal ideas, or cloud models can research for recipes that use the saved items.

### App Evaluation

[Evaluation of your app across the following attributes]
- **Category:** Organizational
- **Mobile:** Mobile is important since the app utilizes the camera for scanning the barcode and for sending notifications for food that is expiring soon. 
- **Story:** Helps users keep track of what's in their refrigerator(s) and manage their shopping list and meals that can be made.
- **Market:** This is for anyone that has a place that they keep food stored. They will also have had to have purchased items that have barcode labels still attached to them. Therefore this does not work for fully homecooked meals but rather the ingredients that the meals were purchased with. This also works for people that have multiple storage containers for each item they purchased.
- **Habit:** Users will want this application for everyday use as most people access their fridges every time they need to eat. They will be able to know when an item is going to expire soon or what their grocery list will look like for the future. 
- **Scope:** The final release will allow for users to use their cameras to scan virtual barcodes and gain information about a particular item and make a inventory of items in their refrigerator 

## Product Spec

### 1. User Features (Required and Optional)

**Required Features**

1. Barcode scannig - users can scan barcodes on food items using the phone camera.
2. Item information storage - the app stores prodict infomation associated with the barcode.
3. Manual item entry - users can manually enter the item information associated with an unknown barcode and that will auto fill data when that code is scanned again.
4. Expiration date tracking - users can enter expiration dates for each item.
5. Expiration notifications - app sends notificaitons when items are close to expiring.
6. Inventory list - users can view a list of all items stored in all refigerators and freezers.

**Optional Features**

1. Recipe suggestions - AI suggests recipes based on the items currently stores
2. Cloud sync - sync inventories across mutiple devices
3. Grocery list features - features like a shoping cart or having the price and calorie count of items in inventory

### 2. Screen Archetypes

- Home
  - view list of all stored items
  - see expiration dates
  - filter by location (mainfridge, garagefridge, freezer, etc)
- Barcode scanner 
  - scan barcode using phone camera
  - retrives saved item data
- Item edit screen
  - manual enter item information
  - set expiration date
  - assign storage location
- Item detail screen
  - view detailed information about item
  - edit expiration date or location
  - delete item from inventory
- Recipe suggestion screen (optional)
  - displays recipes based on items in inventory
- Settings screen
  - manage notification settings
  - manage storage locations
  - other settings that you would see in an app 
  

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home tab - home and inventory screen
* Scan tab - barcode scanner screen 
* Recipes tab (optional) - recipe suggestions screen
* settings tab - settings screen

**Flow Navigation** (Screen to Screen)

- Home Screen
  - navigate to item detail screen by clicking item
  - navigate to item edit screen
- Barcode scanner screen
  - if barcode found go to item detail screen
  - if barcode not found go to item edit screen
- Item edit screen
  - save item then for to home
- Item detail screen
  - edit item go to item edit screen
  - delete item go to home
- Recipe suggestions screen
  - select recipe and have ai talk about it in recipe detail view screen

## Wireframe


![388WireFrame](https://hackmd.io/_uploads/SyDxFpC5bl.png)

<br>

<br>

<br>

# Milestone 2 - Build Sprint 1 (Unit 8)

## GitHub Project board

[Add screenshot of your Project Board with three milestones visible in
this section]
<img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

## Issue cards

- [Add screenshot of your Project Board with the issues that you've been working on for this unit's milestone] <img src="YOUR_WIREFRAME_IMAGE_URL" width=600>
- [Add screenshot of your Project Board with the issues that you're working on in the **NEXT sprint**. It should include issues for next unit with assigned owners.] <img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

## Issues worked on this sprint

- List the issues you completed this sprint
- [Add giphy that shows current build progress for Milestone 2. Note: We will be looking for progression of work between Milestone 2 and 3. Make sure your giphys are not duplicated and clearly show the change from Sprint 1 to 2.]

<br>

# Milestone 3 - Build Sprint 2 (Unit 9)

## GitHub Project board

[Add screenshot of your Project Board with the updated status of issues for Milestone 3. Note that these should include the updated issues you worked on for this sprint and not be a duplicate of Milestone 2 Project board.] <img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

## Completed user stories

- List the completed user stories from this unit
- List any pending user stories / any user stories you decided to cut
from the original requirements

[Add video/gif of your current application that shows build progress]
<img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

## App Demo Video

- Embed the YouTube/Vimeo link of your Completed Demo Day prep video
