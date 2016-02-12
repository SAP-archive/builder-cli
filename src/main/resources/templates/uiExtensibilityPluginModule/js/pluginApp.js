var pluginApp = angular.module('pluginApp', ['builderPlugin','restangular']);


pluginApp.controller('PluginController', ["$scope", "Restangular", function($scope, Restangular) {

    $scope.project = {};

    BuilderPlugin.ready(function() {
        $scope.token = BuilderPlugin.authorizationData.accessToken;
        $scope.scope = decodeURIComponent(BuilderPlugin.authorizationData.scope);
        $scope.tenantId = BuilderPlugin.authorizationData.tenantId;
        $scope.clientId = BuilderPlugin.authorizationData.clientId;

        BuilderPlugin.sendEvent('slot1', BuilderPlugin.authorizationData.pluginId);
        $scope.$apply();
    });


    $scope.updateProject = function(){
       /* Restangular
            .setErrorInterceptor(function (response) {
                if (response.status == 403) {
                    alert("FORBIDDEN");
                }
            })
            .setBaseUrl('PATH_TO_YOUR_SERVICE_URL')
            .one(PATH_TO_YOUR_ENDPOINT).customPUT($scope.project, "", {}, {}).then(function() {
                BuilderPlugin.sendEvent('projectDescriptionChanged', $scope.project.description);
            });*/
    };
}]);