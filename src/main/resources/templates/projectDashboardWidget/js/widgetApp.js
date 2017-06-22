angular.module('widgetApp', ['builderPlugin']).
    controller('WidgetCtrl', ['$scope','Restangular',function($scope, Restangular){
        $scope.numberToDisplay;
        BuilderPlugin.ready(function() {
            Restangular
                .setBaseUrl(BuilderPlugin.settings.serviceBasePath)
                .withConfig(function(RestangularConfigurer) {
                    RestangularConfigurer.setFullResponse(true);
                })
                .one(BuilderPlugin.settings.countableEndpoint+"?pageNumber=1&pageSize=1&totalCount=true")
                .getList()
                .then(function(response){
                    $("#spinner").remove();
                    $scope.numberToDisplay = response.headers("Hybris-Count");
                },function(error){
                    //handle error
                }); 
       });

       $scope.goToSomewhere = function(){
           var link = window.parent.Builder.linkManager().currentProject();
           link.path(BuilderPlugin.settings.bottomLink);
           link.open(true);
       }
    }]);