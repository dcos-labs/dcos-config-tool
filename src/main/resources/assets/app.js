var module = angular.module('docsSimpleDirective', [])


module.controller('Controller', ['$scope', '$http', function($scope, $http) {
   $scope.applicationList = []

   $scope.appSelected = false
   $scope.selectedAppVersion = {}
   $scope.selectedAppProperties = []


    $http.get('/api/repository')
          .then(function(response){

               $scope.applicationList = []

               var repositories = response.data.repositories

               for(repository in repositories){
                   var applications = repositories[repository].applications
                  for(appName in applications){
                     var app = applications[appName]

                     for(version in app.versions){

                       $scope.applicationList.push(app.versions[version])
                     }
                  }
               }

               $scope.applicationList.sort(function(a,b){
                  var aConcat = a.repository + a.version + a.name
                  var bConcat = b.repository + b.version + b.name

                  return aConcat.localeCompare(bConcat)

               })


            },
          function(){})
}]);
module.directive('applicationList', function() {
  return {
    templateUrl: 'application-list.html',
    controller: function($scope){

       var flattenProperties = function(config, acc){

         acc.push(config)

         if(config.variableType === "object"){

           for(var i=0; i < config.children.length; i++) {

             flattenProperties(config.children[i], acc)
           }

         } else if(config.variableType === "array"){
            //TODO handle array
         } else if(config.variableType === "string"){

            config.value = config["default"] ? config["default"] : ""

         } else if(config.variableType === "string"){

            config.value = config["default"] ? config["default"] : ""

         } else if(config.variableType === "number" || config.variableType === "integer"){

            if(config["default"]){
               config.value = Number(config["default"])
            }
         } else if(config.variableType === "boolean"){

             if(config["default"]){
                config.value = (config["default"] === "true")
             }

           }
        }

       $scope.select = function(model) {

         $scope.appSelected = true

         $scope.selectedAppVersion = model

         $scope.selectedAppProperties = []

         flattenProperties(model.config, $scope.selectedAppProperties)


       }


      }

    }
  });

module.directive('applicationDetails', function() {
    return {
      templateUrl: 'application-details.html',
      controller: function($scope){




        }

      }
    });