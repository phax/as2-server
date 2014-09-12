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

import java.io.File;
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
import com.helger.commons.io.resource.ClassPathResource;

/**
 * @author oleo Date: May 4, 2010 Time: 6:56:31 PM
 */
public class TestClient
{
  // Message msg = new AS2Message();
  // getSession().getProcessor().handle(SenderModule.DO_SEND, msg, null);

  private static final Logger s_aLogger = LoggerFactory.getLogger (TestClient.class);

  public static void main (final String [] args)
  {
    final boolean DO_ENCRYPT = true;
    final boolean DO_SIGN = true;

    final ConnectionSettings aSettings = new ConnectionSettings ();
    aSettings.p12FilePath = new ClassPathResource ("config/certs.p12").getAsFile ().getAbsolutePath ();
    aSettings.p12FilePassword = "test";
    aSettings.senderAs2Id = "OpenAS2A";
    aSettings.senderEmail = "email";
    aSettings.senderKeyAlias = "OpenAS2A";
    aSettings.receiverAs2Id = "OpenAS2B";
    aSettings.receiverKeyAlias = "OpenAS2B";
    aSettings.receiverAs2Url = "http://localhost:10080/HttpReceiver";
    aSettings.partnershipName = "partnership name";
    aSettings.mdnOptions = "signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1";
    aSettings.encrypt = DO_ENCRYPT ? "3des" : null;
    aSettings.sign = DO_SIGN ? "sha1" : null;

    final AS2Request aRequest = new AS2Request ("Test message");
    aRequest.setData (new File ("src/test/resources/dummy.txt"));
    AS2Client.sendSynchronous (aSettings, aRequest);
  }

  /**
   * @param args
   *        Main args
   */
  public static void main2 (final String [] args) throws Exception
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

    final TestSenderModule aTestSender = new TestSenderModule ();

    final Partnership aPartnership = new Partnership ("partnership name");
    aPartnership.setAttribute (CPartnershipIDs.PA_AS2_URL, paAs2Url);
    aPartnership.setReceiverID (CPartnershipIDs.PID_AS2, pidAs2);
    aPartnership.setReceiverID (CPartnershipIDs.PID_X509_ALIAS, receiverKey);
    aPartnership.setSenderID (CPartnershipIDs.PID_AS2, pidSenderAs2);
    aPartnership.setSenderID (CPartnershipIDs.PID_X509_ALIAS, senderKey);
    aPartnership.setSenderID (Partnership.PID_EMAIL, pidSenderEmail);

    // partnership.setAttribute(AS2Partnership.PA_AS2_MDN_TO,"http://localhost:10080");
    aPartnership.setAttribute (CPartnershipIDs.PA_AS2_MDN_OPTIONS,
                               "signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1");

    aPartnership.setAttribute (CPartnershipIDs.PA_ENCRYPT, "3des");
    aPartnership.setAttribute (CPartnershipIDs.PA_SIGN, "sha1");
    aPartnership.setAttribute (Partnership.PA_PROTOCOL, "as2");

    aPartnership.setAttribute (CPartnershipIDs.PA_AS2_RECEIPT_OPTION, null);

    s_aLogger.info ("ALIAS: " + aPartnership.getSenderID (CPartnershipIDs.PID_X509_ALIAS));

    final IMessage aMsg = new AS2Message ();
    aMsg.setContentType ("application/xml");
    aMsg.setSubject ("some subject");

    aMsg.setAttribute (CPartnershipIDs.PA_AS2_URL, paAs2Url);

    aMsg.setAttribute (CPartnershipIDs.PID_AS2, pidAs2);
    aMsg.setAttribute (Partnership.PID_EMAIL, "email");

    MimeBodyPart aBodyPart;
    // part = new MimeBodyPart(new FileInputStream("/tmp/tst"));
    aBodyPart = new MimeBodyPart ();

    aBodyPart.setText ("some text from mme part");
    // part.setFileName("/");
    aMsg.setData (aBodyPart);

    aMsg.setPartnership (aPartnership);
    aMsg.setMessageID (aMsg.generateMessageID ());
    s_aLogger.info ("msg id: " + aMsg.getMessageID ());

    final Session aSession = new Session ();
    final ServerPKCS12CertificateFactory aCertFactory = new ServerPKCS12CertificateFactory ();
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

    final StringMap aCertFactorySettings = new StringMap ();
    aCertFactorySettings.setAttribute (PKCS12CertificateFactory.PARAM_FILENAME, filename);
    aCertFactorySettings.setAttribute (PKCS12CertificateFactory.PARAM_PASSWORD, password);

    aCertFactory.initDynamicComponent (aSession, aCertFactorySettings);

    // logger.info(cf.getCertificate(msg.getMDN(), Partnership.PTYPE_SENDER));

    // logger.info(cf.getCertificates());

