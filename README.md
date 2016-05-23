#DCOS CONFIG TOOL#

This project provides a basic interface to browse the Mesosphere Universe repository and add properties and values to
generate a configuration file.

##Starting the application

Start the application with docker:

`docker run -p 8080:8080 -d ftrossbach/dcos-config-reader`



##Using the application

* Visit http://<localhost or ip of docker-machine>:8080/index.html and see the list of packages

![The entry site](https://raw.githubusercontent.com/ftrossbach/dcos-config-tool/master/images/repo.png)

* Select an application version

![The entry site](https://raw.githubusercontent.com/ftrossbach/dcos-config-tool/master/images/selectedApplication.png)

* Select a property and provide a value

![The entry site](https://raw.githubusercontent.com/ftrossbach/dcos-config-tool/master/images/selectValue.png)

* Click the link to generate config

![The entry site](https://raw.githubusercontent.com/ftrossbach/dcos-config-tool/master/images/generateConfig.png)

##To Do

This is still very rough with hardly any error handling. Array properties and required parameters are also not handled yet.
But it should work for a basic use case.

##License
The project is licensed under the Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0)


