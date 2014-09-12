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
package com.helger.as2.util;

import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.as2.XMLSession;
import com.helger.as2lib.IDynamicComponent;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.exception.WrappedOpenAS2Exception;
import com.helger.as2lib.session.ISession;
import com.helger.as2lib.util.StringMap;
import com.helger.as2lib.util.XMLUtil;
import com.helger.commons.lang.GenericReflection;
import com.helger.commons.microdom.IMicroElement;

public final class ServerXMLUtil
{
  private ServerXMLUtil ()
  {}

  @Nonnull
  public static IDynamicComponent createComponent (@Nonnull final IMicroElement aElement,
                                                   @Nonnull final ISession aSession) throws OpenAS2Exception
  {
    final String sClassName = aElement.getAttribute ("classname");
    if (sClassName == null)
      throw new OpenAS2Exception ("Missing classname");

    try
    {
      final IDynamicComponent aObj = GenericReflection.newInstance (sClassName, IDynamicComponent.class);

      final StringMap aParameters = XMLUtil.getAttrsWithLowercaseName (aElement);
      if (aSession instanceof XMLSession)
      {
        // Replace %home% with session base directory
        updateDirectories (((XMLSession) aSession).getBaseDirectory (), aParameters);
      }
      aObj.initDynamicComponent (aSession, aParameters);

      return aObj;
    }
    catch (final Exception e)
    {
      throw new WrappedOpenAS2Exception ("Error creating component: " + sClassName, e);
    }
  }

  public static void updateDirectories (final String baseDirectory, final StringMap attributes) throws OpenAS2Exception
  {
    for (final Map.Entry <String, String> attrEntry : attributes)
    {
      final String value = attrEntry.getValue ();
      if (value.startsWith ("%home%"))
      {
        if (baseDirectory == null)
          throw new OpenAS2Exception ("Base directory isn't set");
        attributes.setAttribute (attrEntry.getKey (), baseDirectory + value.substring (6));
      }
    }
  }
}
