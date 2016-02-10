var defaultListApp = angular.module('defaultListApp', ["builder", "builder_editors","Restangular"]);
defaultListApp.controller('defaultListCtrl', ['$scope', 'Restangular',function($scope,Restangular){

    Restangular.setBaseUrl('${{SERVICE_URL}}');
    Restangular.stripResponse(response);

    $scope.items = ['item1','item2','item3','item4'];
}]);