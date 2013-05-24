/**
 * The FreeBSD Copyright
 * Copyright 1994-2008 The FreeBSD Project. All rights reserved.
 * Copyright (C) 2013 Philip Helger ph[at]phloc[dot]com
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
package com.helger.as2.partner;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.as2.util.FileMonitor;
import com.helger.as2.util.IFileMonitorListener;
import com.helger.as2lib.ISession;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.exception.WrappedException;
import com.helger.as2lib.params.InvalidParameterException;
import com.helger.as2lib.partner.AbstractPartnershipFactory;
import com.helger.as2lib.partner.Partnership;
import com.helger.as2lib.util.XMLUtil;
import com.phloc.commons.microdom.IMicroDocument;
import com.phloc.commons.microdom.IMicroElement;
import com.phloc.commons.microdom.impl.MicroDocument;
import com.phloc.commons.microdom.serialize.MicroReader;
import com.phloc.commons.microdom.serialize.MicroWriter;
import com.phloc.commons.string.StringHelper;

/**
 * original author unknown this release added logic to store partnerships and
 * provide methods for partner/partnership command line processor
 * 
 * @author joseph mcverry
 */
public class XMLPartnershipFactory extends AbstractPartnershipFactory implements IRefreshablePartnershipFactory, IFileMonitorListener
{
  public static final String PARAM_FILENAME = "filename";
  public static final String PARAM_INTERVAL = "interval";
  private static final Logger s_aLogger = LoggerFactory.getLogger (XMLPartnershipFactory.class);

  private FileMonitor m_aFileMonitor;
  private Map <String, Map <String, String>> m_aPartners;

  public void setFileMonitor (final FileMonitor fileMonitor)
  {
    m_aFileMonitor = fileMonitor;
  }

  public FileMonitor getFileMonitor () throws InvalidParameterException
  {
    boolean bCreateMonitor = m_aFileMonitor == null && getParameterNotRequired (PARAM_INTERVAL) != null;

    if (!bCreateMonitor && (m_aFileMonitor != null))
    {
      final String filename = m_aFileMonitor.getFilename ();
      bCreateMonitor = ((filename != null) && !filename.equals (getFilename ()));
    }

    if (bCreateMonitor)
    {
      if (m_aFileMonitor != null)
        m_aFileMonitor.stop ();

      final int interval = getParameterInt (PARAM_INTERVAL);
      final File file = new File (getFilename ());
      m_aFileMonitor = new FileMonitor (file, interval);
      m_aFileMonitor.addListener (this);
    }

    return m_aFileMonitor;
  }

  public void setFilename (final String filename)
  {
    getParameters ().put (PARAM_FILENAME, filename);
  }

  public String getFilename () throws InvalidParameterException
  {
    return getParameterRequired (PARAM_FILENAME);
  }

  public void setPartners (final Map <String, Map <String, String>> map)
  {
    m_aPartners = map;
  }

  public Map <String, Map <String, String>> getPartners ()
  {
    if (m_aPartners == null)
    {
      m_aPartners = new HashMap <String, Map <String, String>> ();
    }

    return m_aPartners;
  }

  public void handle (final FileMonitor monitor, final File file, final int eventID)
  {
    switch (eventID)
    {
      case IFileMonitorListener.EVENT_MODIFIED:

        try
        {
          refresh ();
          s_aLogger.debug ("- Partnerships Reloaded -");
        }
        catch (final OpenAS2Exception oae)
        {
          oae.terminate ();
        }

        break;
    }
  }

  @Override
  public void initDynamicComponent (final ISession session, final Map <String, String> parameters) throws OpenAS2Exception
  {
    super.initDynamicComponent (session, parameters);

    refresh ();
  }

  public void refresh () throws OpenAS2Exception
  {
    try
    {
      load (new FileInputStream (getFilename ()));

      getFileMonitor ();
    }
    catch (final Exception e)
    {
      throw new WrappedException (e);
    }
  }

  protected void load (final InputStream in) throws OpenAS2Exception
  {
    final IMicroDocument document = MicroReader.readMicroXML (in);
    final IMicroElement root = document.getDocumentElement ();

    final Map <String, Map <String, String>> newPartners = new HashMap <String, Map <String, String>> ();
    final List <Partnership> newPartnerships = new ArrayList <Partnership> ();

    for (final IMicroElement rootNode : root.getAllChildElements ())
    {
      final String nodeName = rootNode.getTagName ();

      if (nodeName.equals ("partner"))
      {
        loadPartner (newPartners, rootNode);
      }
      else
        if (nodeName.equals ("partnership"))
        {
          loadPartnership (newPartners, newPartnerships, rootNode);
        }
    }

    synchronized (this)
    {
      setPartners (newPartners);
      setPartnerships (newPartnerships);
    }
  }

