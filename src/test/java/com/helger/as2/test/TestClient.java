/**
 * The FreeBSD Copyright
 * Copyright 1994-2008 The FreeBSD Project. All rights reserved.
 * Copyright (C) 2014 Philip Helger ph[at]phloc[dot]com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation
 * are those of the authors and should not be interpreted as representing
 * official policies, either expressed or implied, of the FreeBSD Project.
 */
package com.helger.as2.test;

import java.net.HttpURLConnection;
import java.util.Enumeration;

import javax.mail.Header;
import javax.mail.internet.MimeBodyPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.as2.cert.ServerPKCS12CertificateFactory;
import com.helger.as2lib.IDynamicComponent;
import com.helger.as2lib.Session;
import com.helger.as2lib.cert.ICertificateFactory;
import com.helger.as2lib.cert.PKCS12CertificateFactory;
import com.helger.as2lib.exception.InvalidParameterException;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.message.AS2Message;
import com.helger.as2lib.message.IMessage;
import com.helger.as2lib.message.IMessageMDN;
import com.helger.as2lib.partner.CPartnershipIDs;
import com.helger.as2lib.partner.IPartnershipFactory;
import com.helger.as2lib.partner.Partnership;
import com.helger.as2lib.processor.sender.IProcessorSenderModule;
import com.helger.as2lib.util.CAS2Header;
import com.helger.as2lib.util.DateUtil;
import com.helger.as2lib.util.StringMap;
import com.phloc.commons.io.resource.ClassPathResource;

/**
 * oleo Date: May 4, 2010 Time: 6:56:31 PM
 */
public class TestClient
{
  // Message msg = new AS2Message();
  // getSession().getProcessor().handle(SenderModule.DO_SEND, msg, null);

  private static Logger s_aLogger = LoggerFactory.getLogger (TestClient.class);

  public static void main (final String [] args)
  {
    final ConnectionSettings settings = new ConnectionSettings ();

    settings.p12FilePath = new ClassPathResource ("config/certs.p12").getAsFile ().getAbsolutePath ();
    settings.p12FilePassword = "test";
    settings.senderAs2Id = "OpenAS2A";
    settings.senderEmail = "email";
    settings.senderKeyAlias = "OpenAS2A";
    settings.receiverAs2Id = "OpenAS2B";
    settings.receiverKeyAlias = "OpenAS2B";
    settings.receiverAs2Url = "http://localhost:10080/HttpReceiver";
    settings.partnershipName = "partnership name";
    settings.mdnOptions = "signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1";
    settings.encrypt = false ? null : "3des";
    settings.sign = false ? null : "sha1";

    final Request request = new Request ();
    request.subject = "Test message";
    request.text = "Some info to you";

    EdiIntAs2Client.sendSync (settings, request);
  }

