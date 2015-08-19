/**
 * The FreeBSD Copyright
 * Copyright 1994-2008 The FreeBSD Project. All rights reserved.
 * Copyright (C) 2013-2015 Philip Helger philip[at]helger[dot]com
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

import javax.annotation.Nonnull;
import javax.mail.Header;
import javax.mail.internet.MimeBodyPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.as2lib.cert.PKCS12CertificateFactory;
import com.helger.as2lib.client.AS2Client;
import com.helger.as2lib.client.AS2ClientRequest;
import com.helger.as2lib.client.AS2ClientSettings;
import com.helger.as2lib.crypto.ECryptoAlgorithmCrypt;
import com.helger.as2lib.crypto.ECryptoAlgorithmSign;
import com.helger.as2lib.disposition.DispositionOptions;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.message.AS2Message;
import com.helger.as2lib.message.IMessage;
import com.helger.as2lib.message.IMessageMDN;
import com.helger.as2lib.params.InvalidParameterException;
import com.helger.as2lib.partner.CPartnershipIDs;
import com.helger.as2lib.partner.Partnership;
import com.helger.as2lib.partner.SelfFillingPartnershipFactory;
import com.helger.as2lib.processor.sender.AS2SenderModule;
import com.helger.as2lib.processor.sender.IProcessorSenderModule;
import com.helger.as2lib.session.AS2Session;
import com.helger.as2lib.util.CAS2Header;
import com.helger.as2lib.util.DateHelper;
import com.helger.as2lib.util.StringMap;
import com.helger.commons.io.resource.ClassPathResource;

/**
 * <pre>
  * &lt;partnerships&gt;
  *  &lt;partner name="OpenAS2A" as2_id="OpenAS2A" x509_alias="OpenAS2A" email="OpenAS2 A email"/&gt;
  *  &lt;partner name="OpenAS2B" as2_id="OpenAS2B" x509_alias="OpenAS2B" email="OpenAS2 B email"/&gt;
  *  &lt;partnership name="OpenAS2A-OpenAS2B"&gt;
  *     &lt;sender name="OpenAS2A"/&gt;
  *     &lt;receiver name="OpenAS2B"/&gt;
  *     &lt;attribute name="protocol" value="as2"/&gt;
  *     &lt;attribute name="subject" value="From OpenAS2A to OpenAS2B"/&gt;
  *     &lt;attribute name="as2_url" value="http://localhost:10080"/&gt;
  *     &lt;attribute name="as2_mdn_to" value="http://localhost:10080"/&gt;
  *     &lt;attribute name="as2_mdn_options" value="signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1" /&gt;
  *     &lt;attribute name="encrypt" value="3des"/&gt;
  *     &lt;attribute name="sign" value="md5"/&gt;
  *   &lt;/partnership&gt;
  *   &lt;partnership name="OpenAS2B-OpenAS2A"&gt;
  *     &lt;sender name="OpenAS2B"/&gt;
  *     &lt;receiver name="OpenAS2A"/&gt;
  *     &lt;attribute name="protocol" value="as2"/&gt;
  *     &lt;attribute name="subject" value="From OpenAS2B to OpenAS2A"/&gt;
  *     &lt;attribute name="as2_url" value="http://localhost:10080"/&gt;
  *     &lt;attribute name="as2_mdn_to" value="http://localhost:10080"/&gt;
  *     &lt;attribute name="as2_mdn_options" value="signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1" /&gt;
  *     &lt;attribute name="encrypt" value="3des"/&gt;
  *     &lt;attribute name="sign" value="sha1"/&gt;
  *   &lt;/partnership&gt;
  * &lt;/partnerships&gt;
 * </pre>
 *
 * @author oleo Date: May 4, 2010 Time: 6:56:31 PM
 */
public class MainTestClient
{
  // Message msg = new AS2Message();
  // getSession().getProcessor().handle(SenderModule.DO_SEND, msg, null);

  private static final Logger s_aLogger = LoggerFactory.getLogger (MainTestClient.class);

  public static void main (final String [] args)
  {
    final boolean DO_ENCRYPT = true;
    final boolean DO_SIGN = true;

    final AS2ClientSettings aSettings = new AS2ClientSettings ();
    aSettings.setKeyStore (ClassPathResource.getAsFile ("config/certs.p12"), "test");
    aSettings.setSenderData ("OpenAS2A", "email@example.org", "OpenAS2A");
    aSettings.setReceiverData ("OpenAS2B", "OpenAS2B", "http://localhost:10080/HttpReceiver");
    aSettings.setPartnershipName ("Partnership name");
    aSettings.setEncryptAndSign (DO_ENCRYPT ? ECryptoAlgorithmCrypt.CRYPT_3DES : null,
                                 DO_SIGN ? ECryptoAlgorithmSign.DIGEST_SHA1 : null);
    // Use the default MDN options
    // Use the default message ID format

    final AS2ClientRequest aRequest = new AS2ClientRequest ("Test message");
    aRequest.setData (new File ("src/test/resources/dummy.txt"));
    new AS2Client ().sendSynchronous (aSettings, aRequest);
  }

