---
title: 'Implementation Guide for the Builder Editor Module version 3 '
service: 'Builder SDK'
area: 'Core'
type: Tutorial
order: 6
---

This brief how-to guide uses version 3 of the builder_editors module (**builder_editor_v3.js**) to outline the necessary implementation steps. This page serves as an addition to the existing documentation for versions 1 and 2.

1. Include the Builder module: https://builder.yaas.io/public/js/builder_editors_v3.js
2. Add the **builder_editors** module to your Angular module.
3. Prepare the configuration:

  ```
   $scope.configuration = {
     'en': {'default': true, 'required': true},
     'de': {'default': false, 'required': false}
    };

  ```

4. Specify the data to display in the element.

  **Example:**

  ```
    $scope.localizedData = {
     'en': 'english text',
     'de': 'german text',
   };

  ```

5. Modify your HTML to include the editor with its corresponding attributes.

  **Example:**

  ```
    <div class="panel-body">
     <h3>Single line</h3>
     <builder-tab-editor builder-locales="configuration" data="localizedData" ng-form="someName2" ></builder-tab-editor>
    </div>

  ```
<br>

<h3>Attributes:</h3>

- **data** - Required. Indicates which text fills the editor prior to user input.
- **builder-validate-tabs** - Not required. The default value is false. Specifies whether a field should be validated or is required in order to save.
- **builder-locales** - Specifies which locale is the default and/or must be filled-<br>
  

<h3>Validating the Builder tab editor</h3>

To allow validation in the editor:
1. Add the **builder-validate-tabs** attribute to the definition of the editor.
2. Set the attribute to ```true```. The system validates each value.