  /**
   * @param args
   */
  public static void main2 (final String [] args)
  {

    // Received-content-MIC
    // original-message-id

    //
    final String pidSenderEmail = "email";
    final String pidAs2 = "GWTESTFM2i";
    final String pidSenderAs2 = "Sender";
    final String receiverKey = "rg_trusted";// "gwtestfm2i_trusted"; //
    final String senderKey = "rg";
    final String paAs2Url = "http://172.16.148.1:8080/as2/HttpReceiver";

    final TestSender service = new TestSender ();

    final Partnership partnership = new Partnership ();
    partnership.setName ("partnership name");
    partnership.setAttribute (CPartnershipIDs.PA_AS2_URL, paAs2Url);
    partnership.setReceiverID (CPartnershipIDs.PID_AS2, pidAs2);
    partnership.setReceiverID (CPartnershipIDs.PID_X509_ALIAS, receiverKey);
    partnership.setSenderID (CPartnershipIDs.PID_AS2, pidSenderAs2);
    partnership.setSenderID (CPartnershipIDs.PID_X509_ALIAS, senderKey);

    partnership.setSenderID (Partnership.PID_EMAIL, pidSenderEmail);

    // partnership.setAttribute(AS2Partnership.PA_AS2_MDN_TO,"http://localhost:10080");
    partnership.setAttribute (CPartnershipIDs.PA_AS2_MDN_OPTIONS,
                              "signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1");

    partnership.setAttribute (CPartnershipIDs.PA_ENCRYPT, "3des");
    partnership.setAttribute (CPartnershipIDs.PA_SIGN, "sha1");
    partnership.setAttribute (Partnership.PA_PROTOCOL, "as2");

    partnership.setAttribute (CPartnershipIDs.PA_AS2_RECEIPT_OPTION, null);

    s_aLogger.info ("ALIAS: " + partnership.getSenderID (CPartnershipIDs.PID_X509_ALIAS));

    final IMessage msg = new AS2Message ();
    msg.setContentType ("application/xml");
    msg.setSubject ("some subject");

    msg.setAttribute (CPartnershipIDs.PA_AS2_URL, paAs2Url);

    msg.setAttribute (CPartnershipIDs.PID_AS2, pidAs2);
    msg.setAttribute (Partnership.PID_EMAIL, "email");
    try
    {
      MimeBodyPart part;
      // part = new MimeBodyPart(new FileInputStream("/tmp/tst"));
      part = new MimeBodyPart ();

      part.setText ("some text from mme part");
      // part.setFileName("/");
      msg.setData (part);
    }
    catch (final Exception e)
    {
      e.printStackTrace ();
    }

    msg.setPartnership (partnership);
    msg.setMessageID (msg.generateMessageID ());
    s_aLogger.info ("msg id: " + msg.getMessageID ());

    Session session = null;
    try
    {
      session = new Session ();
      final ServerPKCS12CertificateFactory cf = new ServerPKCS12CertificateFactory ();
      /*
       * filename="%home%/certs.p12" password="test" interval="300"
       */
      // String filename =
      // "/Users/oleo/samples/parfum.spb.ru/as2/openas2/config/certs.p12";
      final String filename = "/Users/oleo/samples/parfum.spb.ru/as2/mendelson/certificates.p12";
      // String filename =
      // "/Users/oleo/samples/parfum.spb.ru/as2/test/test.p12";
      final String password = "test";
      // gwtestfm2i
      // /Users/oleo/Downloads/portecle-1.5.zip

      // /Users/oleo/samples/parfum.spb.ru/as2/test/test.p12

      final StringMap map = new StringMap ();
      map.setAttribute (PKCS12CertificateFactory.PARAM_FILENAME, filename);
      map.setAttribute (PKCS12CertificateFactory.PARAM_PASSWORD, password);

      cf.initDynamicComponent (session, map);

      // logger.info(cf.getCertificate(msg.getMDN(), Partnership.PTYPE_SENDER));

      // logger.info(cf.getCertificates());

      session.setComponent (ICertificateFactory.COMPID_CERTIFICATE_FACTORY, cf);
      final IDynamicComponent pf = new SimplePartnershipFactory ();
      session.setComponent (IPartnershipFactory.COMPID_PARTNERSHIP_FACTORY, pf);
      service.initDynamicComponent (session, null);
    }
    catch (final OpenAS2Exception e)
    {
      e.printStackTrace ();
    }

    s_aLogger.info ("is requesting  MDN?: " + msg.isRequestingMDN ());
    s_aLogger.info ("is async MDN?: " + msg.isRequestingAsynchMDN ());
    s_aLogger.info ("is rule to recieve MDN active?: " +
                    msg.getPartnership ().getAttribute (CPartnershipIDs.PA_AS2_RECEIPT_OPTION));

    try
    {
      service.handle (IProcessorSenderModule.DO_SEND, msg, null);
      s_aLogger.info ("MDN is " + msg.getMDN ().toString ());

      s_aLogger.info ("message sent" + msg.getLoggingText ());

      final IMessageMDN reply = msg.getMDN ();

      final Enumeration <?> list = reply.getHeaders ().getAllHeaders ();
      final StringBuilder sb = new StringBuilder ("MDN headers:\n");
      while (list.hasMoreElements ())
      {

        final Header h = (Header) list.nextElement ();
        sb.append (h.getName ()).append (" = ").append (h.getValue ()).append ('\n');

      }

      // logger.info(sb);

      final Enumeration <?> list2 = reply.getData ().getAllHeaders ();
      final StringBuilder sb2 = new StringBuilder ("Mime headers:\n");
      while (list2.hasMoreElements ())
      {

        final Header h = (Header) list2.nextElement ();
        sb2.append (h.getName ()).append (" = ").append (h.getValue ()).append ('\n');

      }

      // logger.info(sb2);

      // logger.info(reply.getData().getRawInputStream().toString());

    }
    catch (final Exception e)
    {
      s_aLogger.error ("shit happens");
      e.printStackTrace ();
    }
  }

