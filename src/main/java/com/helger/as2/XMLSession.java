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
import java.io.InputStream;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public XMLSession (final String filename) throws OpenAS2Exception
  {
    final File file = new File (filename).getAbsoluteFile ();
    m_sBaseDirectory = file.getParent ();
    load (FileUtils.getInputStream (file));
  }

  public ICommandRegistry getCommandRegistry ()
  {
    return m_aCommandRegistry;
  }

  public String getBaseDirectory ()
  {
    return m_sBaseDirectory;
  }

  protected void loadCertificates (@Nonnull final IMicroElement aElement) throws OpenAS2Exception
  {
    s_aLogger.info ("  loading certificates");
    final ICertificateFactory certFx = (ICertificateFactory) ServerXMLUtil.createComponent (aElement, this);
    addComponent (ICertificateFactory.COMPID_CERTIFICATE_FACTORY, certFx);
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

  public CommandManager getCommandManager ()
  {
    return m_aCmdManager;
  }

  protected void loadCommandProcessor (@Nonnull final CommandManager aMgr, final IMicroElement aElement) throws OpenAS2Exception
  {
    final AbstractCommandProcessor cmdProcesor = (AbstractCommandProcessor) ServerXMLUtil.createComponent (aElement,
                                                                                                           this);
    aMgr.addProcessor (cmdProcesor);
  }

  protected void loadPartnerships (final IMicroElement rootNode) throws OpenAS2Exception
  {
    s_aLogger.info ("  loading partnerships");
    final IPartnershipFactory partnerFx = (IPartnershipFactory) ServerXMLUtil.createComponent (rootNode, this);
    addComponent (IPartnershipFactory.COMPID_PARTNERSHIP_FACTORY, partnerFx);
  }

  protected void loadProcessor (final IMicroElement rootNode) throws OpenAS2Exception
  {
    s_aLogger.info ("  loading processor");
    final IProcessor proc = (IProcessor) ServerXMLUtil.createComponent (rootNode, this);
    addComponent (IProcessor.COMPID_PROCESSOR, proc);

    for (final IMicroElement module : rootNode.getAllChildElements ("module"))
      loadProcessorModule (proc, module);
  }

  protected void loadProcessorModule (final IProcessor proc, final IMicroElement moduleNode) throws OpenAS2Exception
  {
    final IProcessorModule procmod = (IProcessorModule) ServerXMLUtil.createComponent (moduleNode, this);
    proc.addModule (procmod);
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
}