  protected void loadAttributes (final IMicroElement node, final Partnership partnership) throws OpenAS2Exception
  {
    final Map <String, String> nodes = XMLUtil.mapAttributeNodes (node, "attribute", "name", "value");
    partnership.getAttributes ().putAll (nodes);
  }

  public void loadPartner (final Map <String, Map <String, String>> partners, final IMicroElement node) throws OpenAS2Exception
  {
    final Map <String, String> newPartner = XMLUtil.getAttrsWithLowercaseNameWithRequired (node, "name");
    final String name = newPartner.get ("name");

    if (partners.get (name) != null)
    {
      throw new OpenAS2Exception ("Partner is defined more than once: " + name);
    }

    partners.put (name, newPartner);
  }

  protected void loadPartnerIDs (final Map <String, Map <String, String>> partners,
                                 final String partnershipName,
                                 final IMicroElement partnershipNode,
                                 final String partnerType,
                                 final Map <String, String> idMap) throws OpenAS2Exception
  {
    final IMicroElement partnerNode = partnershipNode.getFirstChildElement (partnerType);

    if (partnerNode == null)
    {
      throw new OpenAS2Exception ("Partnership " + partnershipName + " is missing sender");
    }

    final Map <String, String> partnerAttr = XMLUtil.getAttrsWithLowercaseName (partnerNode);

    // check for a partner name, and look up in partners list if one is found
    final String partnerName = partnerAttr.get ("name");
    if (partnerName != null)
    {
      final Map <String, String> partner = partners.get (partnerName);
      if (partner == null)
      {
        throw new OpenAS2Exception ("Partnership " +
                                    partnershipName +
                                    " has an undefined " +
                                    partnerType +
                                    ": " +
                                    partnerName);
      }

      idMap.putAll (partner);
    }

    // copy all other attributes to the partner id map
    idMap.putAll (partnerAttr);
  }

  public void loadPartnership (final Map <String, Map <String, String>> partners,
                               final List <Partnership> partnerships,
                               final IMicroElement node) throws OpenAS2Exception
  {
    final Partnership partnership = new Partnership ();

    final Map <String, String> psAttributes = XMLUtil.getAttrsWithLowercaseNameWithRequired (node, "name");
    final String name = psAttributes.get ("name");

    if (getPartnership (partnerships, name) != null)
    {
      throw new OpenAS2Exception ("Partnership is defined more than once: " + name);
    }

    partnership.setName (name);

    // load the sender and receiver information
    loadPartnerIDs (partners, name, node, "sender", partnership.getSenderIDs ());
    loadPartnerIDs (partners, name, node, "receiver", partnership.getReceiverIDs ());

    // read in the partnership attributes
    loadAttributes (node, partnership);

    // add the partnership to the list of available partnerships
    partnerships.add (partnership);
  }

  public void storePartnership () throws OpenAS2Exception
  {
    final String fn = getFilename ();

    long nIndex = 0;
    File f;
    do
    {
      f = new File (fn + '.' + StringHelper.getLeadingZero (nIndex, 7));
      nIndex++;
    } while (f.exists ());

    s_aLogger.info ("backing up " + fn + " to " + f.getName ());

    final File fr = new File (fn);
    fr.renameTo (f);

    final IMicroDocument aDoc = new MicroDocument ();
    final IMicroElement ePartnerships = aDoc.appendElement ("partnerships");
    for (final Map <String, String> aAttrs : m_aPartners.values ())
    {
      final IMicroElement ePartner = ePartnerships.appendElement ("partner");
      for (final Map.Entry <String, String> aAttr : aAttrs.entrySet ())
        ePartner.setAttribute (aAttr.getKey (), aAttr.getValue ());
    }

    for (final Partnership partnership : getPartnerships ())
    {
      final IMicroElement ePartnership = ePartnerships.appendElement ("partnership");
      ePartnership.setAttribute ("name", partnership.getName ());

      final IMicroElement eSender = ePartnership.appendElement ("sender");
      eSender.setAttribute ("name", partnership.getSenderIDs ().get ("name"));

      final IMicroElement eReceiver = ePartnership.appendElement ("receiver");
      eReceiver.setAttribute ("name", partnership.getReceiverIDs ().get ("name"));

      for (final Map.Entry <String, String> aAttr : partnership.getAttributes ().entrySet ())
        ePartnership.appendElement ("attribute")
                    .setAttribute ("name", aAttr.getKey ())
                    .setAttribute ("value", aAttr.getValue ());
    }
    if (MicroWriter.writeToFile (aDoc, new File (fn)).isFailure ())
      throw new WrappedException ("Failed to write to file " + fn);
  }
}
