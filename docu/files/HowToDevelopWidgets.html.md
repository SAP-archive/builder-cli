---
title: 'How to implement widgets for Project Dashboard'
service: 'Builder SDK'
area: 'Core'
type: Tutorial
order: 13
---

Widgets are super cool. David, advertise the whole concept a bit..

In this Tutorial we will create widget showing number of email templates.

The widget would need to access velocity teamplates service to fetch and visualize the data. So the the project which would use the widget needs to be entitled to use
https://api.us.yaas.io/hybris/velocity-template/v1 endpoint.

So before starting implementation, make sure that in your project you have a subscription to email package.

Note, that subscribing to package from different markets would give you access to services deployed in defferent regions. In the following example we assume that subscription was made on US, or BETA market. If your Subscription comes from German market, please consume the data via 'eu' region.

 ### If I implement and deploy a widget, who can use it?

 - visible in my project if it was developed and registered as builder module in my project
 - visible to all projects who subscribe the package where the widget was included 

 ### What resources can my widget access

 - Widget can consume what ever services are accessible for the given project where the widget is used (either services comming from subscriptions or own development)
 - Data access restrictions are defined via scopes (just as for regular builder modules.) 


### Generate files

Since widget, from technical point of view, is a builder module, use builder cli to create new builder module. 

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

Select Option 5. In the next step, you will be asked to provide some details about what data and from which API would your widget consume.

    ```
    Please enter name for your widget.
    > Velocity Templates Count
    Please enter base url of the service for your widget.
    > https://api.us.yaas.io/hybris/velocity-template/v1
    Please enter resource for your widget to consume.
    > /templates
    ```

Inspect the files that were generated.
You see :
 - view (widget.html) and the controller (widgetApp.js) for the widget
 - module descriptor (module.json)
 - auth.html for the widget to consume its own authorization context
 - file supporting deployment (manifest.yml, nginx.conf) 


### Implement

Implement the view and the controller in a way that your widget consumes the _https://api.us.yaas.io/hybris/velocity-template/v1/{currentProject}/templates_
 endpoint and shows the total count of velocity templates defined in the current project.

**widgetApp.js**
    ```
    angular.module('widgetApp', ['builderPlugin'])
    .controller('WidgetCtrl', ['$scope','Restangular',function($scope, Restangular){
        $scope.totalCount;
        BuilderPlugin.ready(function() {
            $scope.currentProjectId = BuilderPlugin.authorizationData.tenantId
            Restangular
                .setBaseUrl(BuilderPlugin.settings.serviceBasePath)
                .withConfig(function(RestangularConfigurer) {
                    RestangularConfigurer.setFullResponse(true);
                })
                .one($scope.currentProjectId+"/templates?pageSize=1&pageNumber=1&totalCount=true") // append &totalCount=true if you need totalCount in the response
                .getList()
                .then(function(response){
                    $("#spinner").remove();
                    $scope.totalCount = response.headers("Hybris-Count");
                },function(error){
                    $("#spinner").remove();
                    //handle error
                }); 
       });
    }]);
    ```


**widget.html**
    ```
    <!DOCTYPE html>
    <html>
        <head lang="en">
            <meta charset="UTF-8">
            <!-- custom stylesheets -->
            <link href="https://builder.yaas.io/public/css/styles.css" rel="stylesheet" />
            <script src="https://builder.yaas.io/public/js/vendor/jquery/dist/jquery.min.js"></script>
            <script src="https://builder.yaas.io/public/js/vendor/underscore/underscore.js"></script>
            <script src="https://builder.yaas.io/public/js/vendor/angular/angular.js"></script>
            <script src="https://builder.yaas.io/public/js/vendor/restangular/dist/restangular.js"></script>
            <script src="https://builder.yaas.io/public/js/builder_plugin.js"></script>
            <script src="js/widgetApp.js"></script>
            <title>Widget</title>
        </head>
        <body class="widget-wrapper" ng-app="widgetApp">
            <div class="panel panel-default" id="spinner">
                <div class="panel-body">
                    <div class="spinner">
                    <div class="spinner-container spinner-container1">
                        <div class="spinner-circle1"></div>
                        <div class="spinner-circle2"></div>
                        <div class="spinner-circle3"></div>
                        <div class="circle4"></div>
                    </div>
                    <div class="spinner-container spinner-container2">
                        <div class="spinner-circle1"></div>
                        <div class="spinner-circle2"></div>
                        <div class="spinner-circle3"></div>
                        <div class="circle4"></div>
                    </div>
                    <div class="spinner-container spinner-container3">
                        <div class="spinner-circle1"></div>
                        <div class="spinner-circle2"></div>
                        <div class="spinner-circle3"></div>
                        <div class="circle4"></div>
                    </div>
                    </div>
                </div>
            </div>
            <div sizeElement class="widget-container" ng-controller="WidgetCtrl">
                <h4>Number fo templates</h4>
                <h1 class="numbotron-value">{{totalCount}}</h1>
            </div>
        </body>
    </html>
    ```

### Run the widget locally

Run it locally via CLI 
    ```
    $ builder runModule
    ```
Go to https://localhost:8081/builder/module.json in your browser and accept the security exception. 

### Register local instance of your widget

Defining a widget Builder Module is no different from defining any other Builder module.
When registering a widget module, you should complete the same tasks as for other modules:
 - Provide a URL for the **module.json** file where it was deployed.(For locally running widget : `https://localhost:8081/builder/module.json`)
 - Specify what can it do on behalf of the tenant - 'Required scopes' (in our use case : 'hybris.email_view', 'hybris.velocitytemplate_admin')
 - Make sure that you define the redirect URI for the widget: `https://localhost:8081/builder/auth.html`. This endpoint is important for the widget to capture the access token that was granted for its authorization context.
 - Enable 'Use this Builder Module for my project' option do enable the widget in your project.


### Review the widget

Go to your project dashboard and see that your widget is now available in your project

<img style="width: 30%;" max-width: 326px src="img/addWidget.png" class="img-click-modal" alt="add widget"><br><br>

<img style="width: 30%;" max-width: 326px src="img/widget.png" class="img-click-modal" alt="widget"><br><br>

If you are happy with the result, its time to deploy the widget to a remote location.
Once you do it, go to the builder module definition and adjust the URLs so that they point to the remote location where you deployed the widget:

- `https://{baseUrl of your widget}/builder/auth.html`
- `https://{baseUrl of your widget}/builder/module.json`



