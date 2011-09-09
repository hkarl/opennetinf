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
package netinf.node.access.rest;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.translation.DatamodelTranslator;
import netinf.node.access.AccessServer;
import netinf.node.api.impl.LocalNodeConnection;

import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A RESTAccessServer is a AccessServer providing a RESTful interface to a NetInf node.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class RESTAccessServer implements AccessServer {

   private static final Logger LOG = Logger.getLogger(RESTAccessServer.class);

   private Component component;

   @Inject
   public RESTAccessServer(@Named("node.access.rest.port") int port, LocalNodeConnection connection, DatamodelFactory factory,
         DatamodelTranslator translator) {
      component = new Component();
      component.getServers().add(Protocol.HTTP, port);

      Application application = new RESTApplication(connection, factory, translator);

      component.getDefaultHost().attach(application);
   }

   /**
    * Starts the RESTAccessServer.
    */
   @Override
   public void start() {
      try {
         component.start();
      } catch (Exception e) {
         LOG.error("Could not start RESTAccessServer", e);
      }
   }

   /**
    * Stops the RESTAccessServer.
    */
   @Override
   public void stop() {
      try {
         component.stop();
      } catch (Exception e) {
         LOG.error("Could not stop RESTAccessServer", e);
      }
   }

}
