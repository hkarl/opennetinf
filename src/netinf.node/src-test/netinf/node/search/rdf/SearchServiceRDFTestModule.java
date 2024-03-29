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
package netinf.node.search.rdf;

import java.util.Properties;

import netinf.common.eventservice.AbstractMessageProcessor;
import netinf.node.resolution.ResolutionController;
import netinf.node.resolution.ResolutionInterceptor;
import netinf.node.resolution.ResolutionServiceSelector;
import netinf.node.resolution.impl.ResolutionControllerImpl;
import netinf.node.resolution.impl.SimpleResolutionServiceSelector;
import netinf.node.resolution.locator.LocatorCostProvider;
import netinf.node.resolution.locator.impl.LocatorSelectorImpl;
import netinf.node.resolution.locator.impl.RandomCostProvider;
import netinf.node.resolution.rdf.RDFResolutionServiceTestModule;
import netinf.node.search.SearchController;
import netinf.node.search.esfconnector.SearchEsfMessageProcessor;
import netinf.node.search.impl.SearchControllerImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Module for the {@link SearchServiceRDFTest}
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class SearchServiceRDFTestModule extends AbstractModule {

   private final Properties properties;

   public SearchServiceRDFTestModule(final Properties properties) {
      this.properties = properties;
   }

   @Override
   protected void configure() {
      install(new RDFResolutionServiceTestModule(properties));

      bind(LocatorCostProvider.class).to(RandomCostProvider.class);
      bind(ResolutionServiceSelector.class).to(SimpleResolutionServiceSelector.class).in(Singleton.class);
      bind(ResolutionController.class).to(ResolutionControllerImpl.class).in(Singleton.class);

      bind(SearchController.class).to(SearchControllerImpl.class);
      bind(AbstractMessageProcessor.class).to(SearchEsfMessageProcessor.class);
   }

   @Singleton
   @Provides
   ResolutionInterceptor[] provideResolutionInterceptors(LocatorSelectorImpl locatorSelector) {
      return new ResolutionInterceptor[] { locatorSelector };
   }

}
