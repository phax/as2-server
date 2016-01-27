#as2-server

[![Build Status](https://travis-ci.org/phax/as2-server.svg?branch=master)](https://travis-ci.org/phax/as2-server)
﻿
[![Join the chat at https://gitter.im/phax/as2-server](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/phax/as2-server?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

The standalone AS2 server component based on **[as2-lib](https://github.com/phax/as2-lib)**.
It directly works on Sockets and not on Servlets. Please see **[as2-peppol-servlet](https://github.com/phax/as2-peppol-servlet)** for an example how **[as2-lib](https://github.com/phax/as2-lib)** can be used together with the Servlet specs.
Alternatively a specialized Servlet based server for PEPPOL is available with my **[as2-peppol-server](https://github.com/phax/as2-peppol-server)** project.

Versions <= 1.0.1 are compatible with ph-commons < 6.0.
Versions >= 2.0.0 are compatible with ph-commons >= 6.0.

This project is licensed under the FreeBSD License.

#New and noteworthy

  * Version 2.2.5 - 2015-12-01
    * Updated to as2-lib 2.2.5
  * Version 2.2.4 - 2015-11-11
    * Updated to as2-lib 2.2.4
  * Version 2.2.3 - 2015-10-22
    * Updated to as2-lib 2.2.3
  * Version 2.2.2 - 2015-10-19
    * Updated to as2-lib 2.2.2 and Bouncy Castle 1.53
  * Version 2.2.1 - 2015-10-08
    * Updated to as2-lib 2.2.1
  * Version 2.2.0
    * Extended the configuration file with the attribute `CryptoVerifyUseCertificateInBodyPart` to define whether a certificate passed in the signed MIME body part shall be used to verify the signature (when `true`) or whether to always use the certificate provided in the partnership (when `false`). By default the value is `true`.
    * Extended the configuration file with the attribute `CryptoSignIncludeCertificateInBodyPart` to define whether the certificate used for signing should be included in the signed MIME body part (when `true`) or not to include it (when `false`). By default the value is `true`. This is the sending counter part of `CryptoVerifyUseCertificateInBodyPart`

#Maven usage
Add the following to your pom.xml to use this artifact:
```
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>as2-server</artifactId>
  <version>2.2.5</version>
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

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodeingStyleguide.md) |
On Twitter: <a href="https://twitter.com/philiphelger">@philiphelger</a>
