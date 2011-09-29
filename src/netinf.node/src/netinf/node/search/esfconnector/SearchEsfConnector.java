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
package netinf.node.search.esfconnector;

import java.util.List;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.eventservice.AbstractEsfConnector;
import netinf.common.eventservice.AbstractMessageProcessor;
import netinf.common.eventservice.MessageReceiver;
import netinf.node.resolution.ResolutionController;
import netinf.node.search.rdf.SearchServiceRDF;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Connects the {@link SearchServiceRDF} with an event service. The search service respectively this connector subscribes on
 * specific IO changes.
 * 
 * @author PG Augnet 2, University of Paderborn
 * @see AbstractEsfConnector
 * @see SearchEsfMessageProcessor
 * @see MessageReceiver
 */
public class SearchEsfConnector extends AbstractEsfConnector {

   protected static final Logger LOG = Logger.getLogger(SearchEsfConnector.class);

   private final ResolutionController resController;

   @Inject
   public SearchEsfConnector(final DatamodelFactory dmFactory, final MessageReceiver receiveHandler,
         final AbstractMessageProcessor procHandler, @Named("search_rdf_esf_host") final String host,
         @Named("search_rdf_esf_port") final String port, final ResolutionController resController) {
      super(dmFactory, receiveHandler, procHandler, host, port);
      this.resController = resController;
   }

   @Override
   protected boolean systemReadyToHandleReceivedMessage() {
      // look for existence of rdf rs since it is needed to store the received data
      int count = 0;
      List<ResolutionServiceIdentityObject> installedRSList = resController.getResolutionServices();
      for (ResolutionServiceIdentityObject rsIdO : installedRSList) {
         if (rsIdO.getName().equals("RDFResolutionService")) {
            count++;
         }
      }
      if (count == 0) {
         LOG.error("Can not find any registered 'RDFResolutionService' where the received data could be stored");
         return false;
      }
      return true;
   }

}
