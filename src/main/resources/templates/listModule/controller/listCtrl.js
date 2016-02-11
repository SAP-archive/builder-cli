var defaultListApp = angular.module('defaultListApp', ['builder', 'builder_editors']);
defaultListApp.controller('defaultListCtrl', ['$scope','Restangular', function($scope, Restangular){

    $scope.items = ['item1','item2','item3','item4'];

    Restangular.setBaseUrl('{{YOUR_SERVICE_URL}}');
    Restangular.one('{{YOUR_ENDPOINT}}').getList().then(function(response){
        $scope.items = response;
    });

}]);
