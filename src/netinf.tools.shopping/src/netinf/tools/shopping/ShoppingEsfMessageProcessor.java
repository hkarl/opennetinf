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
package netinf.tools.shopping;

import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.eventservice.AbstractMessageProcessor;
import netinf.common.eventservice.MessageReceiver;
import netinf.common.log.demo.DemoLevel;
import netinf.common.messages.ESFEventMessage;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.mapviewer.Waypoint;

/**
 * The {@link ShoppingEsfMessageProcessor} handles incoming Events and triggers actions in the Shopping Tool based on the incoming
 * Event.
 * 
 * @see AbstractMessageProcessor
 * @see ShoppingEsfConnector
 * @see MessageReceiver
 * @author PG Augnet 2, University of Paderborn
 */
public class ShoppingEsfMessageProcessor extends AbstractMessageProcessor {

   protected static final Logger LOG = Logger.getLogger(ShoppingEsfMessageProcessor.class);

   private MainFrame mainFrame;

   void setMainFrame(final MainFrame mainFrame) {
      this.mainFrame = mainFrame;
   }

   @Override
   protected void handleESFEventMessage(final ESFEventMessage eventMessage) {
      LOG.trace(null);

      InformationObject receivedIO = eventMessage.getNewInformationObject();

      if (eventMessage.getMatchedSubscriptionIdentification().equals(mainFrame.getLoadedIdentifier().toString())) {
         LOG.log(DemoLevel.DEMO, "(TOOL ) Got informed about a modification of the loaded ShoppingList. Updating list ");

         mainFrame.updateProductListFromIO();
         mainFrame.updateProductListView();
      }

      if (eventMessage.getMatchedSubscriptionIdentification().equals("newShoppinglists")) {
         LOG.log(DemoLevel.DEMO, "(TOOL ) Got informed about a new ShoppingList. Adding list to drop down menu ");

         mainFrame.addShoppinglist(eventMessage.getNewInformationObject());
      }

      if (eventMessage.getMatchedSubscriptionIdentification().equals("newShops")) {
         LOG.log(DemoLevel.DEMO, "(TOOL ) Got informed about a new Shop. Updating map ");

         Double lat = receivedIO.getSingleAttribute(DefinedAttributeIdentification.GEO_LAT.getURI()).getValue(Double.class);
         Double lon = receivedIO.getSingleAttribute(DefinedAttributeIdentification.GEO_LONG.getURI()).getValue(Double.class);
         mainFrame.addWaypointToMap(new Waypoint(lat, lon));
      }
   }
}
