# as2-server

[![Build Status](https://travis-ci.org/phax/as2-server.svg?branch=master)](https://travis-ci.org/phax/as2-server)
﻿
[![Join the chat at https://gitter.im/phax/as2-server](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/phax/as2-server?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

The standalone AS2 server component based on **[as2-lib](https://github.com/phax/as2-lib)**.
It directly works on Sockets and not on Servlets. Please see **[as2-peppol-servlet](https://github.com/phax/as2-peppol-servlet)** for an example how **[as2-lib](https://github.com/phax/as2-lib)** can be used together with the Servlet specs.
Alternatively a specialized Servlet based server for PEPPOL is available with my **[as2-peppol-server](https://github.com/phax/as2-peppol-server)** project.

This project is licensed under the FreeBSD License.

# News and noteworthy

* v4.4.0 - 2019-06-14
    * Updated to as2-lib 4.4.0
* v4.3.0 - 2019-05-17
    * Updated to as2-lib 4.3.0
    * Moved interface `IRefreshablePartnershipFactory` to project as2-lib
    * New class `ServerSelfFillingXMLPartnershipFactory` handles dynamic partnerships based on `SelfFillingXMLPartnershipFactory`. See [as2-lib #63](https://github.com/phax/as2-lib/issues/63) and [as2-lib #79](https://github.com/phax/as2-lib/issues/79) 
* v4.2.2 - 2019-03-21
    * Updated to as2-lib 4.2.2
* v4.2.1 - 2018-11-22
    * Updated to as2-lib 4.2.1
* v4.2.0 - 2018-11-21
    * Updated to as2-lib 4.2.0
* v4.1.1 - 2018-07-27
    * Updated to as2-lib 4.1.1
* v4.1.0 - 2018-06-20
    * Updated to as2-lib 4.1.0
* v4.0.2 - 2018-04-05
    * Updated to as2-lib 4.0.2
* v4.0.1 - 2018-03-27
    * Updated to as2-lib 4.0.1
    * Fixes issue #20
* v4.0.0 - 2018-03-22
    * Updated to as2-lib 4.0.0
    * Anonymous TLS cipher suite determination improved
    * Changed internally from `java.util.Date` to `java.time.LocalDateTime` - so all the Date parameters must be changed from `yyyy` to `uuuu`!!! 
    * The certificate factory `com.helger.as2.app.cert.ServerPKCS12CertificateFactory` was deprecated in favor of the more generic `com.helger.as2.app.cert.ServerCertificateFactory` that handles arbitrary keystore types (like JKS).
* v3.1.0 - 2017-07-27
    * Updated to as2-lib 3.1.0
* v3.0.4 - 2017-06-19
    * Updated to as2-lib 3.0.4
* v3.0.3 - 2017-01-24
    * Updated to as2-lib 3.0.3
* v3.0.2 - 2016-12-12
    * Updated to as2-lib 3.0.2
* v3.0.1 - 2016-09-27
    * Updated to as2-lib 3.0.1
* v3.0.0 - 2016-08-21
    * Requires JDK 8
    * Updated to as2-lib 3.0.0
* v2.2.7 - 2016-04-27
    * Updated to as2-lib 2.2.7
* v2.2.5 - 2015-12-01
    * Updated to as2-lib 2.2.5
* v2.2.4 - 2015-11-11
    * Updated to as2-lib 2.2.4
* v2.2.3 - 2015-10-22
    * Updated to as2-lib 2.2.3
* v2.2.2 - 2015-10-19
    * Updated to as2-lib 2.2.2 and Bouncy Castle 1.53
* v2.2.1 - 2015-10-08
    * Updated to as2-lib 2.2.1
* v2.2.0
    * Extended the configuration file with the attribute `CryptoVerifyUseCertificateInBodyPart` to define whether a certificate passed in the signed MIME body part shall be used to verify the signature (when `true`) or whether to always use the certificate provided in the partnership (when `false`). By default the value is `true`.
    * Extended the configuration file with the attribute `CryptoSignIncludeCertificateInBodyPart` to define whether the certificate used for signing should be included in the signed MIME body part (when `true`) or not to include it (when `false`). By default the value is `true`. This is the sending counter part of `CryptoVerifyUseCertificateInBodyPart`

# Maven usage

Add the following to your pom.xml to use this artifact:

```xml
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>as2-server</artifactId>
  <version>4.4.0</version>
</dependency>
```

# Configuration
Start server: run class `com.helger.as2.app.MainOpenAS2Server`

Startup arguments (required): `src/main/resources/config/config.xml`
This configuration file should be the starting point for your own customizations. You may simple copy the file to a different location and provide the absolute path to it instead of the example given above. 

Waits for incoming AS2 messages on `http://localhost:10080/HttpReceiver`
Note: the port for the incoming messages can be configured in the configuration file.

Than run `com.helger.as2.test.TestClient` as a Java main application to perform a simple AS2 transmission.

No database or additional software is needed to exchange AS2 messages!

# Building and running from source
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

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodingStyleguide.md) |
On Twitter: <a href="https://twitter.com/philiphelger">@philiphelger</a>
