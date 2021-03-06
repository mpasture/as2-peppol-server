/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.peppol.as2server.jetty;

import java.io.File;

import javax.annotation.concurrent.Immutable;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.system.SystemProperties;
import com.helger.peppol.utils.ConfigFile;

/**
 * Run as2-peppol-server as a standalone web application in Jetty on port 8080.<br>
 * http://localhost:8080/
 *
 * @author Philip Helger
 */
@Immutable
public final class RunInJettyPEPPOLAS2
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (RunInJettyPEPPOLAS2.class);
  private static final String RESOURCE_PREFIX = "target/webapp-classes";

  public static void main (final String [] args) throws Exception
  {
    run (8080);
  }

  public static void run (final int nPort) throws Exception
  {
    if (System.getSecurityManager () != null)
      throw new IllegalStateException ("Security Manager is set but not supported - aborting!");

    // Proxy configuration is simply applied by setting system properties
    final ConfigFile aCF = new ConfigFile ("private-configProxy.properties", "configProxy.properties");
    for (final String sKey : aCF.getAllKeys ())
    {
      final String sValue = aCF.getString (sKey);
      System.setProperty (sKey, sValue);
      s_aLogger.info ("Setting Proxy property " + sKey + "=" + sValue);
    }

    // Create main server
    final Server aServer = new Server ();
    // Create connector on Port
    final ServerConnector aConnector = new ServerConnector (aServer);
    aConnector.setPort (nPort);
    aConnector.setIdleTimeout (30000);
    aServer.setConnectors (new Connector [] { aConnector });

    final WebAppContext aWebAppCtx = new WebAppContext ();
    aWebAppCtx.setDescriptor (RESOURCE_PREFIX + "/WEB-INF/web.xml");
    aWebAppCtx.setResourceBase (RESOURCE_PREFIX);
    aWebAppCtx.setContextPath ("/");
    aWebAppCtx.setTempDirectory (new File (SystemProperties.getTmpDir () + '/' + RunInJettyPEPPOLAS2.class.getName ()));
    aWebAppCtx.setParentLoaderPriority (true);
    aWebAppCtx.setThrowUnavailableOnStartupException (true);
    aWebAppCtx.setCopyWebInf (true);
    aWebAppCtx.setCopyWebDir (true);
    aServer.setHandler (aWebAppCtx);
    final ServletContextHandler aCtx = aWebAppCtx;

    // Setting final properties
    // Stops the server when ctrl+c is pressed (registers to
    // Runtime.addShutdownHook)
    aServer.setStopAtShutdown (true);
    // Starting shutdown listener thread
    if (nPort == 8080)
      new JettyMonitor ().start ();
    try
    {
      // Starting the engines:
      aServer.start ();
    }
    catch (final Exception ex)
    {
      throw new IllegalStateException ("Failed to start server!", ex);
    }
    finally
    {
      if (aCtx.isFailed ())
      {
        s_aLogger.error ("Failed to start server - stopping server!");
        aServer.stop ();
        s_aLogger.error ("Failed to start server - stopped server!");
      }
      else
        if (!aServer.isFailed ())
        {
          // Running the server!
          aServer.join ();
        }
    }
  }
}
