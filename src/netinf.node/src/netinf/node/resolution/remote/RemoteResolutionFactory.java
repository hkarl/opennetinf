/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.node.resolution.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import netinf.common.communication.SerializeFormat;
import netinf.common.exceptions.NetInfUncheckedException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * A factory for creating RemoteResolution objects.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class RemoteResolutionFactory {

   public static final String REMOTERS_CONFIG_SUFFIX = "remoteRS";

   private static final Logger LOG = Logger.getLogger(RemoteResolutionFactory.class);

   private final String directory;

   private final Provider<RemoteResolutionService> remoteRSProvider;

   @Inject
   public RemoteResolutionFactory(@Named("remoteRS.config.directory") String directory,
         Provider<RemoteResolutionService> remoteRSProvider) {
      this.directory = directory;
      this.remoteRSProvider = remoteRSProvider;
   }

   // TODO: (Ede) Cant we move these things to the configuration file? Where is the problem?
   public List<RemoteResolutionService> getRemoteResolutionServices() {
      File configDir = new File(directory);
      List<RemoteResolutionService> rsServices = new ArrayList<RemoteResolutionService>();
      if (configDir.exists() && configDir.isDirectory()) {
         File[] configFiles = configDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
               if (name.endsWith(REMOTERS_CONFIG_SUFFIX)) {
                  return true;
               }
               return false;
            }
         });
         for (File f : configFiles) {
            Properties rsProps = new Properties();
            FileInputStream fis = null;
            try {
               fis = new FileInputStream(f);
               rsProps.load(fis);
               Integer port = Integer.valueOf(rsProps.getProperty("port"));
               String host = rsProps.getProperty("host");
               if (host == null) {
                  throw new NullPointerException();
               }
               RemoteResolutionService service = remoteRSProvider.get();
               service.setUp(host, port, SerializeFormat.JAVA);

               rsServices.add(service);
            } catch (NullPointerException ex) {
               LOG.warn("Could not load properties for RemoteRS from file " + f.getAbsolutePath(), ex);
            } catch (NumberFormatException ex) {
               LOG.warn("Could not load port for RemoteRS from file " + f.getAbsolutePath(), ex);
            } catch (FileNotFoundException e) {
               LOG.warn("Could not find file " + f.getAbsolutePath(), e);
            } catch (IOException e) {
               LOG.warn("Could not load properties for RemoteRS from file " + f.getAbsolutePath(), e);
            } catch (NetInfUncheckedException ex) {
               LOG.warn("Could not connect to RemoteRS ", ex);
            } finally {
               IOUtils.closeQuietly(fis);
            }
         }
      }
      return rsServices;
   }

}
