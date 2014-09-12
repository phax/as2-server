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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.collections.ContainerHelper;

public final class CommandResult
{
  private final ECommandResultType m_eType;
  private final List <Serializable> m_aResults = new ArrayList <Serializable> ();

  public CommandResult (@Nonnull final ECommandResultType eType)
  {
    m_eType = ValueEnforcer.notNull (eType, "Type");
  }

  public CommandResult (@Nonnull final ECommandResultType eType, @Nonnull final String msg)
  {
    this (eType);
    addResult (msg);
  }

  public CommandResult (final Exception e)
  {
    super ();
    m_eType = ECommandResultType.TYPE_EXCEPTION;
    addResult (e);
  }

  @Nonnull
  public ECommandResultType getType ()
  {
    return m_eType;
  }

  public void addResult (@Nonnull final Serializable aResult)
  {
    ValueEnforcer.notNull (aResult, "Result");
    m_aResults.add (aResult);
  }

  public boolean hasNoResult ()
  {
    return m_aResults.isEmpty ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <Serializable> getAllResults ()
  {
    return ContainerHelper.newList (m_aResults);
  }

  @Nonnull
  public String getResultAsString ()
  {
    final StringBuilder results = new StringBuilder ();
    for (final Serializable x : m_aResults)
      results.append (x.toString ()).append ("\r\n");
    return results.toString ();
  }

  @Override
  public String toString ()
  {
    final StringBuilder buf = new StringBuilder ();
    buf.append (m_eType.getText ()).append (":\r\n");
    for (final Serializable aResult : m_aResults)
      buf.append (aResult.toString ()).append ("\r\n");
    return buf.toString ();
  }

  public String toXML ()
  {
    final StringBuilder buf = new StringBuilder ();
    for (final Serializable x : m_aResults)
    {
      buf.append ("<result>");
      buf.append (x.toString ());
      buf.append ("</result>");
    }
    return buf.toString ();
  }
}
