---
title: 'Design a Builder UI with Skeletor'
service: 'Builder SDK'
area: 'Core'
type: Tutorial
order: -
---

The Builder CLI features the Skeletor tool with which you can easily create views just by dragging and dropping UI components. <br><br>

<h3> How to use the Skeletor</h3>


1. Open a terminal window.

 <img src="img/builder_cli_menu_runSkeletor.png"><br><br>

2. Run the command ```builder runSkeletor``` to start the Skeletor. The HTTP server will start locally.
3. Enter the URL http://localhost:8082 in a browser.  The Skeletor design interface will appear. 
4. To design a view, drag and drop the components from the left of the screen onto the main screen. Organize the components as they should appear in the Builder UI. 

  <img src="img/skeletor_ui_drag.png" style="width:600px" class="img-click-modal" alt="The Skeletor UI"/><br><br>
5. After you have created the view, you can preview your results by clicking the preview button to the right of the delete icon.  The view it will be immediately stored in the local cache of your browser. That means you’ll never loose the view until you delete it by explicitly pressing the delete button.

  <img src="img/skeletor_ui_menu.png" width="35%" class="img-click-modal" alt="The Skeletor UI"/><br>
  
6. Next, export the code that Skeletor generates by clicking on the brackets icon to the right of the preview icon.
7. Here is an example of a UI with its corresponding code preview:<br>

  <img src="img/skeletor_ui_completed.png" class="img-click-modal" alt="The Skeletor UI"/><br><br>
  <img src="img/skeletor_code_sample.png" class="img-click-modal" alt="Code Preview"/><br><br>
  
  If you are using Chrome or Firefox, a download button is available in preview mode allowing you to download the code to a file. 
  
  <img src="img/skeletor_menu_download_button.png"  width="35%" class="img-click-modal" alt="The Skeletor download button"/><br><br>
  Internext Explorer and Safari do not support the download button. Instead, you can copy the HTML code directly from the broswer. <br><br>
  
  
<h3>Component Settings</h3>

You can adjust the settings for each component in the Skeletor. This allows you to assign custom text and and labelling to the component. For example, the <strong>Page Header</strong> component features a <strong>Back</strong> button, a long page title, as well as a <strong>Save</strong> and <strong>Cancel</strong> button by default. You can set the default text of the title and rename each of the buttons.


<br>
  <img src="img/component_settings.png" class="img-click-modal" alt="Code Preview"/>