    aSession.addComponent (ICertificateFactory.COMPID_CERTIFICATE_FACTORY, aCertFactory);

    final IDynamicComponent aPartnershipFactory = new SimplePartnershipFactory ();
    aSession.addComponent (IPartnershipFactory.COMPID_PARTNERSHIP_FACTORY, aPartnershipFactory);
    aTestSender.initDynamicComponent (aSession, null);

    s_aLogger.info ("is requesting  MDN?: " + aMsg.isRequestingMDN ());
    s_aLogger.info ("is async MDN?: " + aMsg.isRequestingAsynchMDN ());
    s_aLogger.info ("is rule to recieve MDN active?: " +
                    aMsg.getPartnership ().getAttribute (CPartnershipIDs.PA_AS2_RECEIPT_OPTION));

    aTestSender.handle (IProcessorSenderModule.DO_SEND, aMsg, null);
    s_aLogger.info ("MDN is " + aMsg.getMDN ().toString ());

    s_aLogger.info ("message sent" + aMsg.getLoggingText ());

    final IMessageMDN reply = aMsg.getMDN ();

    final Enumeration <?> aList = reply.getHeaders ().getAllHeaders ();
    final StringBuilder aSB = new StringBuilder ("MDN headers:\n");
    while (aList.hasMoreElements ())
    {
      final Header aHeader = (Header) aList.nextElement ();
      aSB.append (aHeader.getName ()).append (" = ").append (aHeader.getValue ()).append ('\n');
    }

    // logger.info(sb);

    final Enumeration <?> list2 = reply.getData ().getAllHeaders ();
    final StringBuilder aSB2 = new StringBuilder ("Mime headers:\n");
    while (list2.hasMoreElements ())
    {

      final Header aHeader = (Header) list2.nextElement ();
      aSB2.append (aHeader.getName ()).append (" = ").append (aHeader.getValue ()).append ('\n');

    }

    // logger.info(sb2);

    // logger.info(reply.getData().getRawInputStream().toString());
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

    conn.setRequestProperty (CAS2Header.HEADER_CONNECTION, CAS2Header.DEFAULT_CONNECTION);
    conn.setRequestProperty (CAS2Header.HEADER_USER_AGENT, CAS2Header.DEFAULT_USER_AGENT);

    conn.setRequestProperty (CAS2Header.HEADER_DATE, DateUtil.getFormattedDateNow (CAS2Header.DEFAULT_DATE_FORMAT));
    conn.setRequestProperty (CAS2Header.HEADER_MESSAGE_ID, msg.getMessageID ());
    // make sure this is the encoding used in the msg, run TBF1
    conn.setRequestProperty (CAS2Header.HEADER_MIME_VERSION, CAS2Header.DEFAULT_MIME_VERSION);
    conn.setRequestProperty (CAS2Header.HEADER_CONTENT_TYPE, msg.getContentType ());
    conn.setRequestProperty (CAS2Header.HEADER_AS2_VERSION, CAS2Header.DEFAULT_AS2_VERSION);
    conn.setRequestProperty (CAS2Header.HEADER_RECIPIENT_ADDRESS, partnership.getAttribute (CPartnershipIDs.PA_AS2_URL));
    conn.setRequestProperty (CAS2Header.HEADER_AS2_TO, partnership.getReceiverID (CPartnershipIDs.PID_AS2));
    conn.setRequestProperty (CAS2Header.HEADER_AS2_FROM, partnership.getSenderID (CPartnershipIDs.PID_AS2));
    conn.setRequestProperty (CAS2Header.HEADER_SUBJECT, msg.getSubject ());
    conn.setRequestProperty (CAS2Header.HEADER_FROM, partnership.getSenderID (Partnership.PID_EMAIL));

    final String dispTo = partnership.getAttribute (CPartnershipIDs.PA_AS2_MDN_TO);
    if (dispTo != null)
      conn.setRequestProperty (CAS2Header.HEADER_DISPOSITION_NOTIFICATION_TO, dispTo);

    final String dispOptions = partnership.getAttribute (CPartnershipIDs.PA_AS2_MDN_OPTIONS);
    if (dispOptions != null)
      conn.setRequestProperty (CAS2Header.HEADER_DISPOSITION_NOTIFICATION_OPTIONS, dispOptions);

    // Asynch MDN 2007-03-12
    final String receiptOption = partnership.getAttribute (CPartnershipIDs.PA_AS2_RECEIPT_OPTION);
    if (receiptOption != null)
      conn.setRequestProperty (CAS2Header.HEADER_RECEIPT_DELIVERY_OPTION, receiptOption);

    // As of 2007-06-01
    final String contentDisp = msg.getContentDisposition ();
    if (contentDisp != null)
      conn.setRequestProperty (CAS2Header.HEADER_CONTENT_DISPOSITION, contentDisp);
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
