#as2-server

The standalone AS2 server component based on **[as2-lib](https://github.com/phax/as2-lib)**.
It directly works on Sockets and not on Servlets. Please see **[as2-peppol-servlet](https://github.com/phax/as2-peppol-servlet)** for an example how **[as2-lib](https://github.com/phax/as2-lib)** can be used together with the Servlet specs.
Alternatively a specialized Servlet based server for PEPPOL is available with my **[as2-peppol-server](https://github.com/phax/as2-peppol-server)** project.

Versions <= 1.0.1 are compatible with ph-commons < 6.0.
Versions >= 2.0.0 are compatible with ph-commons >= 6.0.

This project is licensed under the FreeBSD License.

#New and noteworthy

  * Version 2.2.0
    * Extended the configuration file with the attribute "CryptoVerifyUseCertificateInBodyPart"

#Maven usage
Add the following to your pom.xml to use this artifact:
```
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>as2-server</artifactId>
  <version>2.1.0</version>
</dependency>
```

#Configuration
Start server: run class `com.helger.as2.app.MainOpenAS2Server`

Startup arguments (required): `src/main/resources/config/config.xml`
This configuration file should be the starting point for your own customizations. You may simple copy the file to a different location and provide the absolute path to it instead of the example given above. 

Waits for incoming AS2 messages on `http://localhost:10080/HttpReceiver`
Note: the port for the incoming messages can be configured in the configuration file.

Than run `com.helger.as2.test.TestClient` as a Java main application to perform a simple AS2 transmission.

No database or additional software is needed to exchange AS2 messages!

#Building and running from source
To run this server stand-alone from the source build, perform the following steps.
In the below commands `x.y.z` denotes the effective version number

1. build the binary artefacts using Apache Maven 3.x: `mvn clean install -Pwithdep` (selects the profile "withdep" which means "with dependencies"). On Windows you may run `build.cmd` as an alternative.
  1. If this fails than potentially because a SNAPSHOT version of `as2-lib` is referenced. In that case check out the [as2-lib](https://github.com/phax/as2-lib/) project as well, run `mvn clean install` on as2-lib and go back to the first step on this project. 
2. The resulting JAR file is than located at `standalone/as2-server.jar`
3. Launch the server (note: `src/main/resources/config/config.xml` is the path to the configuration file to be used and may be changed): 
  1. On Unix/Linux systems run the AS2 server using the following command (on one line):
  
     `java -cp "standalone/*" com.helger.as2.app.MainOpenAS2Server standalone/config/config.xml`
`
  2. On Windows systems run the AS2 server using the following command (on one line) or execute the `run.cmd` file:
  
     `"%JAVA_HOME%\bin\java" -cp "standalone/*" com.helger.as2.app.MainOpenAS2Server standalone/config/config.xml`

---

On Twitter: <a href="https://twitter.com/philiphelger">Follow @philiphelger</a>
