---
title: 'The localized editor component in the Builder'
service: 'Builder SDK'
area: 'Core'
type: Tutorial
order: 4
---

The localized editor is a custom Angular directive in **builder_editors** with a dependency to the **builder**.

### Configure the localized editor

As a Builder module developer, you need to complete a few simple tasks:

* Include the https://builder.yaas.io/public/js/builder_editors.js (or builder_editors_v2.js) file. Add **builder_editors** to your module.
* The directive expects data in the example format:
```js
    /*simple string with localized property label */
    $scope.localizedLabel = 'Demo Label';

    /*array of json objects containing language iso and localized value of property*/
    $scope.localizedData = [
        {iso: 'en', value: 'english text'},
        {iso: 'de', value: 'german text'},
        {iso: 'pl', value: 'polish text'}];
```
Since version 2 of the **builder_editors** structure of localized data has been changed, the localized editor accepts data in example format:
```js
    /*json object containing language iso and localized value of a property*/
    $scope.localizedData = {
        'en': 'english text',
        'de': 'german text',
        'pl': 'polish text'};
```
Remember that a list of languages (currencies) in which the property is localized has to match the list of defined languages (currencies) for the current user.
* HTML markup
```html
    <div class="panel-body">
        <h3>Single Line</h3>
        <localized-text-editor data="localizedData" ng-form="someName2" builder-locale-type="currencies"></localized-text-editor>
    </div>
```
The <b>builder-locale-type</b> attribute enables you to choose between a localized editor for languages and for currencies. You must set the value to `languages` or `currencies`, respectively. That attribute is required for a localized editor component. Please avoid configuration that will render your code invalid. For example, a multi-line currency editor is an example of configuration that is not supported.
The <b>builder-editor-type</b> attribute defines the type of editor. If it is empty, that indicates a single-line editor. If it contains a tab, that indicates a multi-line editor with tabs for languages. For the multi-line editor, you can specify the number of rows using the <b>builder-editor-rows</b> attribute. The <b>localizedData</b> object is passed on the <b>data</b> attribute, and the localized field label is passed with the <b>label</b> attribute.

### Validate the localized editor

If you want your localized editor to be validated, you need to add the <b>builder-validate-required-languages</b> attribute set as `true` to the definition of a localized editor. Every value of the localized property is validated.

There are two warning indicators used in the validation process:

<ul>
<li> **globe** - Informs indirectly that one or more fields with required language from the list is empty.</li>
<li> **frame text box** - Informs directly which field from the required ones you need to fill in.</li>
</ul>
The validation process consists of two stages. You can find more detailed information about each of them below:

<ol>
<li> On the load view, warning indicators are highlighted in orange if one or more required fields are empty. This stage is implemented out of the box if you apply localized editor validation.</li>
<br>
<li> Optionally, you can broadcast the following inside your controller to add one more validation stage:
<br><br>
```js
    $rootScope.$broadcast($scope.someName2.$name + '-validate-on-save');
```
<br><br> In this second stage, when user tries to save or publish the item and there are required text boxes that are still empty or filled incorrectly, your localized editor is re-validated. The event is broadcast and both indicators turn red.</li>
</ol>

  <div class="note panel">
        If a **globe warning** indicator is turned on, it means that at least one required field is empty.  It might be hidden in a drop-down list. Make sure you've checked it properly.
    </div>

### Customize the localized editor (introduced with version 2)

The <b>builder-locales</b> lets you inject your own configuration of languages. This functionality lets you implement custom validation scenarios for specific cases. Be aware that configuration of locales for the current user is the default configuration for the localized editor. It can be overridden by injecting the following object:
```js
    /*json object containing language iso as key and configuration as value*/
    $scope.configuration = {
        'en': {'default': true, 'required': true},
        'de': {'default': false, 'required': false}
        };
```
HTML markup:
```html
    <div class="panel-body">
        <h3>Single Line</h3>
        <localized-text-editor data="localizedData" builder-locales="configuration" ng-form="someName2" builder-locale-type="currencies"></localized-text-editor>
    </div>
```
