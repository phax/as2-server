#as2-server

The standalone AS2 server component based on **[as2-lib](https://github.com/phax/as2-lib)**.
It directly works on Sockets and not on Servlets. Please see **[as2-peppol-servlet](https://github.com/phax/as2-peppol-servlet)** for an example how **[as2-lib](https://github.com/phax/as2-lib)** can be used together with the Servlet specs.
Alternatively a specialized Servlet based server for PEPPOL is available with my **[as2-peppol-server](https://github.com/phax/as2-peppol-server)** project.

Versions <= 1.0.1 are compatible with ph-commons < 6.0.
Versions >= 2.0.0 are compatible with ph-commons >= 6.0.

This project is licensed under the FreeBSD License.

#Configuration
Start server: run class `com.helger.as2.app.MainOpenAS2Server`

Startup arguments (required): src/main/resources/config/config.xml

Waits for incoming AS2 messages on http://localhost:10080/HttpReceiver

Than run `com.helger.as2.test.TestClient` as a Java main application to perform a simple AS2 transmission.

No database or additional software is needed to exchange AS2 messages!

---

On Twitter: <a href="https://twitter.com/philiphelger">Follow @philiphelger</a>
