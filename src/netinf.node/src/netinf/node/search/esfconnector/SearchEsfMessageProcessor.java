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
package netinf.node.search.esfconnector;

import java.util.ArrayList;
import java.util.List;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.DeleteMode;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.eventservice.AbstractMessageProcessor;
import netinf.common.eventservice.MessageReceiver;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.messages.ESFEventMessage;
import netinf.common.messages.NetInfMessage;
import netinf.common.messages.RSPutRequest;
import netinf.node.resolution.ResolutionController;
import netinf.node.resolution.rdf.RDFResolutionService;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * This class is the implementation of the {@link AbstractMessageProcessor} for the {@link SearchEsfConnector}. It stores the
 * received IO changes (creation, modification, deletion) in a {@link RDFResolutionService}.
 * 
 * @author PG Augnet 2, University of Paderborn
 * @see AbstractMessageProcessor
 * @see SearchEsfConnector
 * @see MessageReceiver
 */
public class SearchEsfMessageProcessor extends AbstractMessageProcessor {

   protected static final Logger LOG = Logger.getLogger(SearchEsfMessageProcessor.class);

   private ResolutionController resController;
   private DatamodelFactory dmFactory;

   @Inject
   public SearchEsfMessageProcessor(final ResolutionController resController, final DatamodelFactory dmFactory) {
      LOG.trace(null);
      this.resController = resController;
      this.dmFactory = dmFactory;
   }

   @Override
   protected void handleESFEventMessage(final ESFEventMessage eventMessage) {
      LOG.trace(null);
      LOG.debug("new event message to handle");
      LOG.log(DemoLevel.DEMO, "(NODE ) Handling incoming event message: Storing included information");

      List<ResolutionServiceIdentityObject> rsList = new ArrayList<ResolutionServiceIdentityObject>();

      // search for rdf rs
      List<ResolutionServiceIdentityObject> installedRSList = resController.getResolutionServices();
      for (ResolutionServiceIdentityObject rsIdO : installedRSList) {
         if (rsIdO.getName().equals("RDFResolutionService")) {
            rsList.add(rsIdO);
         }
      }
      if (rsList.isEmpty()) {
         throw new NetInfUncheckedException("Can not find any registered 'RDFResolutionService'");
         // TODO receiving of event messages should be stopped since they can not be handled
      }

      if (eventMessage.getNewInformationObject() == null) {
         // => message informs about IO deletion

         // delete information object
         InformationObject oldIO = eventMessage.getOldInformationObject();
         // mark information object for deletion
         Attribute attr = dmFactory.createAttribute(DefinedAttributeIdentification.DELETE.getURI(),
               DeleteMode.DELETE_DATA.getMode());
         attr.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
         oldIO.addAttribute(attr);

         RSPutRequest delReq = new RSPutRequest(oldIO, rsList);
         NetInfMessage resultMsg;
         resultMsg = resController.processNetInfMessage(delReq);
         if (resultMsg.getErrorMessage() != null) {
            LOG.error("Something went wrong while deleting the information object");
         }
      } else {
         // => message informs about creation or modification of an IO

         // store information object
         RSPutRequest putReq = new RSPutRequest(eventMessage.getNewInformationObject(), rsList);
         NetInfMessage resultMsg = resController.processNetInfMessage(putReq);
         if (resultMsg.getErrorMessage() != null) {
            LOG.error("Something went wrong while putting the information object");
         }
      }
   }
}
