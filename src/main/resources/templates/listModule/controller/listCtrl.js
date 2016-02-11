var defaultListApp = angular.module('defaultListApp', ['builder', 'builder_editors']);
defaultListApp.controller('defaultListCtrl', ['$scope','Restangular', function($scope, Restangular){

    /*Restangular.setBaseUrl('YOUR_SERVICE_URL');
     Restangular.stripRestangular(response);
     var headers={
     'Authorization':'Bearer'+Builder.authManager().
     }
     $scope.items = Restangular.one('ENDPOINT').customGET("", headers);*/

    $scope.items = ['item1','item2','item3','item4'];
}]);