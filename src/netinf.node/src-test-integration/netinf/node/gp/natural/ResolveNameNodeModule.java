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
package netinf.node.gp.natural;

import netinf.access.NetInfServer;
import netinf.access.TCPServer;
import netinf.common.datamodel.rdf.module.DatamodelRdfModule;
import netinf.common.datamodel.translation.module.DatamodelTranslationModule;
import netinf.common.utils.Utils;
import netinf.node.api.NetInfNode;
import netinf.node.gp.module.GPConnectionModule;
import netinf.node.gp.module.GPNodeCapabilityModule;
import netinf.node.gp.module.GPResolveModule;
import netinf.node.module.AbstractNodeModule;
import netinf.node.resolution.ResolutionInterceptor;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.remote.gp.GPResolutionService;
import netinf.node.search.SearchService;
import netinf.node.transfer.TransferService;
import netinf.node.transfer.gp.TransferServiceGP;
import netinf.node.transfer.module.TransferModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * This is a {@link ResolveNameNodeModule}, which belongs to a node that searches a remote node. The other {@link NetInfNode} must
 * have particular capabilitites.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class ResolveNameNodeModule extends AbstractNodeModule {
   public static final String NODE_PROPERTIES = "../netinf.node/src-test-integration/netinf/node/gp/natural/"
         + "resolveName.properties";

   public ResolveNameNodeModule() {
      super(Utils.loadProperties(NODE_PROPERTIES));
   }

   @Override
   protected void configure() {
      super.configure();

      // The datamodel
      install(new DatamodelRdfModule());
      install(new DatamodelTranslationModule());

      // Transfer things
      install(new TransferModule());

      install(new GPConnectionModule());
      install(new GPResolveModule());

      install(new GPNodeCapabilityModule(getProperties()));
   }

   @Singleton
   @Provides
   ResolutionService[] provideResolutionServices(GPResolutionService gpResolutionService) {
      return new ResolutionService[] { gpResolutionService };
   }

   @Singleton
   @Provides
   ResolutionInterceptor[] provideResolutionInterceptors() {
      return new ResolutionInterceptor[] {};
   }

   @Singleton
   @Provides
   SearchService[] provideSearchServices() {
      return new SearchService[] {};
   }

   @Singleton
   @Provides
   TransferService[] provideTransferServices(TransferServiceGP transferServiceGP) {
      return new TransferService[] { transferServiceGP };
   }

   @Singleton
   @Provides
   NetInfServer[] providesAccess(TCPServer tcpServer) {
      return new NetInfServer[] { tcpServer };
   }
}
