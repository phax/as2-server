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
package com.helger.as2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.as2.cmd.CommandManager;
import com.helger.as2.cmd.ICommandRegistry;
import com.helger.as2.cmd.ICommandRegistryFactory;
import com.helger.as2.cmd.processor.AbstractCommandProcessor;
import com.helger.as2.util.ServerXMLUtil;
import com.helger.as2lib.cert.ICertificateFactory;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.exception.WrappedOpenAS2Exception;
import com.helger.as2lib.partner.IPartnershipFactory;
import com.helger.as2lib.processor.IMessageProcessor;
import com.helger.as2lib.processor.module.IProcessorModule;
import com.helger.as2lib.session.Session;
import com.helger.commons.io.file.FileUtils;
import com.helger.commons.microdom.IMicroDocument;
import com.helger.commons.microdom.IMicroElement;
import com.helger.commons.microdom.serialize.MicroReader;

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

  private static final Logger s_aLogger = LoggerFactory.getLogger (XMLSession.class);

  private final String m_sBaseDirectory;
  private final CommandManager m_aCmdManager = CommandManager.getCmdManager ();
  private ICommandRegistry m_aCommandRegistry;

  public XMLSession (final String sFilename) throws OpenAS2Exception
  {
    try
    {
      final File aFile = new File (sFilename).getCanonicalFile ().getAbsoluteFile ();
      m_sBaseDirectory = aFile.getParentFile ().getAbsolutePath ();
      load (FileUtils.getInputStream (aFile));
    }
    catch (final IOException ex)
    {
      throw new WrappedOpenAS2Exception (ex);
    }
  }

  @Nonnull
  public String getBaseDirectory ()
  {
    return m_sBaseDirectory;
  }

  @Nonnull
  public CommandManager getCommandManager ()
  {
    return m_aCmdManager;
  }

  @Nullable
  public ICommandRegistry getCommandRegistry ()
  {
    return m_aCommandRegistry;
  }

  protected void loadCertificates (@Nonnull final IMicroElement aElement) throws OpenAS2Exception
  {
    s_aLogger.info ("  loading certificates");
    final ICertificateFactory certFx = (ICertificateFactory) ServerXMLUtil.createComponent (aElement, this);
    setCertificateFactory (certFx);
  }

  protected void loadCommands (@Nonnull final IMicroElement aElement) throws OpenAS2Exception
  {
    s_aLogger.info ("  loading commands");
    final ICommandRegistry cmdReg = (ICommandRegistry) ServerXMLUtil.createComponent (aElement, this);
    m_aCommandRegistry = cmdReg;
  }

  protected void loadCommandProcessors (@Nonnull final IMicroElement aElement) throws OpenAS2Exception
  {
    final List <IMicroElement> aElements = aElement.getAllChildElements ("commandProcessor");
    s_aLogger.info ("  loading " + aElements.size () + " command processors");
    for (final IMicroElement processor : aElements)
      loadCommandProcessor (m_aCmdManager, processor);
  }

  protected void loadCommandProcessor (@Nonnull final CommandManager aCommandMgr, @Nonnull final IMicroElement aElement) throws OpenAS2Exception
  {
    final AbstractCommandProcessor aCmdProcesor = (AbstractCommandProcessor) ServerXMLUtil.createComponent (aElement,
                                                                                                            this);
    aCommandMgr.addProcessor (aCmdProcesor);
    s_aLogger.info ("    loaded command processor " + aCmdProcesor.getName ());
  }

  protected void loadPartnerships (final IMicroElement eRootNode) throws OpenAS2Exception
  {
    s_aLogger.info ("  loading partnerships");
    final IPartnershipFactory partnerFx = (IPartnershipFactory) ServerXMLUtil.createComponent (eRootNode, this);
    setPartnershipFactory (partnerFx);
  }

  protected void loadMessageProcessor (final IMicroElement eRootNode) throws OpenAS2Exception
  {
    s_aLogger.info ("  loading message processor");
    final IMessageProcessor aMsgProcessor = (IMessageProcessor) ServerXMLUtil.createComponent (eRootNode, this);
    setMessageProcessor (aMsgProcessor);

    for (final IMicroElement eModule : eRootNode.getAllChildElements ("module"))
      loadProcessorModule (aMsgProcessor, eModule);
  }

  protected void loadProcessorModule (@Nonnull final IMessageProcessor aMsgProcessor,
                                      @Nonnull final IMicroElement eModule) throws OpenAS2Exception
  {
    final IProcessorModule aProcessorModule = (IProcessorModule) ServerXMLUtil.createComponent (eModule, this);
    aMsgProcessor.addModule (aProcessorModule);
    s_aLogger.info ("    loaded processor module " + aProcessorModule.getName ());
  }

  protected void load (@Nonnull @WillClose final InputStream aIS) throws OpenAS2Exception
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
          loadMessageProcessor (eRootChild);
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
}
