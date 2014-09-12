/**
 * The FreeBSD Copyright
 * Copyright 1994-2008 The FreeBSD Project. All rights reserved.
 * Copyright (C) 2013-2014 Philip Helger philip[at]helger[dot]com
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
package com.helger.as2.app.partner;

import com.helger.as2.cmd.CommandResult;
import com.helger.as2.cmd.ECommandResultType;
import com.helger.as2.partner.XMLPartnershipFactory;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.partner.IPartnershipFactory;
import com.helger.as2lib.partner.Partnership;
import com.helger.commons.microdom.IMicroDocument;
import com.helger.commons.microdom.IMicroElement;
import com.helger.commons.microdom.impl.MicroDocument;

/**
 * adds a new partnership entry in partneship store
 *
 * @author joseph mcverry
 */
public class AddPartnershipCommand extends AbstractAliasedPartnershipsCommand
{
  @Override
  public String getDefaultDescription ()
  {
    return "Add a new partnership definition to partnership store.";
  }

  @Override
  public String getDefaultName ()
  {
    return "add";
  }

  @Override
  public String getDefaultUsage ()
  {
    return "add name senderId receiverId <attribute 1=value 1> <attribute 2=value 2> ... <attribute n=value n>";
  }

  @Override
  public CommandResult execute (final IPartnershipFactory partFx, final Object [] params) throws OpenAS2Exception
  {
    if (params.length < 3)
      return new CommandResult (ECommandResultType.TYPE_INVALID_PARAM_COUNT, getUsage ());

    synchronized (partFx)
    {
      final IMicroDocument doc = new MicroDocument ();
      final IMicroElement root = doc.appendElement ("partnership");

      for (int nIndex = 0; nIndex < params.length; nIndex++)
      {
        final String param = (String) params[nIndex];
        final int pos = param.indexOf ('=');
        if (nIndex == 0)
        {
          root.setAttribute ("name", param);
        }
        else
          if (nIndex == 1)
          {
            final IMicroElement elem = root.appendElement ("sender");
            elem.setAttribute ("name", param);
          }
          else
            if (nIndex == 2)
            {
              final IMicroElement elem = root.appendElement ("receiver");
              elem.setAttribute ("name", param);
            }
            else
              if (pos == 0)
              {
                return new CommandResult (ECommandResultType.TYPE_ERROR, "incoming parameter missing name");
              }
              else
                if (pos > 0)
                {
                  final IMicroElement elem = root.appendElement ("attribute");
                  elem.setAttribute ("name", param.substring (0, pos));
                  elem.setAttribute ("value", param.substring (pos + 1));
                }
                else
                  return new CommandResult (ECommandResultType.TYPE_ERROR, "incoming parameter missing value");

      }

      final Partnership aPartnership = ((XMLPartnershipFactory) partFx).loadPartnership (root,
                                                                                         partFx.getPartnerMap (),
                                                                                         partFx.getAllPartnerships ());
      // add the partnership to the list of available partnerships
      partFx.addPartnership (aPartnership);

      return new CommandResult (ECommandResultType.TYPE_OK);
    }
  }
}
