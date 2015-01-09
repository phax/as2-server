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
package com.helger.as2.cmd.cert;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import com.helger.as2.cmd.CommandResult;
import com.helger.as2.cmd.ECommandResultType;
import com.helger.as2lib.cert.IAliasedCertificateFactory;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.exception.WrappedOpenAS2Exception;
import com.helger.as2lib.util.AS2Util;

public class ImportCertCommand extends AbstractAliasedCertCommand
{
  @Override
  public String getDefaultDescription ()
  {
    return "Import a certificate into the current certificate store";
  }

  @Override
  public String getDefaultName ()
  {
    return "import";
  }

  @Override
  public String getDefaultUsage ()
  {
    return "import <alias> <filename> [<password>]";
  }

  @Override
  public CommandResult execute (final IAliasedCertificateFactory certFx, final Object [] params) throws OpenAS2Exception
  {
    if (params.length < 2)
    {
      return new CommandResult (ECommandResultType.TYPE_INVALID_PARAM_COUNT, getUsage ());
    }

    synchronized (certFx)
    {
      final String alias = params[0].toString ();
      final String filename = params[1].toString ();
      String password = null;

      if (params.length > 2)
      {
        password = params[2].toString ();
      }

      try
      {
        if (filename.endsWith (".p12"))
        {
          if (password == null)
          {
            return new CommandResult (ECommandResultType.TYPE_INVALID_PARAM_COUNT, getUsage () +
                                                                                   " (Password is required for p12 files)");
          }

          return importPrivateKey (certFx, alias, filename, password);
        }
        return importCert (certFx, alias, filename);
      }
      catch (final Exception ex)
      {
        throw WrappedOpenAS2Exception.wrap (ex);
      }
    }
  }

  protected CommandResult importCert (final IAliasedCertificateFactory certFx, final String alias, final String filename) throws IOException,
                                                                                                                         CertificateException,
                                                                                                                         OpenAS2Exception
  {
    final FileInputStream fis = new FileInputStream (filename);
    final BufferedInputStream bis = new BufferedInputStream (fis);

    final java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance ("X.509");

    final CommandResult cmdRes = new CommandResult (ECommandResultType.TYPE_OK, "Certificate(s) imported successfully");

    while (bis.available () > 0)
    {
      final Certificate cert = cf.generateCertificate (bis);

      if (cert instanceof X509Certificate)
      {
        certFx.addCertificate (alias, (X509Certificate) cert, true);
        cmdRes.addResult ("Imported certificate: " + cert.toString ());

        return cmdRes;
      }
    }

    return new CommandResult (ECommandResultType.TYPE_ERROR, "No valid X509 certificates found");
  }

  protected CommandResult importPrivateKey (final IAliasedCertificateFactory certFx,
                                            final String alias,
                                            final String filename,
                                            final String password) throws Exception
  {
    final KeyStore ks = AS2Util.getCryptoHelper ().createNewKeyStore ();
    ks.load (new FileInputStream (filename), password.toCharArray ());

    final Enumeration <String> aliases = ks.aliases ();
    while (aliases.hasMoreElements ())
    {
      final String certAlias = aliases.nextElement ();
      final Certificate cert = ks.getCertificate (certAlias);

      if (cert instanceof X509Certificate)
      {
        certFx.addCertificate (alias, (X509Certificate) cert, true);

        final Key certKey = ks.getKey (certAlias, password.toCharArray ());
        certFx.addPrivateKey (alias, certKey, password);

        return new CommandResult (ECommandResultType.TYPE_OK, "Imported certificate and key: " + cert.toString ());
      }
    }

    return new CommandResult (ECommandResultType.TYPE_ERROR, "No valid X509 certificates found");

  }
}
