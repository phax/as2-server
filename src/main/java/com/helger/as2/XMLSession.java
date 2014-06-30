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
package com.helger.as2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import com.helger.as2.cmd.CommandManager;
import com.helger.as2.cmd.ICommandRegistry;
import com.helger.as2.cmd.ICommandRegistryFactory;
import com.helger.as2.cmd.processor.AbstractCommandProcessor;
import com.helger.as2.util.ServerXMLUtil;
import com.helger.as2lib.Session;
import com.helger.as2lib.cert.ICertificateFactory;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.partner.IPartnershipFactory;
import com.helger.as2lib.processor.IProcessor;
import com.helger.as2lib.processor.module.IProcessorModule;
import com.phloc.commons.io.streams.StreamUtils;
import com.phloc.commons.microdom.IMicroDocument;
import com.phloc.commons.microdom.IMicroElement;
import com.phloc.commons.microdom.serialize.MicroReader;

/**
 * original author unknown in this release added command registry methods
 * 
 * @author joseph mcverry
 */
public class XMLSession extends Session implements ICommandRegistryFactory
{
  public static final String EL_CERTIFICATES = "certificates";
  public static final String EL_CMDPROCESSOR = "commandProcessors";
  public static final String EL_PROCESSOR = "processor";
  public static final String EL_PARTNERSHIPS = "partnerships";
  public static final String EL_COMMANDS = "commands";
  public static final String PARAM_BASE_DIRECTORY = "basedir";

  private ICommandRegistry commandRegistry;
  private String baseDirectory;
  private CommandManager cmdManager;

  public XMLSession (final InputStream in) throws OpenAS2Exception
  {
    super ();
    load (in);
  }

  public XMLSession (final String filename) throws OpenAS2Exception, IOException
  {
    final File file = new File (filename).getAbsoluteFile ();
    setBaseDirectory (file.getParent ());
    final FileInputStream fin = new FileInputStream (file);
    try
    {
      load (fin);
    }
    finally
    {
      StreamUtils.close (fin);
    }
  }

  public void setCommandRegistry (final ICommandRegistry registry)
  {
    commandRegistry = registry;
  }

  public ICommandRegistry getCommandRegistry ()
  {
    return commandRegistry;
  }

  protected void load (@Nonnull final InputStream aIS) throws OpenAS2Exception
  {
    final IMicroDocument aDoc = MicroReader.readMicroXML (aIS);
    final IMicroElement eRoot = aDoc.getDocumentElement ();

    for (final IMicroElement eRootChild : eRoot.getAllChildElements ())
    {
      final String sNodeName = eRootChild.getTagName ();

      if (sNodeName.equals (EL_CERTIFICATES))
        loadCertificates (eRootChild);
      else
        if (sNodeName.equals (EL_PROCESSOR))
          loadProcessor (eRootChild);
        else
          if (sNodeName.equals (EL_CMDPROCESSOR))
            loadCommandProcessors (eRootChild);
          else
            if (sNodeName.equals (EL_PARTNERSHIPS))
              loadPartnerships (eRootChild);
            else
              if (sNodeName.equals (EL_COMMANDS))
                loadCommands (eRootChild);
              else
                throw new OpenAS2Exception ("Undefined tag: " + sNodeName);
    }
  }

  protected void loadCertificates (final IMicroElement aElement) throws OpenAS2Exception
  {
    final ICertificateFactory certFx = (ICertificateFactory) ServerXMLUtil.createComponent (aElement, this);
    setComponent (ICertificateFactory.COMPID_CERTIFICATE_FACTORY, certFx);
  }

  protected void loadCommands (final IMicroElement aElement) throws OpenAS2Exception
  {
    final ICommandRegistry cmdReg = (ICommandRegistry) ServerXMLUtil.createComponent (aElement, this);
    setCommandRegistry (cmdReg);
  }

  protected void loadCommandProcessors (final IMicroElement aElement) throws OpenAS2Exception
  {
    cmdManager = CommandManager.getCmdManager ();

    for (final IMicroElement processor : aElement.getAllChildElements ("commandProcessor"))
      loadCommandProcessor (cmdManager, processor);
  }

  public CommandManager getCommandManager ()
  {
    return cmdManager;
  }

  protected void loadCommandProcessor (@Nonnull final CommandManager aMgr, final IMicroElement aElement) throws OpenAS2Exception
  {
    final AbstractCommandProcessor cmdProcesor = (AbstractCommandProcessor) ServerXMLUtil.createComponent (aElement,
                                                                                                           this);
    aMgr.addProcessor (cmdProcesor);
  }

  protected void loadPartnerships (final IMicroElement rootNode) throws OpenAS2Exception
  {
    final IPartnershipFactory partnerFx = (IPartnershipFactory) ServerXMLUtil.createComponent (rootNode, this);
    setComponent (IPartnershipFactory.COMPID_PARTNERSHIP_FACTORY, partnerFx);
  }

  protected void loadProcessor (final IMicroElement rootNode) throws OpenAS2Exception
  {
    final IProcessor proc = (IProcessor) ServerXMLUtil.createComponent (rootNode, this);
    setComponent (IProcessor.COMPID_PROCESSOR, proc);

    for (final IMicroElement module : rootNode.getAllChildElements ("module"))
      loadProcessorModule (proc, module);
  }

  protected void loadProcessorModule (final IProcessor proc, final IMicroElement moduleNode) throws OpenAS2Exception
  {
    final IProcessorModule procmod = (IProcessorModule) ServerXMLUtil.createComponent (moduleNode, this);
    proc.getModules ().add (procmod);
  }

  public String getBaseDirectory ()
  {
    return baseDirectory;
  }

  public void setBaseDirectory (final String dir)
  {
    baseDirectory = dir;
  }

}
