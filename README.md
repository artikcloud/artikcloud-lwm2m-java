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


Usage
------

Before using the LWM2M SDK to managing your devices, you need to first enable device management on your Device Type. <INSERT LINK TO DOCUMENTATION HERE>.

After a device has been created in Artik Cloud, you need to create the device token and use the deviceId and deviceToken to instantiate the LWM2M Client as below:

~~~
        Device device = new Device("<Manufacturer>", "<ModelNumber>", "<SerialNumber>", SupportedBinding.UDP) {
            
            @Override
            public ExecuteResponse executeReboot() {
                System.out.println("executeReboot called");
                return ExecuteResponse.success();
            }
            
            @Override
            public ExecuteResponse executeFactoryReset() {
                System.out.println("executeFactoryReset called");
                return ExecuteResponse.success();
            }
            
            @Override
            protected ExecuteResponse executeResetErrorCode() {
                System.out.println("executeResetErrorCode called");
                return super.executeResetErrorCode();
            }
        };
        ArtikCloudClient client = new ArtikCloudClient("<DeviceID>", "<DeviceToken>", device);
~~~

You can use a binding mode of UDP or TCP while registering the device.

If using TCP binding mode you will need to additionally add Artik Cloud intermediate certs to your Java system CACert.
These certificates can be downloaded in PEM format from the following links:

VeriSign Class 3 Public Primary Certification Authority - G5: [pca3-g5ss.crt](http://www.tbs-internet.com/verisign/pca3-g5ss.crt)

Symantec Class 3 ECC 256 bit SSL CA - G2: [rc.crt](http://symantec.tbs-certificats.com/rc.crt)

Use the following commands to get and load the PEMs into the Java system CACert:

~~~
$
$ # Download certs
$ wget http://www.tbs-internet.com/verisign/pca3-g5ss.crt
$ wget http://symantec.tbs-certificats.com/rc.crt
$
$ # Load certs
$ keytool -importcert -alias "verisignclass3ppg5ca" -file pca3-g5ss.crt -keystore cacerts -storepass changeit -trustcacerts -noprompt
$ keytool -importcert -alias "verisignclass3eccg2ca" -file rc.crt -keystore cacerts -storepass changeit -trustcacerts -noprompt
$
~~~

To support FirmwareUpdates, you need to subclass FirmwareUpdate to provide concrete implementation of the downloadPackage and updateFirmware execute methods:
~~~

        // FirmwareUpdate
        FirmwareUpdate dummyUpdater = new FirmwareUpdate() {
            
            @Override
            public FirmwareUpdateResult downloadPackage(String packageUri) {
                System.out.println("download package: " + packageUri);
                this.setPkgName("<PKG_NAME>", true);
                this.setPkgVersion("<PKG_VERSION>", true);
                return FirmwareUpdateResult.SUCCESS;
            }
            
            @Override
            public FirmwareUpdateResult executeUpdateFirmware() {
                System.out.println("update firmware");
                return FirmwareUpdateResult.SUCCESS;
            }
        };
        client.setFirmwareUpdate(dummyUpdater);
~~~

Finally, you need to start the registration process. 

~~~
        // Register
        client.start();
        
        // Sleep for 10 seconds for the registration to complete
        Thread.sleep(60000);
        
        // De-Register
        client.stop(true);
        // Finish
        client.close();
~~~

Peek into [tests](https://github.com/artikcloud/artikcloud-lwm2m-java/tree/master/src/test/java/cloud/artik/lwm2m) for examples about how to use the SDK.

More about ARTIK Cloud
----------------------

If you are not familiar with ARTIK Cloud, we have extensive documentation at https://developer.artik.cloud/documentation

The full ARTIK Cloud API specification can be found at https://developer.artik.cloud/documentation/api-reference/

Check out advanced sample applications at https://developer.artik.cloud/documentation/samples/

To create and manage your services and devices on ARTIK Cloud, create an account at https://developer.artik.cloud

Also see the ARTIK Cloud blog for tutorials, updates, and more: http://artik.io/blog/cloud

License and Copyright
---------------------

Licensed under the Apache License. See [LICENSE](LICENSE).

Copyright (c) 2016 Samsung Electronics Co., Ltd.
