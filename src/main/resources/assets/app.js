var module = angular.module('docsSimpleDirective', [])


module.controller('Controller', ['$scope', function($scope) {
  $scope.customer = {
    name: 'Naomi',
    address: '1600 Amphitheatre'
  };
}])
.directive('myCustomer', function($http) {
  return {
    template: 'Name: {{customer.name}} Address: {{customer.address}}',
    controller: function($scope){

       $scope.result = {}

       $http.get('/api/repository')
       .then(function(response){
            console.log(response)

         },
       function(){})}

    }
  }
);