angular.module('widgetApp', ['builderPlugin'])
    .controller('WidgetCtrl', ['$scope','Restangular',function($scope, Restangular){
        //$scope.totalCount;
        //$scope.data;
        BuilderPlugin.ready(function() {
            $scope.currentProjectId = BuilderPlugin.authorizationData.tenantId
            Restangular
                .setBaseUrl(BuilderPlugin.settings.serviceBasePath)
                .withConfig(function(RestangularConfigurer) {
                    RestangularConfigurer.setFullResponse(true);
                })
                .one("{{YOUR_ENDPOINT}}") // append &totalCount=true if you need totalCount in the response
                .getList() // or get() if calling a single element resource
                .then(function(response){
                    $("#spinner").remove();
                    //$scope.data = Restangular.stripRestangular(response.data);
                    //$scope.totalCount = response.headers("Hybris-Count");
                },function(error){
                    $("#spinner").remove();
                    //handle error
                }); 
       });
    }]);