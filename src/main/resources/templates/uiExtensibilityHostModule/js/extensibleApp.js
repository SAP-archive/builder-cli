var extensibleApp = angular.module('extensibleApp', ["builder"]);


extensibleApp.controller('ExtensibleController', ["$scope","Restangular", "currentProjectId",function($scope, Restangular, currentProjectId) {
    $scope.widgetSettings = Builder.currentWidget.settings;
    $scope.token = Builder.authManager().getAccessToken();
    $scope.scope = decodeURIComponent(Builder.authManager().getScope());

    $scope.project = {};

    $scope.slot1pluginId = "-";

    BuilderExtensible.injectExtensions();

    Restangular.setBaseUrl('https://api.yaas.io/hybris/account/v1');

    Restangular.one("projects/" + encodeURIComponent(currentProjectId)).get($scope.project).then(function(data){
        $scope.currentProjectDescription = data.description;
    });

    BuilderExtensible.on("slot1", function(data) {
        $scope.slot1pluginId = data;
        $scope.$apply();
    });

    BuilderExtensible.on("projectDescriptionChanged", function(data) {
        $scope.currentProjectDescription = data;
        $scope.$apply();
    });

    $scope.updateProject = function(){
        Restangular
            .one("projects/" + encodeURIComponent(currentProjectId)).customPUT($scope.project, "", {}, {});
    };
}]);