  /**
   * @param args
   *        Main args
   * @throws Exception
   *         in case of error
   */
  public static void main2 (final String [] args) throws Exception
  {
    // Received-content-MIC
    // original-message-id

    final String pidSenderEmail = "email";
    final String pidAs2 = "GWTESTFM2i";
    final String pidSenderAs2 = "Sender";
    final String receiverKey = "rg_trusted";// "gwtestfm2i_trusted"; //
    final String senderKey = "rg";
    final String paAs2Url = "http://172.16.148.1:8080/as2/HttpReceiver";

    final AS2SenderModule aTestSender = new AS2SenderModule ();

    final Partnership aPartnership = new Partnership ("partnership name");
    aPartnership.setSenderAS2ID (pidSenderAs2);
    aPartnership.setSenderX509Alias (senderKey);
    aPartnership.setSenderEmail (pidSenderEmail);

    aPartnership.setReceiverAS2ID (pidAs2);
    aPartnership.setReceiverX509Alias (receiverKey);

    aPartnership.setAttribute (CPartnershipIDs.PA_AS2_URL, paAs2Url);
    if (false)
      aPartnership.setAttribute (CPartnershipIDs.PA_AS2_MDN_TO, "http://localhost:10080");
    aPartnership.setAttribute (CPartnershipIDs.PA_AS2_MDN_OPTIONS,
                               new DispositionOptions ().setProtocolImportance (DispositionOptions.IMPORTANCE_OPTIONAL)
                                                        .setProtocol (DispositionOptions.PROTOCOL_PKCS7_SIGNATURE)
                                                        .setMICAlgImportance (DispositionOptions.IMPORTANCE_OPTIONAL)
                                                        .setMICAlg (ECryptoAlgorithmSign.DIGEST_SHA1)
                                                        .getAsString ());

    aPartnership.setAttribute (CPartnershipIDs.PA_ENCRYPT, ECryptoAlgorithmCrypt.CRYPT_3DES.getID ());
    aPartnership.setAttribute (CPartnershipIDs.PA_SIGN, ECryptoAlgorithmSign.DIGEST_SHA1.getID ());
    aPartnership.setAttribute (CPartnershipIDs.PA_PROTOCOL, AS2Message.PROTOCOL_AS2);

    aPartnership.setAttribute (CPartnershipIDs.PA_AS2_RECEIPT_OPTION, null);

    s_aLogger.info ("ALIAS: " + aPartnership.getSenderX509Alias ());

    final IMessage aMsg = new AS2Message ();
    aMsg.setContentType ("application/xml");
    aMsg.setSubject ("some subject");

    aMsg.setAttribute (CPartnershipIDs.PA_AS2_URL, paAs2Url);

    aMsg.setAttribute (CPartnershipIDs.PID_AS2, pidAs2);
    aMsg.setAttribute (CPartnershipIDs.PID_EMAIL, "email");

    MimeBodyPart aBodyPart;
    // part = new MimeBodyPart(new FileInputStream("/tmp/tst"));
    aBodyPart = new MimeBodyPart ();

    aBodyPart.setText ("some text from mme part");
    // part.setFileName("/");
    aMsg.setData (aBodyPart);

    aMsg.setPartnership (aPartnership);
    aMsg.setMessageID (aMsg.generateMessageID ());
    s_aLogger.info ("msg id: " + aMsg.getMessageID ());

    final AS2Session aSession = new AS2Session ();
    final PKCS12CertificateFactory aCertFactory = new PKCS12CertificateFactory ();
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
    aCertFactorySettings.setAttribute (PKCS12CertificateFactory.ATTR_FILENAME, filename);
    aCertFactorySettings.setAttribute (PKCS12CertificateFactory.ATTR_PASSWORD, password);

    aCertFactory.initDynamicComponent (aSession, aCertFactorySettings);

    // logger.info(cf.getCertificate(msg.getMDN(), Partnership.PTYPE_SENDER));

    // logger.info(cf.getCertificates());

    aSession.setCertificateFactory (aCertFactory);

    final SelfFillingPartnershipFactory aPartnershipFactory = new SelfFillingPartnershipFactory ();
    aSession.setPartnershipFactory (aPartnershipFactory);
    aTestSender.initDynamicComponent (aSession, null);

    s_aLogger.info ("is requesting  MDN?: " + aMsg.isRequestingMDN ());
    s_aLogger.info ("is async MDN?: " + aMsg.isRequestingAsynchMDN ());
    s_aLogger.info ("is rule to recieve MDN active?: " +
                    aMsg.getPartnership ().getAttribute (CPartnershipIDs.PA_AS2_RECEIPT_OPTION));

    aTestSender.handle (IProcessorSenderModule.DO_SEND, aMsg, null);
    s_aLogger.info ("MDN is " + aMsg.getMDN ().toString ());

    s_aLogger.info ("message sent" + aMsg.getLoggingText ());

    final IMessageMDN reply = aMsg.getMDN ();

    if (false)
      s_aLogger.info ("MDN headers:\n" + reply.getHeadersDebugFormatted ());

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

  protected static void checkRequired (final IMessage aMsg)
  {
    final Partnership aPartnership = aMsg.getPartnership ();

    try
    {
      InvalidParameterException.checkValue (aMsg, "ContentType", aMsg.getContentType ());
      InvalidParameterException.checkValue (aMsg,
                                            "Attribute: " + CPartnershipIDs.PA_AS2_URL,
                                            aPartnership.getAttribute (CPartnershipIDs.PA_AS2_URL));
      InvalidParameterException.checkValue (aMsg,
                                            "Receiver: " + CPartnershipIDs.PID_AS2,
                                            aPartnership.getReceiverAS2ID ());
      InvalidParameterException.checkValue (aMsg, "Sender: " + CPartnershipIDs.PID_AS2, aPartnership.getSenderAS2ID ());
      InvalidParameterException.checkValue (aMsg, "Subject", aMsg.getSubject ());
      InvalidParameterException.checkValue (aMsg,
                                            "Sender: " + CPartnershipIDs.PID_EMAIL,
                                            aPartnership.getSenderEmail ());
      InvalidParameterException.checkValue (aMsg, "Message Data", aMsg.getData ());
    }
    catch (final InvalidParameterException rpe)
    {
      rpe.addSource (OpenAS2Exception.SOURCE_MESSAGE, aMsg);
    }
  }

  protected void updateHttpHeaders (@Nonnull final HttpURLConnection aConn, @Nonnull final IMessage aMsg)
  {
    final Partnership aPartnership = aMsg.getPartnership ();

    aConn.setRequestProperty (CAS2Header.HEADER_CONNECTION, CAS2Header.DEFAULT_CONNECTION);
    aConn.setRequestProperty (CAS2Header.HEADER_USER_AGENT, CAS2Header.DEFAULT_USER_AGENT);

    aConn.setRequestProperty (CAS2Header.HEADER_DATE, DateHelper.getFormattedDateNow (CAS2Header.DEFAULT_DATE_FORMAT));
    aConn.setRequestProperty (CAS2Header.HEADER_MESSAGE_ID, aMsg.getMessageID ());
    // make sure this is the encoding used in the msg, run TBF1
    aConn.setRequestProperty (CAS2Header.HEADER_MIME_VERSION, CAS2Header.DEFAULT_MIME_VERSION);
    aConn.setRequestProperty (CAS2Header.HEADER_CONTENT_TYPE, aMsg.getContentType ());
    aConn.setRequestProperty (CAS2Header.HEADER_AS2_VERSION, CAS2Header.DEFAULT_AS2_VERSION);
    aConn.setRequestProperty (CAS2Header.HEADER_RECIPIENT_ADDRESS,
                              aPartnership.getAttribute (CPartnershipIDs.PA_AS2_URL));
    aConn.setRequestProperty (CAS2Header.HEADER_AS2_TO, aPartnership.getReceiverAS2ID ());
    aConn.setRequestProperty (CAS2Header.HEADER_AS2_FROM, aPartnership.getSenderAS2ID ());
    aConn.setRequestProperty (CAS2Header.HEADER_SUBJECT, aMsg.getSubject ());
    aConn.setRequestProperty (CAS2Header.HEADER_FROM, aPartnership.getSenderEmail ());

    final String sDispTo = aPartnership.getAttribute (CPartnershipIDs.PA_AS2_MDN_TO);
    if (sDispTo != null)
      aConn.setRequestProperty (CAS2Header.HEADER_DISPOSITION_NOTIFICATION_TO, sDispTo);

    final String sDispOptions = aPartnership.getAttribute (CPartnershipIDs.PA_AS2_MDN_OPTIONS);
    if (sDispOptions != null)
      aConn.setRequestProperty (CAS2Header.HEADER_DISPOSITION_NOTIFICATION_OPTIONS, sDispOptions);

    // Asynch MDN 2007-03-12
    final String sReceiptOption = aPartnership.getAttribute (CPartnershipIDs.PA_AS2_RECEIPT_OPTION);
    if (sReceiptOption != null)
      aConn.setRequestProperty (CAS2Header.HEADER_RECEIPT_DELIVERY_OPTION, sReceiptOption);

    // As of 2007-06-01
    final String sContentDisp = aMsg.getContentDisposition ();
    if (sContentDisp != null)
      aConn.setRequestProperty (CAS2Header.HEADER_CONTENT_DISPOSITION, sContentDisp);
  }
}
