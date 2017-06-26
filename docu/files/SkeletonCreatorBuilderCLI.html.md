---
title: 'Design a Builder UI with Skeleton Creator'
service: 'Builder SDK'
area: 'Core'
type: Tutorial
order: 3
---

The Builder CLI features the Skeleton Creator tool, with which you can easily create views just by dragging and dropping UI components. Here are the steps to get started.<br>

1. Open a terminal window.

  <img style="width: 30%;" max-width: 326px src="img/builder_cli_menu_runSkeletor.png" class="img-click-modal" alt="Skeleton Creator"><br><br>

2. Run the command `builder runSkeletonCreator` to start the Skeleton Creator. The HTTP server starts locally.
3. Enter the URL <b>http://localhost:8082</b> in a browser.  The Skeleton Creator design interface will appear.
4. To design a view, drag and drop the components from the left of the screen onto the main screen. Organize the components as they should appear in the Builder UI.

  <img style="width: 30%;" max-width: 326px src="img/skeletor_ui_drag.png" class="img-click-modal" alt="The Skeleton Creator UI"/><br><br>

5. After you create the view, click the preview button to the right of the delete button.  The browser immediately stores your view in the local cache. That means you will not loose the view until you delete it by explicitly clicking on the delete button.

  <img style="width: 30%;" max-width: 326px src="img/skeletor_ui_menu.png" class="img-click-modal" alt="The Skeleton Creator UI"/><br>

6. Export the code that the Skeleton Creator generates by clicking on the show button to the right of the preview button.

  Here is an example of a UI with its corresponding code preview:<br>

  <img style="width: 30%;" max-width: 326px src="img/skeletor_ui_completed.png" class="img-click-modal" alt="The Skeleton Creator UI"/><br><br>
  <img style="width: 30%;" max-width: 326px src="img/skeletor_code_sample.png" class="img-click-modal" alt="Code Preview"/><br><br>

  If you are using Chrome or Firefox, a download button is available in preview mode allowing you to download the code to a file.

  <img style="width: 30%;" max-width: 326px src="img/skeletor_menu_download_button.png"  width="35%" class="img-click-modal" alt="The Skeleton Creator download button"/><br><br>
  Internet Explorer and Safari do not support the download button. Instead, you can copy the HTML code directly from the broswer. <br><br>


**Component settings**

You can adjust the settings for each component in the Skeleton Creator. This allows you to assign custom text and and labeling to the component. For example, the <strong>Page Header</strong> component features a <strong>Back</strong> button, a long page title, as well as a <strong>Save</strong> and <strong>Cancel</strong> button by default. You can set the default text of the title and rename each of the buttons.

<br>
  <img style="width: 30%;" max-width: 326px src="img/component_settings.png" class="img-click-modal" alt="Code Preview"/><br><br>
  

