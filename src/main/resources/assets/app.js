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
                  var aConcat = a.name + a.packageVersion
                  var bConcat = b.name + b.packageVersion

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
                config.value = config["default"]
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
      controller: function($scope, $http){


            $scope.pageLink = function(){
               {
                 var properties = $scope.selectedAppProperties
                 var length = properties.length

                 var terminalProps = []

                 for(var i = 0; i < length; i++){
                   var prop = properties[i]

                   if(prop.variableType == "string" && prop.use) {
                     terminalProps.push(window.encodeURIComponent(prop.name + "/s") + "="  + window.encodeURIComponent(prop.value))
                   }
                  if(prop.variableType == "number" && prop.use) {
                    terminalProps.push(window.encodeURIComponent(prop.name + "/n") + "="  + window.encodeURIComponent(prop.value))
                   }

                   if(prop.variableType == "integer" && prop.use) {
                     terminalProps.push(window.encodeURIComponent(prop.name + "/i") + "="  + window.encodeURIComponent(prop.value))
                    }

                    if(prop.variableType == "boolean" && prop.use) {
                      terminalProps.push(window.encodeURIComponent(prop.name + "/b") + "="  + window.encodeURIComponent(prop.value))
                     }




                 }

                 var result = "api/generate?"
                 var queryParams = ""

                 for(var i=0; i<terminalProps.length; i++){

                    if(i>0){
                      queryParams=queryParams + "&"

                    }

                    queryParams = queryParams + terminalProps[i]

                 }

                 return result + queryParams


               }
            }

        }

      }
    });