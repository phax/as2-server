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
package com.helger.as2.cmd;

import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.as2.util.ServerXMLUtil;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.session.IAS2Session;
import com.helger.as2lib.util.IStringMap;
import com.helger.as2lib.util.XMLUtil;
import com.helger.commons.io.file.FileUtils;
import com.helger.commons.microdom.IMicroDocument;
import com.helger.commons.microdom.IMicroElement;
import com.helger.commons.microdom.serialize.MicroReader;

public class XMLCommandRegistry extends BaseCommandRegistry
{
  public static final String ATTR_FILENAME = "filename";

  @Override
  public void initDynamicComponent (@Nonnull final IAS2Session session, @Nullable final IStringMap parameters) throws OpenAS2Exception
  {
    super.initDynamicComponent (session, parameters);

    refresh ();
  }

  public void load (final InputStream in) throws OpenAS2Exception
  {
    final IMicroDocument document = MicroReader.readMicroXML (in);
    final IMicroElement root = document.getDocumentElement ();

    clearCommands ();

    for (final IMicroElement rootNode : root.getAllChildElements ())
    {
      final String nodeName = rootNode.getTagName ();
      if (nodeName.equals ("command"))
      {
        loadCommand (rootNode, null);
      }
      else
        if (nodeName.equals ("multicommand"))
        {
          loadMultiCommand (rootNode, null);
        }
    }
  }

  public void refresh () throws OpenAS2Exception
  {
    load (FileUtils.getInputStream (getAttributeAsStringRequired (ATTR_FILENAME)));
  }

  protected void loadCommand (final IMicroElement node, final MultiCommand parent) throws OpenAS2Exception
  {
    final ICommand cmd = (ICommand) ServerXMLUtil.createComponent (node, getSession ());

    if (parent != null)
    {
      parent.getCommands ().add (cmd);
    }
    else
    {
      addCommand (cmd);
    }
  }

  protected void loadMultiCommand (@Nonnull final IMicroElement node, @Nullable final MultiCommand parent) throws OpenAS2Exception
  {
    final MultiCommand cmd = new MultiCommand ();
    cmd.initDynamicComponent (getSession (), XMLUtil.getAttrsWithLowercaseName (node));

    if (parent != null)
      parent.getCommands ().add (cmd);
    else
      addCommand (cmd);

    for (final IMicroElement childNode : node.getAllChildElements ())
    {
      final String sChildName = childNode.getNodeName ();

      if (sChildName.equals ("command"))
        loadCommand (childNode, cmd);
      else
        if (sChildName.equals ("multicommand"))
          loadMultiCommand (childNode, cmd);
    }
  }
}
