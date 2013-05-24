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
package com.helger.as2.app;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.as2.XMLSession;
import com.helger.as2.cmd.CommandManager;
import com.helger.as2.cmd.ICommandRegistry;
import com.helger.as2.cmd.processor.AbstractCommandProcessor;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.util.CInfo;

/**
 * original author unknown in this release added ability to have multiple
 * command processors
 * 
 * @author joseph mcverry
 */
public class MainOpenAS2Server
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (MainOpenAS2Server.class);

  public static void main (final String [] args)
  {
    final MainOpenAS2Server server = new MainOpenAS2Server ();
    server.start (args);
  }

  public void start (final String [] args)
  {
    XMLSession session = null;
    try
    {
      s_aLogger.info (CInfo.NAME_VERSION + " - starting Server...");

      // create the OpenAS2 Session object
      // this is used by all other objects to access global configs and
      // functionality
      s_aLogger.info ("Loading configuration...");

      if (args.length > 0)
      {
        session = new XMLSession (args[0]);
      }
      else
      {
        s_aLogger.info ("Usage:");
        s_aLogger.info ("java " + getClass ().getName () + " <configuration file>");
        throw new Exception ("Missing configuration file");
      }
      // create a command processor

      // get a registry of Command objects, and add Commands for the Session
      s_aLogger.info ("Registering Session to Command Processor...");

      final ICommandRegistry reg = session.getCommandRegistry ();

      // start the active processor modules
      s_aLogger.info ("Starting Active Modules...");
      session.getProcessor ().startActiveModules ();

      // enter the command processing loop
      s_aLogger.info ("OpenAS2 Started");

      final CommandManager cmdMgr = session.getCommandManager ();
      final List <AbstractCommandProcessor> processors = cmdMgr.getProcessors ();
      for (final AbstractCommandProcessor cmd : processors)
      {
        s_aLogger.info ("Loading Command Processor..." + cmd.getClass ().getName () + "");
        cmd.init ();
        cmd.addCommands (reg);
        cmd.start ();
      }

      // Start waiting for termination
      breakOut: while (true)
      {
        for (final AbstractCommandProcessor cmd : processors)
        {
          if (cmd.isTerminated ())
            break breakOut;
          Thread.sleep (100);
        }
      }
      s_aLogger.info ("- OpenAS2 Stopped -");
    }
    catch (final Throwable t)
    {
      t.printStackTrace ();
    }
    finally
    {
      if (session != null)
      {
        try
        {
          session.getProcessor ().stopActiveModules ();
        }
        catch (final OpenAS2Exception same)
        {
          same.terminate ();
        }
      }

      s_aLogger.info ("OpenAS2 has shut down");
    }
  }
}
