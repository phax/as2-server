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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.as2.cert.ServerPKCS12CertificateFactory;
import com.helger.as2lib.IDynamicComponent;
import com.helger.as2lib.Session;
import com.helger.as2lib.cert.ICertificateFactory;
import com.helger.as2lib.cert.PKCS12CertificateFactory;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.message.AS2Message;
import com.helger.as2lib.message.IMessage;
import com.helger.as2lib.partner.CPartnershipIDs;
import com.helger.as2lib.partner.IPartnershipFactory;
import com.helger.as2lib.partner.Partnership;
import com.helger.as2lib.processor.sender.IProcessorSenderModule;
import com.helger.as2lib.util.StringMap;
import com.helger.commons.annotations.UnsupportedOperation;

public class EdiIntAs2Client
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (EdiIntAs2Client.class);

  // TODO do object
  // TODO extract interface
  public static Response sendSync (final ConnectionSettings settings, final Request request)
  {
    final Response response = new Response ();
    IMessage msg = null;
    try
    {
      final Partnership partnership = _buildPartnership (settings);

      msg = _buildMessage (partnership, request);
      response.originalMessageId = msg.getMessageID ();

      // logger.info("msgId to send: "+msg.getMessageID());

      final Session aSession = new Session ();
      {
        final StringMap aParams = new StringMap ();
        aParams.setAttribute (PKCS12CertificateFactory.PARAM_FILENAME, settings.p12FilePath);
        aParams.setAttribute (PKCS12CertificateFactory.PARAM_PASSWORD, settings.p12FilePassword);

        final ServerPKCS12CertificateFactory aCertFactory = new ServerPKCS12CertificateFactory ();
        aCertFactory.initDynamicComponent (aSession, aParams);
        aSession.addComponent (ICertificateFactory.COMPID_CERTIFICATE_FACTORY, aCertFactory);
      }

      final IDynamicComponent pf = new SimplePartnershipFactory ();
      aSession.addComponent (IPartnershipFactory.COMPID_PARTNERSHIP_FACTORY, pf);

      final TestSender aSender = new TestSender ();
      aSender.initDynamicComponent (aSession, null);

      // logger.info("sender is ready.");
      if (false)
        s_aLogger.info (msg.toString ());

      aSender.handle (IProcessorSenderModule.DO_SEND, msg, null);
    }
    catch (final Exception e)
    {
      s_aLogger.error (e.getMessage (), e);
      response.isError = true;
      response.exception = e;
      response.errorDescription = e.getMessage ();
    }
    finally
    {
      if (msg != null && msg.getMDN () != null)
      {
        response.receivedMdnId = msg.getMDN ().getMessageID ();
        response.text = msg.getMDN ().getText ();
        response.disposition = msg.getMDN ().getAttribute ("DISPOSITION");
      }
    }

    s_aLogger.info (response.toString ());

    return response;
  }

  private static IMessage _buildMessage (final Partnership aPartnership, final Request aRequest) throws MessagingException,
                                                                                                FileNotFoundException,
                                                                                                OpenAS2Exception
  {
    final AS2Message aMsg = new AS2Message ();
    aMsg.setContentType (aRequest.contentType);
    aMsg.setSubject (aRequest.subject);
    aMsg.setPartnership (aPartnership);
    aMsg.setMessageID (aMsg.generateMessageID ());

    aMsg.setAttribute (CPartnershipIDs.PA_AS2_URL, aPartnership.getAttribute (CPartnershipIDs.PA_AS2_URL));
    aMsg.setAttribute (CPartnershipIDs.PID_AS2, aPartnership.getReceiverID (CPartnershipIDs.PID_AS2));
    aMsg.setAttribute (Partnership.PID_EMAIL, aPartnership.getSenderID (Partnership.PID_EMAIL));

    MimeBodyPart part;
    if (aRequest.stream != null)
      part = new MimeBodyPart (aRequest.stream);
    else
      if (aRequest.filename != null)
        part = new MimeBodyPart (new FileInputStream (aRequest.filename));
      else
      {
        part = new MimeBodyPart ();
        part.setText (aRequest.text);
      }

    aMsg.setData (part);

    return aMsg;
  }

  /**
   * @param settings
   *        Settings
   * @param request
   *        Request
   * @return UnsupportedOperationException
   */
  @UnsupportedOperation
  public Response sendAsync (final ConnectionSettings settings, final Request request)
  {
    throw new UnsupportedOperationException ();
    // Response response = null;
    // return response;
  }

  /**
   * @param settings
   *        Settings
   * @param stream
   *        Input stream
   * @return UnsupportedOperationException
   */
  @UnsupportedOperation
  public Response processAsyncReply (final ConnectionSettings settings, final InputStream stream)
  {
    throw new UnsupportedOperationException ();
    // Response response = null;
    // return response;
  }

  @Nonnull
  private static Partnership _buildPartnership (final ConnectionSettings settings)
  {
    final Partnership partnership = new Partnership (settings.partnershipName);

    partnership.setAttribute (CPartnershipIDs.PA_AS2_URL, settings.receiverAs2Url);
    partnership.setReceiverID (CPartnershipIDs.PID_AS2, settings.receiverAs2Id);
    partnership.setReceiverID (CPartnershipIDs.PID_X509_ALIAS, settings.receiverKeyAlias);

    partnership.setSenderID (CPartnershipIDs.PID_AS2, settings.senderAs2Id);
    partnership.setSenderID (CPartnershipIDs.PID_X509_ALIAS, settings.senderKeyAlias);
    partnership.setSenderID (Partnership.PID_EMAIL, settings.senderEmail);

    partnership.setAttribute (CPartnershipIDs.PA_AS2_MDN_OPTIONS, settings.mdnOptions);

    partnership.setAttribute (CPartnershipIDs.PA_ENCRYPT, settings.encrypt);
    partnership.setAttribute (CPartnershipIDs.PA_SIGN, settings.sign);
    partnership.setAttribute (Partnership.PA_PROTOCOL, "as2");
    // partnership.setAttribute(AS2Partnership.PA_AS2_MDN_TO,"http://localhost:10080");
    partnership.setAttribute (CPartnershipIDs.PA_AS2_RECEIPT_OPTION, null);

    partnership.setAttribute (CPartnershipIDs.PA_MESSAGEID, settings.format);
    return partnership;
  }
}
