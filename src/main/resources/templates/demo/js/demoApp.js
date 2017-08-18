var demoUIModuleApp = angular.module('demoUIModuleApp', ["builder", "builder.inputs", "builder_editors", "builder.translate", 'builder.navbar']);

demoUIModuleApp.controller('settingsCtrl', function ($scope, $http) {
    $scope.widgetSettings = Builder.currentWidget.settings;
    $scope.availableMarkets = Builder.currentWidget.settings.availableMarkets;
    $scope.availableRegions = Builder.currentWidget.settings.availableRegions;
    $scope.currLanguage = Builder.currentLanguage;
});

demoUIModuleApp.controller('authCtrl', function ($scope, $http) {
    $scope.accessToken = Builder.authManager().getAccessToken();
    $scope.currentAccountId = Builder.currentAccountId;
});

demoUIModuleApp.controller('linkManagerCtrl', function ($scope) {
    $scope.showProfile = function(){
        Builder.linkManager().path('/My Account/My Profile/Account Settings').open()
    };

    $scope.showWidgetTwo = function(){
        Builder.linkManager().path('/Home/'+Builder.currentOrganizationId+'/Projects/'+Builder.currentProjectId+'/parent/settingsExample1').open();
    }

    $scope.showProductOverview = function(){
        Builder.linkManager().path('/Product Overview')
    };
    $('#isPathExisting').prop('disabled', !Builder.linkManager().path('/Product Overview').exists());
});

demoUIModuleApp.controller('notificationCtrl', function ($scope, notificationManager) {
    $scope.notificationManager = notificationManager;

    $scope.generateNotification = function () {
        return { id: 'testNotification' + new Date().getTime(), title: 'Test', message: loremIpsum.split('. ')[Math.floor(Math.random() * 75)] + '.' };
    };
    $scope.getNotificationIdPattern = function () {
        return /testNotification.*/;
    };

    $scope.showSuccess = function(val){
        Builder.notificationManager.showSuccess(val);
    };

    $scope.showWarning = function(val){
        Builder.notificationManager.showWarning(val);
    }

    $scope.showError = function(val){
        Builder.notificationManager.showError(val);
    }

    $scope.showInfo = function(val){
        Builder.notificationManager.showInfo(val);
    }

    $scope.showConfirmation = function(value, value2, cb){
        Builder.notificationManager.showConfirmation(value,value2, cb);
    }

    $scope.confirmationCb = function(){
        alert('Great!');
    };

    $scope.processing = function () {
        Builder.notificationManager.pushProcessing();
        setTimeout(function () {
            Builder.notificationManager.popProcessing();
        }, 3000);
    };

    var loremIpsum = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc, quis gravida magna mi a libero. Fusce vulputate eleifend sapien. Vestibulum purus quam, scelerisque ut, mollis sed, nonummy id, metus. Nullam accumsan lorem in dui. Cras ultricies mi eu turpis hendrerit fringilla. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; In ac dui quis mi consectetuer lacinia. Nam pretium turpis et arcu. Duis arcu tortor, suscipit eget, imperdiet nec, imperdiet iaculis, ipsum. Sed aliquam ultrices mauris. Integer ante arcu, accumsan a, consectetuer eget, posuere ut, mauris. Praesent adipiscing. Phasellus ullamcorper ipsum rutrum nunc. Nunc nonummy metus. Vestibulum volutpat pretium libero. Cras id dui. Aenean ut eros et nisl sagittis vestibulum. Nullam nulla eros, ultricies sit amet, nonummy id, imperdiet feugiat, pede. Sed lectus. Donec mollis hendrerit risus. Phasellus nec sem in justo pellentesque facilisis. Etiam imperdiet imperdiet orci. Nunc nec neque. Phasellus leo dolor, tempus non, auctor et, hendrerit quis, nisi. Curabitur ligula sapien, tincidunt non, euismod vitae, posuere imperdiet, leo. Maecenas malesuada. Praesent congue erat at massa. Sed cursus turpis vitae tortor. Donec posuere vulputate arcu. Phasellus accumsan cursus velit. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Sed aliquam, nisi quis porttitor congue, elit erat euismod orci, ac placerat dolor lectus quis orci. Phasellus consectetuer vestibulum elit. Aenean tellus metus, bibendum sed, posuere ac, mattis non, nunc. Vestibulum fringilla pede sit amet augue. In turpis. Pellentesque posuere. Praesent turpis. Aenean posuere, tortor sed cursus feugiat, nunc augue blandit nunc, eu sollicitudin urna dolor sagittis lacus. Donec elit libero, sodales nec, volutpat a, suscipit non, turpis. Nullam sagittis. Suspendisse pulvinar, augue ac venenatis condimentum, sem libero volutpat nibh, nec pellentesque velit pede quis nunc. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Fusce id purus. Ut varius tincidunt libero. Phasellus dolor. Maecenas vestibulum mollis";
});

demoUIModuleApp.controller('localizedCtrl', function ($scope) {
    $scope.configuration = {
        'en': { 'default': true, 'required': true },
        'de': { 'default': false, 'required': false }
    };
    $scope.localizedData = {
        'en': 'english text',
        'de': 'german text'
    };
});

demoUIModuleApp.controller('validationCtrl', ['$scope', 'inputValidator', '$timeout', '$translate',
    function ($scope, inputValidator, $timeout, $translate) {
}]);

demoUIModuleApp.controller('fullScreenCtrl', function ($scope) {
    $scope.fullScreenMode = function(){
        Builder.moduleFullScreen.turnOn();
    }
});

demoUIModuleApp.controller('virtualChildrenCtrl', function($scope){
    $scope.showVirtualChildView = function(){
        Builder.linkManager().currentLocation().path('/virtualChild').open();
    };
});

demoUIModuleApp.controller('dynamicNodeCtrl', function($scope){
    $scope.dynamicValue = undefined;
    $scope.showDynamicNodeView = function(){
        Builder.linkManager().currentLocation().path($scope.dynamicValue).open();
    }
    $scope.somevalue = Builder.currentWidget.settings.dynamicNodeExampleValue;
});

Builder.initialize(function(){
    $("#header").append("Welcome " + Builder.currentAccountId + "!");
    $("#location").append(Builder.currentLocation);
    $("#token").append(Builder.authManager);

    if(Builder.currentWidget.settings.example_setting==='widget#2'){
        $("#Widget1").show();
        $("#Widget2").hide();
    }else{
        $("#Widget1").hide();
        $("#Widget2").show();
    }
});