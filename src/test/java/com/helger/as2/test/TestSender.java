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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.cert.X509Certificate;

import javax.mail.internet.MimeBodyPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.as2lib.cert.ICertificateFactory;
import com.helger.as2lib.exception.DispositionException;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.exception.WrappedException;
import com.helger.as2lib.message.AS2Message;
import com.helger.as2lib.message.AS2MessageMDN;
import com.helger.as2lib.message.IMessageMDN;
import com.helger.as2lib.partner.CPartnershipIDs;
import com.helger.as2lib.partner.Partnership;
import com.helger.as2lib.processor.sender.AS2SenderModule;
import com.helger.as2lib.processor.storage.IProcessorStorageModule;
import com.helger.as2lib.util.AS2Util;
import com.helger.as2lib.util.CAS2Header;
import com.helger.as2lib.util.DispositionType;
import com.phloc.commons.io.streams.NonBlockingByteArrayOutputStream;
import com.phloc.commons.io.streams.StreamUtils;
import com.phloc.commons.string.StringParser;

public class TestSender extends AS2SenderModule
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (TestSender.class);

  /**
   * @param msg
   *        AS2Message
   * @param conn
   *        URLConnection
   * @param originalmic
   *        mic value from original msg
   */
  @Override
  protected void receiveMDN (final AS2Message msg, final HttpURLConnection conn, final String originalmic) throws OpenAS2Exception,
                                                                                                          IOException
  {
    try
    {
      // Create a MessageMDN and copy HTTP headers
      final IMessageMDN mdn = new AS2MessageMDN (msg);
      copyHttpHeaders (conn, mdn.getHeaders ());

      // Receive the MDN data
      final InputStream connIn = conn.getInputStream ();
      final NonBlockingByteArrayOutputStream mdnStream = new NonBlockingByteArrayOutputStream ();

      // Retrieve the message content
      final long nContentLength = StringParser.parseLong (mdn.getHeader ("Content-Length"), -1);
      if (nContentLength >= 0)
        StreamUtils.copyInputStreamToOutputStreamWithLimit (connIn, mdnStream, nContentLength);
      else
        StreamUtils.copyInputStreamToOutputStream (connIn, mdnStream);

      final MimeBodyPart part = new MimeBodyPart (mdn.getHeaders (), mdnStream.toByteArray ());
      msg.getMDN ().setData (part);

      // get the MDN partnership info
      mdn.getPartnership ().setSenderID (CPartnershipIDs.PID_AS2, mdn.getHeader (CAS2Header.HEADER_AS2_FROM));
      mdn.getPartnership ().setReceiverID (CPartnershipIDs.PID_AS2, mdn.getHeader (CAS2Header.HEADER_AS2_TO));
      if (false)
        getSession ().getPartnershipFactory ().updatePartnership (mdn, false);

      final ICertificateFactory cFx = getSession ().getCertificateFactory ();
      if (false)
      {
        s_aLogger.info ("ALIAS: " + mdn.getPartnership ());
        // //.getSenderID(SecurePartnership.PID_X509_ALIAS));
        // X509Certificate senderCert = cFx.getCertificate(mdn,
        // Partnership.PTYPE_SENDER);
        final String certAlias = msg.getPartnership ().getReceiverID (CPartnershipIDs.PID_X509_ALIAS);
        s_aLogger.info ("CERT ALIAS: " + certAlias);
      }
      final X509Certificate senderCert = cFx.getCertificate (msg, Partnership.PARTNERSHIP_TYPE_RECEIVER);

      AS2Util.parseMDN (msg, senderCert);

      if (false)
        getSession ().getProcessor ().handle (IProcessorStorageModule.DO_STOREMDN, msg, null);

      final String disposition = msg.getMDN ().getAttribute (AS2MessageMDN.MDNA_DISPOSITION);

      s_aLogger.info ("received MDN [" + disposition + "]" + msg.getLoggingText ());

      // Asynch MDN 2007-03-12
      // Verify if the original mic is equal to the mic in returned MDN
      final String returnmic = msg.getMDN ().getAttribute (AS2MessageMDN.MDNA_MIC);

      if (!returnmic.replaceAll (" ", "").equals (originalmic.replaceAll (" ", "")))
      {
        // file was sent completely but the returned mic was not matched,
        // don't know it needs or needs not to be resent ? it's depended on
        // what!
        // anyway, just log the warning message here.
        s_aLogger.info ("mic is not matched, original mic: " +
                        originalmic +
                        " return mic: " +
                        returnmic +
                        msg.getLoggingText ());
      }
      else
      {
        s_aLogger.info ("mic is matched, mic: " + returnmic + msg.getLoggingText ());
      }

      try
      {
        new DispositionType (disposition).validate ();
      }
      catch (final DispositionException ex)
      {
        ex.setText (msg.getMDN ().getText ());

        if (ex.getDisposition () != null && ex.getDisposition ().isWarning ())
        {
          ex.addSource (OpenAS2Exception.SOURCE_MESSAGE, msg);
          ex.terminate ();
        }
        else
        {
          throw ex;
        }
      }
    }
    catch (final IOException ex)
    {
      throw ex;
    }
    catch (final Exception ex)
    {
      final WrappedException we = new WrappedException (ex);
      we.addSource (OpenAS2Exception.SOURCE_MESSAGE, msg);
      throw we;
    }
  }
}
