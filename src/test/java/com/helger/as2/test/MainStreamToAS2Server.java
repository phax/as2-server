package com.helger.as2.test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.helger.commons.io.file.FileHelper;
import com.helger.commons.io.stream.StreamHelper;

/**
 * Small main application that writes a fully fledged AS2 message via a socket
 * to the server.
 *
 * @author Philip Helger
 */
public final class MainStreamToAS2Server
{
  public static void main (final String [] args) throws UnknownHostException, IOException
  {
    final File aFile = new File ("src/test/resources/test-messages/issue12-1.txt");
    final byte [] aPayload = StreamHelper.getAllBytes (FileHelper.getInputStream (aFile));

    final Socket aSocket = new Socket ("localhost", 10080);
    try
    {
      final OutputStream out = aSocket.getOutputStream ();
      out.write (aPayload);
      out.close ();
      System.out.println ("Stream payload to AS2 server");
    }
    finally
    {
      aSocket.close ();
    }
  }
}
