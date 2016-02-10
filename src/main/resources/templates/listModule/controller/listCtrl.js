var defaultListApp = angular.module('defaultListApp', ["builder", "builder_editors"]);
defaultListApp.controller('defaultListCtrl', function($scope){
    $scope.items = ['item1','item2','item3','item4'];
});