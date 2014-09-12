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

import com.helger.as2lib.cert.ECertificatePartnershipType;
import com.helger.as2lib.cert.ICertificateFactory;
import com.helger.as2lib.exception.DispositionException;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.exception.WrappedOpenAS2Exception;
import com.helger.as2lib.message.AS2Message;
import com.helger.as2lib.message.AS2MessageMDN;
import com.helger.as2lib.message.IMessageMDN;
import com.helger.as2lib.partner.CPartnershipIDs;
import com.helger.as2lib.processor.sender.AS2SenderModule;
import com.helger.as2lib.processor.storage.IProcessorStorageModule;
import com.helger.as2lib.util.AS2Util;
import com.helger.as2lib.util.CAS2Header;
import com.helger.as2lib.util.DispositionType;
import com.helger.commons.io.streams.NonBlockingByteArrayOutputStream;
import com.helger.commons.io.streams.StreamUtils;
import com.helger.commons.string.StringParser;

public class TestSenderModule extends AS2SenderModule
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (TestSenderModule.class);

  /**
   * @param aMsg
   *        AS2Message
   * @param aConn
   *        URLConnection
   * @param sOriginalMIC
   *        mic value from original msg
   */
  protected void __receiveMDN (final AS2Message aMsg, final HttpURLConnection aConn, final String sOriginalMIC) throws OpenAS2Exception,
                                                                                                               IOException
  {
    try
    {
      // Create a MessageMDN and copy HTTP headers
      final IMessageMDN aMDN = new AS2MessageMDN (aMsg);
      copyHttpHeaders (aConn, aMDN.getHeaders ());

      // Receive the MDN data
      final InputStream aConnIS = aConn.getInputStream ();
      final NonBlockingByteArrayOutputStream aMDNStream = new NonBlockingByteArrayOutputStream ();

      // Retrieve the message content
      final long nContentLength = StringParser.parseLong (aMDN.getHeader (CAS2Header.HEADER_CONTENT_LENGTH), -1);
      if (nContentLength >= 0)
        StreamUtils.copyInputStreamToOutputStreamWithLimit (aConnIS, aMDNStream, nContentLength);
      else
        StreamUtils.copyInputStreamToOutputStream (aConnIS, aMDNStream);

      final MimeBodyPart aPart = new MimeBodyPart (aMDN.getHeaders (), aMDNStream.toByteArray ());
      aMsg.getMDN ().setData (aPart);

      // get the MDN partnership info
      aMDN.getPartnership ().setSenderID (CPartnershipIDs.PID_AS2, aMDN.getHeader (CAS2Header.HEADER_AS2_FROM));
      aMDN.getPartnership ().setReceiverID (CPartnershipIDs.PID_AS2, aMDN.getHeader (CAS2Header.HEADER_AS2_TO));
      if (false)
        getSession ().getPartnershipFactory ().updatePartnership (aMDN, false);

      final ICertificateFactory aCertFactory = getSession ().getCertificateFactory ();
      final X509Certificate aSenderCert = aCertFactory.getCertificate (aMsg, ECertificatePartnershipType.RECEIVER);

      AS2Util.parseMDN (aMsg, aSenderCert);

      if (false)
        getSession ().getProcessor ().handle (IProcessorStorageModule.DO_STOREMDN, aMsg, null);

      final String sDisposition = aMsg.getMDN ().getAttribute (AS2MessageMDN.MDNA_DISPOSITION);

      s_aLogger.info ("received MDN [" + sDisposition + "]" + aMsg.getLoggingText ());

      // Asynch MDN 2007-03-12
      // Verify if the original mic is equal to the mic in returned MDN
      final String sReturnMIC = aMsg.getMDN ().getAttribute (AS2MessageMDN.MDNA_MIC);

      if (!sReturnMIC.replaceAll (" ", "").equals (sOriginalMIC.replaceAll (" ", "")))
      {
        // file was sent completely but the returned mic was not matched,
        // don't know it needs or needs not to be resent ? it's depended on
        // what!
        // anyway, just log the warning message here.
        s_aLogger.info ("mic is not matched, original mic: " +
                        sOriginalMIC +
                        " return mic: " +
                        sReturnMIC +
                        aMsg.getLoggingText ());
      }
      else
      {
        s_aLogger.info ("mic is matched, mic: " + sReturnMIC + aMsg.getLoggingText ());
      }

      try
      {
        new DispositionType (sDisposition).validate ();
      }
      catch (final DispositionException ex)
      {
        ex.setText (aMsg.getMDN ().getText ());

        if (ex.getDisposition () != null && ex.getDisposition ().isWarning ())
        {
          ex.addSource (OpenAS2Exception.SOURCE_MESSAGE, aMsg);
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
      final WrappedOpenAS2Exception we = new WrappedOpenAS2Exception (ex);
      we.addSource (OpenAS2Exception.SOURCE_MESSAGE, aMsg);
      throw we;
    }
  }
}
