/**
 * Copyright (C) 2014 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.peppol.as2server.servlet;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;

import com.helger.as2servlet.AS2PeppolReceiveServlet;
import com.helger.commons.string.StringHelper;

/**
 * Special version of the {@link AS2PeppolReceiveServlet} customizing the path
 * to the AS2 data file
 *
 * @author Philip Helger
 */
public class MyAS2PeppolReceiveServlet extends AS2PeppolReceiveServlet
{
  @Override
  @Nonnull
  protected File getConfigurationFile () throws ServletException
  {
    // Customize the path - e.g. read from a config file
    final String sConfigurationFilename = "as2-server-data/as2-server-config.xml";
    if (StringHelper.hasNoText (sConfigurationFilename))
      throw new ServletException ("Configuration filename is missing or empty!");

    try
    {
      final File aFile = new File (sConfigurationFilename).getCanonicalFile ();
      return aFile;
    }
    catch (final IOException ex)
    {
      throw new ServletException ("Failed to get the canonical file from '" + sConfigurationFilename + "'");
    }
  }
}