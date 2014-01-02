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
package com.helger.as2.cmd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommandResult
{
  public static final String TYPE_OK = "OK";
  public static final String TYPE_ERROR = "ERROR";
  public static final String TYPE_WARNING = "WARNING";
  public static final String TYPE_INVALID_PARAM_COUNT = "INVALID PARAMETER COUNT";
  public static final String TYPE_COMMAND_NOT_SUPPORTED = "COMMAND NOT SUPPORTED";
  public static final String TYPE_EXCEPTION = "EXCEPTION";

  private String m_sType;
  private List <Serializable> m_aResults;

  public CommandResult (final String type, final String msg)
  {
    super ();
    m_sType = type;
    getResults ().add (msg);
  }

  public CommandResult (final String type)
  {
    super ();
    m_sType = type;
  }

  public CommandResult (final Exception e)
  {
    super ();
    m_sType = TYPE_EXCEPTION;
    getResults ().add (e);
  }

  public List <Serializable> getResults ()
  {
    if (m_aResults == null)
      m_aResults = new ArrayList <Serializable> ();
    return m_aResults;
  }

  public String getResult ()
  {
    final StringBuilder results = new StringBuilder ();
    for (final Serializable x : getResults ())
      results.append (x.toString ()).append ("\r\n");
    return results.toString ();
  }

  public void setResults (final List <Serializable> list)
  {
    m_aResults = list;
  }

  public String getType ()
  {
    return m_sType;
  }

  @Override
  public String toString ()
  {
    final StringBuilder buf = new StringBuilder ();
    buf.append (getType ()).append (":\r\n");
    for (final Serializable x : getResults ())
      buf.append (x.toString ()).append ("\r\n");
    return buf.toString ();
  }

  public String toXML ()
  {
    final StringBuilder buf = new StringBuilder ();
    for (final Serializable x : getResults ())
    {
      buf.append ("<result>");
      buf.append (x.toString ());
      buf.append ("</result>");
    }
    return buf.toString ();
  }

  public void setType (final String string)
  {
    m_sType = string;
  }
}
