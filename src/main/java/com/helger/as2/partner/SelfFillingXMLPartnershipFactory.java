package com.helger.as2.partner;

import javax.annotation.Nonnull;

import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.exception.PartnershipNotFoundException;
import com.helger.as2lib.partner.CPartnershipIDs;
import com.helger.as2lib.partner.Partnership;

/**
 * A special {@link XMLPartnershipFactory} that adds a new partnership if it is
 * not yet exsting.
 * 
 * @author Philip Helger
 */
public class SelfFillingXMLPartnershipFactory extends XMLPartnershipFactory
{
  @Override
  @Nonnull
  public Partnership getPartnership (@Nonnull final Partnership aPartnership) throws OpenAS2Exception
  {
    try
    {
      return super.getPartnership (aPartnership);
    }
    catch (final PartnershipNotFoundException ex)
    {
      // Ensure the X509 key is contained for certificate store alias retrieval
      if (!aPartnership.containsSenderID (CPartnershipIDs.PID_X509_ALIAS))
        aPartnership.setSenderID (CPartnershipIDs.PID_X509_ALIAS, aPartnership.getSenderID (CPartnershipIDs.PID_AS2));
      if (!aPartnership.containsReceiverID (CPartnershipIDs.PID_X509_ALIAS))
        aPartnership.setReceiverID (CPartnershipIDs.PID_X509_ALIAS,
                                    aPartnership.getReceiverID (CPartnershipIDs.PID_AS2));

      // Create a new one
      addPartnership (aPartnership);
      return aPartnership;
    }
  }
}
