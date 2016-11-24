---
title: 'Implementation Guide for the Builder Editor Module version 3 '
service: 'Builder SDK'
area: 'Core'
type: Tutorial
order: 5
---

This brief how-to guide uses version 3 of the builder_editors module (**builder_editor_v3.js**) to outline the necessary implementation steps. This page serves as an addendum to the existing documentation for versions 1 and 2.


* Include the builder module: https://builder.yaas.io/public/js/builder_editors_v3.js<br>
* Add 'builder_editors' module to your angular module.<br>
* Prepare the configuration: <br>

```
 $scope.configuration = {
    'en': {'default': true, 'required': true},
    'de': {'default': false, 'required': false}
 };

```
* Next, specify the data that you would like to display in the element.  Here is an example:             

```
   $scope.localizedData = {
   'en': 'english text',
   'de': 'german text',
   };
   
```
* Modify your html to include the editor with its corresponding attributes.

Example:

```
<div class="panel-body">
 <h3>Single Line</h3>
 <builder-tab-editor builder-locales="configuration" data="localizedData" ng-form="someName2" ></builder-tab-editor>
</div>

```
<br>
****Attributes:****

- **data** - Required. Indicates which text should fill the editor prior to user input.
- **builder-validate-tabs** - Not required. Default false. Specifies whether a field should be validated or is required in order to save
- **builder-locales** - A structure specifing which locale is default and/or must be filled<br>
  

****Validating the builder tab editor****

If you want your editor to be validated, you need to add the builder-validate-tabs attribute set as true to the definition of the editor. Each value is validated.