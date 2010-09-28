/*
 * Copyright (C) 2009,2010 University of Paderborn, Computer Networks Group
 * Copyright (C) 2009,2010 Christian Dannewitz <christian.dannewitz@upb.de>
 * Copyright (C) 2009,2010 Thorsten Biermann <thorsten.biermann@upb.de>
 * 
 * Copyright (C) 2009,2010 Eduard Bauer <edebauer@mail.upb.de>
 * Copyright (C) 2009,2010 Matthias Becker <matzeb@mail.upb.de>
 * Copyright (C) 2009,2010 Frederic Beister <azamir@zitmail.uni-paderborn.de>
 * Copyright (C) 2009,2010 Nadine Dertmann <ndertmann@gmx.de>
 * Copyright (C) 2009,2010 Michael Kionka <mkionka@mail.upb.de>
 * Copyright (C) 2009,2010 Mario Mohr <mmohr@mail.upb.de>
 * Copyright (C) 2009,2010 Felix Steffen <felix.steffen@gmx.de>
 * Copyright (C) 2009,2010 Sebastian Stey <sebstey@mail.upb.de>
 * Copyright (C) 2009,2010 Steffen Weber <stwe@mail.upb.de>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.eventservice.siena;

import java.io.IOException;
import java.util.Properties;

import netinf.common.utils.Utils;
import netinf.eventservice.siena.module.EventServiceSienaModule;

import org.apache.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Launches the EventServiceFramework and Siena
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class StarterEventServiceSiena {
   private static final Logger LOG = Logger.getLogger(StarterEventServiceSiena.class);

   public static final String EVENTSERVICE_SIENA_DEFAULT_PROPERTIES = "../configs_official/eventservicesiena.properties";

   public static void main(String[] args) throws IOException {

      // Starts the whole EventServiceSiena
      String pathToPropertyFile = null;

      if (args.length > 0) {
         pathToPropertyFile = args[0];
      } else {
         pathToPropertyFile = EVENTSERVICE_SIENA_DEFAULT_PROPERTIES;
      }

      System.out.println("Using file '" + pathToPropertyFile + "' for configuration");
      StarterEventServiceSiena eventServiceSienaStarter = new StarterEventServiceSiena(pathToPropertyFile);
      eventServiceSienaStarter.start();
   }

   private final Injector injector;
   private EventServiceSiena eventServiceSiena;

   public StarterEventServiceSiena(String pathToProperties) throws IOException {
      Properties properties = Utils.loadProperties(pathToProperties);

      this.injector = Guice.createInjector(new EventServiceSienaModule(properties));
   }

   private void start() throws IOException {
      LOG.trace(null);

      this.eventServiceSiena = this.injector.getInstance(EventServiceSiena.class);
      this.eventServiceSiena.setup();

      LOG.debug("Event Service Siena successfully started");
   }

}
