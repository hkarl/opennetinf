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
package netinf.node.module.scenario2;

import netinf.access.NetInfServer;
import netinf.access.TCPServer;
import netinf.common.datamodel.rdf.module.DatamodelRdfModule;
import netinf.common.datamodel.translation.module.DatamodelTranslationModule;
import netinf.common.utils.Utils;
import netinf.node.module.AbstractNodeModule;
import netinf.node.resolution.ResolutionInterceptor;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.eventprocessing.EventPublisher;
import netinf.node.resolution.local.LocalResolutionService;
import netinf.node.resolution.local.module.LocalResolutionModule;
import netinf.node.resolution.locator.impl.LocatorSelectorImpl;
import netinf.node.search.SearchService;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.internal.Nullable;

/**
 * Module is used for the resolution service node of scenario 2 (shopping scenario). This node offers a resolution service using a
 * {@link LocalResolutionService}. The node can be accessed via TCP.
 * 
 * @see AbstractNodeModule
 * @author PG Augnet 2, University of Paderborn
 */
public class GlobalRSNodeModule extends AbstractNodeModule {
   public static final String NODE_PROPERTIES = "../configs_official/scenario2/globalRSNode.properties";

   public GlobalRSNodeModule() {
      super(Utils.loadProperties(NODE_PROPERTIES));
   }

   @Override
   protected void configure() {
      super.configure();

      // The datamodel
      install(new DatamodelRdfModule());
      install(new DatamodelTranslationModule());

      // LocalRS
      install(new LocalResolutionModule());
      // install(new LocalResolutionPersistentModule());

   }

   @Singleton
   @Provides
   ResolutionService[] provideResolutionServices(@Nullable EventPublisher ep, LocalResolutionService localResolutionService) {
      if (ep != null) {
         localResolutionService.addEventService(ep);
      }
      return new ResolutionService[] { localResolutionService };
   }

   @Singleton
   @Provides
   ResolutionInterceptor[] provideResolutionInterceptors(LocatorSelectorImpl locatorSelector) {
      return new ResolutionInterceptor[] { locatorSelector };
   }

   @Singleton
   @Provides
   SearchService[] provideSearchServices() {
      return new SearchService[] {};
   }

   @Singleton
   @Provides
   NetInfServer[] providesAccess(TCPServer tcpServer) {
      return new NetInfServer[] { tcpServer };
   }
}
