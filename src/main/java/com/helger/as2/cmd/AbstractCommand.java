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

import java.util.Map;

import com.helger.as2lib.AbstractBaseComponent;
import com.helger.as2lib.ISession;
import com.helger.as2lib.exception.OpenAS2Exception;

public abstract class AbstractCommand extends AbstractBaseComponent implements ICommand
{
  public static final String PARAM_NAME = "name";
  public static final String PARAM_DESCRIPTION = "description";
  public static final String PARAM_USAGE = "usage";

  @Override
  public void initDynamicComponent (final ISession session, final Map <String, String> parameters) throws OpenAS2Exception
  {
    super.initDynamicComponent (session, parameters);
    if (getName () == null)
      setName (getDefaultName ());
    if (getDescription () == null)
      setDescription (getDefaultDescription ());
    if (getUsage () == null)
      setUsage (getDefaultUsage ());
  }

  public String getDescription ()
  {
    return getParameterNotRequired (PARAM_DESCRIPTION);
  }

  @Override
  public String getName ()
  {
    return getParameterNotRequired (PARAM_NAME);
  }

  public String getUsage ()
  {
    return getParameterNotRequired (PARAM_USAGE);
  }

  public abstract String getDefaultName ();

  public abstract String getDefaultDescription ();

  public abstract String getDefaultUsage ();

  public abstract CommandResult execute (Object [] params);

  public void setDescription (final String desc)
  {
    setParameter (PARAM_DESCRIPTION, desc);
  }

  public void setName (final String name)
  {
    setParameter (PARAM_NAME, name);
  }

  public void setUsage (final String usage)
  {
    setParameter (PARAM_USAGE, usage);
  }
}