  protected static void checkRequired (final IMessage msg)
  {
    final Partnership partnership = msg.getPartnership ();

    try
    {
      InvalidParameterException.checkValue (msg, "ContentType", msg.getContentType ());
      InvalidParameterException.checkValue (msg,
                                            "Attribute: " + CPartnershipIDs.PA_AS2_URL,
                                            partnership.getAttribute (CPartnershipIDs.PA_AS2_URL));
      InvalidParameterException.checkValue (msg,
                                            "Receiver: " + CPartnershipIDs.PID_AS2,
                                            partnership.getReceiverID (CPartnershipIDs.PID_AS2));
      InvalidParameterException.checkValue (msg,
                                            "Sender: " + CPartnershipIDs.PID_AS2,
                                            partnership.getSenderID (CPartnershipIDs.PID_AS2));
      InvalidParameterException.checkValue (msg, "Subject", msg.getSubject ());
      InvalidParameterException.checkValue (msg,
                                            "Sender: " + Partnership.PID_EMAIL,
                                            partnership.getSenderID (Partnership.PID_EMAIL));
      InvalidParameterException.checkValue (msg, "Message Data", msg.getData ());
    }
    catch (final InvalidParameterException rpe)
    {
      rpe.addSource (OpenAS2Exception.SOURCE_MESSAGE, msg);

    }
  }

  protected void updateHttpHeaders (final HttpURLConnection conn, final IMessage msg)
  {
    final Partnership partnership = msg.getPartnership ();

    conn.setRequestProperty ("Connection", "close, TE");
    conn.setRequestProperty ("User-Agent", "OpenAS2 AS2Sender");

    conn.setRequestProperty ("Date", DateUtil.getFormattedDateNow ("EEE, dd MMM yyyy HH:mm:ss Z"));
    conn.setRequestProperty ("Message-ID", msg.getMessageID ());
    // make sure this is the encoding used in the msg, run TBF1
    conn.setRequestProperty ("Mime-Version", "1.0");
    conn.setRequestProperty ("Content-type", msg.getContentType ());
    conn.setRequestProperty (CAS2Header.AS2_VERSION, "1.1");
    conn.setRequestProperty ("Recipient-Address", partnership.getAttribute (CPartnershipIDs.PA_AS2_URL));
    conn.setRequestProperty (CAS2Header.AS2_TO, partnership.getReceiverID (CPartnershipIDs.PID_AS2));
    conn.setRequestProperty (CAS2Header.AS2_FROM, partnership.getSenderID (CPartnershipIDs.PID_AS2));
    conn.setRequestProperty ("Subject", msg.getSubject ());
    conn.setRequestProperty ("From", partnership.getSenderID (Partnership.PID_EMAIL));

    final String dispTo = partnership.getAttribute (CPartnershipIDs.PA_AS2_MDN_TO);
    if (dispTo != null)
      conn.setRequestProperty ("Disposition-Notification-To", dispTo);

    final String dispOptions = partnership.getAttribute (CPartnershipIDs.PA_AS2_MDN_OPTIONS);
    if (dispOptions != null)
      conn.setRequestProperty ("Disposition-Notification-Options", dispOptions);

    // Asynch MDN 2007-03-12
    final String receiptOption = partnership.getAttribute (CPartnershipIDs.PA_AS2_RECEIPT_OPTION);
    if (receiptOption != null)
      conn.setRequestProperty ("Receipt-delivery-option", receiptOption);

    // As of 2007-06-01
    final String contentDisp = msg.getContentDisposition ();
    if (contentDisp != null)
      conn.setRequestProperty ("Content-Disposition", contentDisp);
  }

  /*
   * <partnerships> <partner name="OpenAS2A" as2_id="OpenAS2A"
   * x509_alias="OpenAS2A" email="OpenAS2 A email"/> <partner name="OpenAS2B"
   * as2_id="OpenAS2B" x509_alias="OpenAS2B" email="OpenAS2 A email"/>
   * <partnership name="OpenAS2A-OpenAS2B"> <sender name="OpenAS2A"/> <receiver
   * name="OpenAS2B"/> <attribute name="protocol" value="as2"/> <attribute
   * name="subject" value="From OpenAS2A to OpenAS2B"/> <attribute
   * name="as2_url" value="http://localhost:10080"/> <attribute
   * name="as2_mdn_to" value="http://localhost:10080"/> <attribute
   * name="as2_mdn_options" value=
   * "signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1"
   * /> <attribute name="encrypt" value="3des"/> <attribute name="sign"
   * value="md5"/> </partnership> <partnership name="OpenAS2B-OpenAS2A"> <sender
   * name="OpenAS2B"/> <receiver name="OpenAS2A"/> <attribute name="protocol"
   * value="as2"/> <attribute name="subject" value="From OpenAS2B to OpenAS2A"/>
   * <attribute name="as2_url" value="http://localhost:10080"/> <attribute
   * name="as2_mdn_to" value="http://localhost:10080"/> <attribute
   * name="as2_mdn_options" value=
   * "signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1"
   * /> <attribute name="encrypt" value="3des"/> <attribute name="sign"
   * value="sha1"/> </partnership> </partnerships>
   */

}
