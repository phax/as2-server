package com.helger.as2.test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.helger.commons.charset.CCharset;
import com.helger.commons.io.file.FileHelper;
import com.helger.commons.io.stream.StreamHelper;

/**
 * Small main application that writes a fully fledged AS2 message via a socket
 * to the server.
 *
 * @author Philip Helger
 */
public final class MainStreamToAS2Server
{
  public static void streamCompleteHttpRequestToServer (final byte [] aPayload) throws UnknownHostException, IOException
  {
    final Socket aSocket = new Socket ("localhost", 10080);
    try
    {
      // Write request
      final OutputStream out = aSocket.getOutputStream ();
      out.write (aPayload);
      out.flush ();
      System.out.println ("Streamed payload to AS2 server");

      // Get response
      final byte [] aResponse = StreamHelper.getAllBytes (aSocket.getInputStream ());
      System.out.println ("Response:\n\n" + new String (aResponse, CCharset.CHARSET_ISO_8859_1_OBJ));
    }
    finally
    {
      aSocket.close ();
    }
  }

  public static void main (final String [] args) throws UnknownHostException, IOException
  {
    final File aFile = new File ("src/test/resources/test-messages/issue12-1.txt");
    byte [] aPayload = StreamHelper.getAllBytes (FileHelper.getInputStream (aFile));

    if (true)
    {
      // Build as String to easily handle different line ending
      final String sMsg = "AS2-To: OpenAS2A\r\n" +
                          "AS2-From: OpenAS2B\r\n" +
                          "AS2-Version: 1.2\r\n" +
                          "EDIINT-Features: multiple-attachments, AS2-Reliability\r\n" +
                          "Date: Tue, 20 Oct 2015 13:27:21 GMT\r\n" +
                          "Message-Id: <0dbb26a8046f4887_-14540955_14ebb365888_-7e99@0dbb26a8046f4887_-14540955_14ebb365888_-7e98>\r\n" +
                          "Disposition-Notification-To: testas2@freeas2.com\r\n" +
                          "Disposition-Notification-Options: signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1\r\n" +
                          "MIME-Version: 1.0\r\n" +
                          "Content-Type: multipart/signed; protocol=\"application/pkcs7-signature\"; micalg=sha1; boundary=\"boundaryEHSgAQ==\"\r\n" +
                          "Content-Length: 4273\r\n" +
                          "\r\n" +
                          "--boundaryEHSgAQ==\r\n" +
                          "Content-Type: application/edi-x12\r\n" +
                          "Content-Disposition: Attachment; filename=\"test_data_2.edi\"\r\n" +
                          "\r\n" +
                          "ISA*00*ssssssssss*00*rrrrrrrrrr*ZZ*testas2     *zz*testas2      *961007*2013*U*00200*754320000*0*T*:\n" +
                          "GS*PO*S1S1S1S1S1S1S1S*R1R1R1R1R1R1R1R*961007*2013*000000004*X*003050\n" +
                          "ST*850*000040001\n" +
                          "BEG*00*BE*2a*43324234v5523*961007*23tc4vy24v2h3vh3vh*ZZ*IEL*09*RE*09\n" +
                          "CUR*11*TRN*5656*65*566*IMF*006*961007\n" +
                          "REF*6A*433r1c3r34r34c3312qctgc54*Reference Number\n" +
                          "PER*AA*Hans Gutten*CP*1.322.323.4444*****rgg4egv4t4\n" +
                          "TAX*4tgtbt4tr4tr*GL*ghgh*********G*C\n" +
                          "FOB*TP*CA*USA*02*DOM*CC*Regular Locations per Terms\n" +
                          "CTP*DE*C04*453*25000*D9*SEL*23214*23432423423*ES*42243423\n" +
                          "SAC*A*B000*AE*3545*3442300\n" +
                          "CUR*11*767*7767*65\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "PO1*111-aaa*1000000*AS*90.00*BD*AK*234235v3534q6f3534v4353453vq3q32***********A1*ytrh\n" +
                          "CTT*1\n" +
                          "SE*32*000040001\n" +
                          "GE*1*000000004\n" +
                          "IEA*1*000000001\n" +
                          "\n" +
                          "--boundaryEHSgAQ==\r\n" +
                          "Content-Type: application/pkcs7-signature;      name=\"smime.p7s\"\r\n" +
                          "Content-Disposition: attachment; filename=\"smime.p7s\"\r\n" +
                          "Content-Transfer-Encoding: base64\r\n" +
                          "\r\n" +
                          "MIIEowYJKoZIhvcNAQcCoIIElDCCBJACAQExCzAJBgUrDgMCGgUAMAsGCSqGSIb3DQEHAaCCAr0wggK5MIIBoQIEVAgwHjANBgkqhkiG9w0BAQUFADAgMQswCQYDVQQGEwJBVDERMA8GA1UEAwwIT3BlbkFTMkIwIBcNMTQwOTA0MDkyNTUwWhgPMjExNDA4MTEwOTI1NTBaMCAxCzAJBgNVBAYTAkFUMREwDwYDVQQDDAhPcGVuQVMyQjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJE+izonf1SVOday/KqIECp0Vq+eHB1MLQ9VzylQXSpfBRQz1425TzDIHAG94NqywsBg3J31DJJ32Ve7F4ps2kRQIlTjPt340QMR9OJiAdsdHdPH1i9brns354f775ul1gF4ikWpoYmjwR5tnintH8NFr/v3UO/phQR4X3GxJwmNV50H6VbbQerreW3yBYFQ/unNkFgjam4UustHnaGW2RFZXIGd9+14ivUJZTjagcC1b/hVI/hbaz8Ikd/u5ifsRl47jaoQN590epAh2SWyx0nfCUuLLaHBpNw/1Hbug5O6o2ytxeKbE0ySa6wDjabgjz5j5bLME79eYDNzVAE7qJsCAwEAATANBgkqhkiG9w0BAQUFAAOCAQEAdwN9YIO7AaKkcaS63Iiatl+kr3alV7AIRPFzTZCUU0S37XSNLEvOJRvUfKWp6GcCYjRHoTVvXdtvlQJR2pWeVYmimI1opG7eJRmoNM/M8jeeax0KaQQdszcqx7gNyFXEdTpWpxihJSI7cJ5g9VjsX4VsENn9C/B4UJozP+Y2296HAOY+Z4ryd7p73fKij7vcUsTAcu1zhQI/2ZcQTQQjR7y3ZgyM6fO3bkyujf8Ngruco9j9lCjPTak3tUG+jw+UY8suDtCjXYR7hWX1hv2mE941vcNdD6gpcH/pvbcE9G8DfLtUqTirnuJ6pCWr7p0DTckHw5GQ8E4NRM7/MC0D7jGCAa4wggGqAgEBMCgwIDELMAkGA1UEBhMCQVQxETAPBgNVBAMMCE9wZW5BUzJCAgRUCDAeMAkGBSsOAwIaBQCgXTAYBgkqhkiG9w0BCQMxCwYJKoZIhvcNAQcBMBwGCSqGSIb3DQEJBTEPFw0xNTEwMjAxMzI3MjFaMCMGCSqGSIb3DQEJBDEWBBRq3/2/+PQG2HopME3GRSa/XEeE6jANBgkqhkiG9w0BAQEFAASCAQBw7rsjUkahafIBNMEFZBnaO2Nzj1Ybr5e/5RmgdGv32/rgI81QN5GIAnijCEzqSGpDkfK5QLfVWC5wjQISuaOrEsJAM3x2av/9VjzE4ZLQFzvD9yHfafPwulbIkn9QyTROjVhgr574ETg+ZQ2FEB+VN2N4NJMNeNJxCpBoCTBq2alt8kS9sHoYq3vH4ic96B3VhgZmsV+YbSx3ItTZoW2fNUSspcufIJdSfSdGX1TkSakuVIcsT7Y9a7WlJpZ3PL77ZIJ8ow1lQLVVl8WqP1WeoVSpyQ/iAC1HOHF9tvFZQyEoRpuRo7ONizQ25+nDLvjFwXLNMl+IDP5H3Q5Plzjc\r\n" +
                          "\r\n" +
                          "--boundaryEHSgAQ==--\r\n";
      // Check content length
      if (false)
        System.out.println (sMsg.substring (sMsg.indexOf ("--boundaryEHSgAQ==")).length ());
      aPayload = sMsg.getBytes (CCharset.CHARSET_ISO_8859_1_OBJ);
    }

    streamCompleteHttpRequestToServer (aPayload);
  }
}
