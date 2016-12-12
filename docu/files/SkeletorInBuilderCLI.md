---
title: 'Design a Builder UI with Skeletor'
service: 'Builder SDK'
area: 'Core'
type: Tutorial
order: -
---

The Builder CLI features the Skeletor tool with which you can easily create user interfaces just by dragging and dropping UI components. 

To run and use Skeletor:


1. Open a terminal window 
2. Navigate to the folder that contains your builder module.

 <img src="img/builder_cli_menu_runSkeletor.png"><br><br>

3. Run the command ```./builder runSkeletor``` to start the Skeletor. The HTTP server will start locally.
4. Enter the URL http://localhost:8082 in a browser.  The Skeletor design interface will appear. 
5. To design a UI, drag and drop the components from the left of the screen onto the main screen. Organize the components as they should appear in the Builder UI. 

  <img src="img/skeletor_ui.png" style="width:600px" class="img-click-modal" alt="The Skeletor UI"/><br><br>
6. After you have created the UI, export the code that Skeletor generates. To do this click on the brackets icon in the top right corner of the screen.

  <img src="img/skeletor_ui_menu.png" class="img-click-modal" alt="The Skeletor UI"/><br><br>
7. Here is an example of a UI with its corresponding code preview:

  <img src="img/skeletor_ui_completed.png" class="img-click-modal" alt="The Skeletor UI"/><br><br>
  <img src="img/skeletor_code_sample.png" class="img-click-modal" alt="Code Preview"/><br><br>
  
  You can copy the HTML code directly from the broswer. Some browsers allow you to download the code to a file.