---
title: 'How to implement widgets for Project Dashboard'
service: 'Builder SDK'
area: 'Core'
type: Tutorial
order: 13
---

Widgets are super cool. Advertise the whole concept a bit..

In this Tutorial we will create widget showing number of email templates
Prerequisites
 - subscription to email package

 ### Who can use my widget

 - visible in my project as it comes from my development
 - visible to all subscribers of the package if the widget was included

 ### What resources can my widget access

 - what ever is accessible for the given tenant where the widget is used
 - scope restrictions



### Generate files

Use builder cli... bla

```
$ builder createModule widget

Available templates:

0. simple - Only the bare minimum needed for a builder module.
1. demo - Contains many examples of various features.
2. list - A view displaying a list, already prepared for making calls to an API via Restangular.
3. uiExtensibilityHost - Contains a view capable of including plugin modules.
4. uiExtensibilityPlugin - Plugin module example.
5. projectDashboardWidget - Project Dashboard Widget example.
```

use option 5.

files inspection.. what was generated.

### Run the widget 

Run it via CLI and create a locally hosted module

### Implement

```
put some code here
```

### Register widget

Widget modules, just like all other Builder modules, must be registered in YaaS.
So, after you have implemented and deployed them, use the Builder to register the modules in YaaS and include them in the target packages that your project provides.

Defining a widget is no different from defining any other Builder module.
When registering a widget module, you should complete the same tasks as for other modules:
 - Provide a URL for the **module.json** file where it was deployed.
 - Specify what can it do on behalf of the tenant (the *authorization scopes* of the module),
but pay special attention when defining redirect URIs.
 - Make sure that you define the redirect URI for the widget: `https://{baseURL of your plug-in}/builder/auth.html`. This endpoint is important for the widget to capture the access token that was granted for its authorization context.


