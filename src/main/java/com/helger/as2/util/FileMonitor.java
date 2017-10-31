/**
 * The FreeBSD Copyright
 * Copyright 1994-2008 The FreeBSD Project. All rights reserved.
 * Copyright (C) 2013-2017 Philip Helger philip[at]helger[dot]com
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

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;

public class FileMonitor
{
  private ICommonsList <IFileMonitorListener> m_aListeners;
  private Date m_aLastModified;
  private File m_aFile;
  private Timer m_aTimer;
  private boolean m_bBusy;
  private int m_nInterval;

  public FileMonitor (final File file, final int interval)
  {
    super ();
    m_aFile = file;
    m_nInterval = interval;
    start ();
  }

  public void setBusy (final boolean busy)
  {
    m_bBusy = busy;
  }

  public boolean isBusy ()
  {
    return m_bBusy;
  }

  public void setFile (final File file)
  {
    m_aFile = file;
  }

  public File getFile ()
  {
    return m_aFile;
  }

  public String getFilename ()
  {
    if (getFile () != null)
    {
      return getFile ().getAbsolutePath ();
    }

    return null;
  }

  public void setInterval (final int interval)
  {
    m_nInterval = interval;
    restart ();
  }

  public int getInterval ()
  {
    return m_nInterval;
  }

  public void setLastModified (final Date lastModified)
  {
    m_aLastModified = lastModified;
  }

  public Date getLastModified ()
  {
    return m_aLastModified;
  }

  public void setListeners (@Nullable final ICommonsList <IFileMonitorListener> listeners)
  {
    m_aListeners = listeners;
  }

  public ICommonsList <IFileMonitorListener> getListeners ()
  {
    if (m_aListeners == null)
      m_aListeners = new CommonsArrayList <> ();
    return m_aListeners;
  }

  public void addListener (final IFileMonitorListener listener)
  {
    getListeners ().add (listener);
  }

  public void restart ()
  {
    stop ();
    start ();
  }

  public void start ()
  {
    m_aTimer = getTimer ();
    m_aTimer.scheduleAtFixedRate (new TimerTick (), 0, getInterval () * 1000);
  }

  public void stop ()
  {
    if (m_aTimer != null)
    {
      m_aTimer.cancel ();
    }
  }

  protected boolean isModified ()
  {
    final Date lastModified = getLastModified ();

    if (lastModified != null)
    {
      final Date currentModified = new Date (getFile ().lastModified ());

      return !currentModified.equals (getLastModified ());
    }
    updateModified ();
    return false;
  }

  @Nonnull
  protected Timer getTimer ()
  {
    if (m_aTimer == null)
      m_aTimer = new Timer (true);
    return m_aTimer;
  }

  protected void updateListeners ()
  {
    if (isModified ())
    {
      updateModified ();
      updateListeners (IFileMonitorListener.EVENT_MODIFIED);
    }
  }

  protected void updateListeners (final int eventID)
  {
    final List <IFileMonitorListener> listeners = getListeners ();
    for (final IFileMonitorListener iFileMonitorListener : listeners)
      iFileMonitorListener.handle (this, getFile (), eventID);
  }

  protected void updateModified ()
  {
    setLastModified (new Date (getFile ().lastModified ()));
  }

  private class TimerTick extends TimerTask
  {
    @Override
    public void run ()
    {
      if (!isBusy ())
      {
        setBusy (true);
        updateListeners ();
        setBusy (false);
      }
      else
      {
        updateListeners (IFileMonitorListener.EVENT_MISSED_TICK);
      }
    }
  }
}
