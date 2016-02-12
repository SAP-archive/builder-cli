var extensibleApp = angular.module('extensibleApp', ["builder"]);


extensibleApp.controller('ExtensibleController', ["$scope","Restangular", "currentProjectId",function($scope, Restangular, currentProjectId) {
    $scope.widgetSettings = Builder.currentWidget.settings;
    $scope.token = Builder.authManager().getAccessToken();
    $scope.scope = decodeURIComponent(Builder.authManager().getScope());

    $scope.project = {};

    $scope.slot1pluginId = "-";

    BuilderExtensible.injectExtensions();


    BuilderExtensible.on("slot1", function(data) {
        $scope.slot1pluginId = data;
        $scope.$apply();
    });

    BuilderExtensible.on("projectDescriptionChanged", function(data) {
        $scope.currentProjectDescription = data;
        $scope.$apply();
    });

}]);