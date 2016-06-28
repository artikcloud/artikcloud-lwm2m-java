# ARTIK Cloud Java SDK for LWM2M
================
This SDK helps you connect your Device to ARTIK Cloud using LWM2M. 

Prerequisites
-------------

 * [Maven](http://maven.apache.org/) or [Maven Integration for Eclipse](https://www.eclipse.org/m2e/)
 * JavaSE 1.7 or above 

Installation
---------------------

You can generate the SDK libraries using one of the following ways. 

If using Maven command line,
- run "mvn clean install -DskipTests" in the root directory of the repository to install to your local Maven repository.

If using Eclipse, 
- import the SDK library project as "Existing Maven Projects".
- right click the project, and choose "Run As" then "Maven install"

- To use them in your Maven project, modify `pom.xml` file in your project to add dependency to sami-android-2.x.x.jar under `target` of the imported Maven project as following

~~~
<dependency>
    <groupId>cloud.artik</groupId>
    <artifactId>artikcloud-java</artifactId>
    <version>2.0.0</version>
    <scope>compile</scope>
</dependency>
~~~


Usage
------

Peek into [tests](https://github.com/artikcloud/artikcloud-java-lwm2m/tree/master/src/test/java/cloud/artik) for examples about how to use the SDK.

More about ARTIK Cloud
----------------------

If you are not familiar with ARTIK Cloud, we have extensive documentation at https://developer.artik.cloud/documentation

The full ARTIK Cloud API specification can be found at https://developer.artik.cloud/documentation/api-reference/

Check out advanced sample applications at https://developer.artik.cloud/documentation/samples/

To create and manage your services and devices on ARTIK Cloud, create an account at https://developer.artik.cloud

Also see the ARTIK Cloud blog for tutorials, updates, and more: http://artik.io/blog/cloud

License and Copyright
---------------------

Licensed under the Apache License. See [LICENSE](https://github.com/artikcloud/artikcloud-java/blob/master/LICENSE).

Copyright (c) 2016 Samsung Electronics Co., Ltd.
