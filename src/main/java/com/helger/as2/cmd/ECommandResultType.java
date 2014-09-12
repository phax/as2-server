package com.helger.as2.cmd;

import javax.annotation.Nonnull;

import com.helger.commons.annotations.Nonempty;
import com.helger.commons.state.ISuccessIndicator;

public enum ECommandResultType implements ISuccessIndicator
{
  TYPE_OK ("OK"),
  TYPE_ERROR ("ERROR"),
  TYPE_WARNING ("WARNING"),
  TYPE_INVALID_PARAM_COUNT ("INVALID PARAMETER COUNT"),
  TYPE_COMMAND_NOT_SUPPORTED ("COMMAND NOT SUPPORTED"),
  TYPE_EXCEPTION ("EXCEPTION");

  private final String m_sText;

  private ECommandResultType (@Nonnull @Nonempty final String sText)
  {
    m_sText = sText;
  }

  public boolean isSuccess ()
  {
    return this == TYPE_OK;
  }

  public boolean isFailure ()
  {
    return !isSuccess ();
  }

  @Nonnull
  @Nonempty
  public String getText ()
  {
    return m_sText;
  }
}
