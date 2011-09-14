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
package netinf.eventservice.framework;

import java.util.ArrayList;
import java.util.List;

import netinf.common.communication.AsyncReceiveHandler;
import netinf.common.communication.Communicator;
import netinf.common.messages.ESFEventMessage;
import netinf.common.messages.NetInfMessage;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * The handler is in charge of translating between the messages which arrive over the {@link Communicator} and method invocations
 * on the {@link EventServiceNetInf}. Accordingly, the handler has to regard the live cycle of <code>Communicator</code> objects.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
@SuppressWarnings("unchecked")
public class PublisherHandler implements AsyncReceiveHandler {
   private static final Logger LOG = Logger.getLogger(PublisherHandler.class);

   private final EventServiceNetInf eventService;
   private final ArrayList<PublisherNetInf> publisherStorage;

   @Inject
   public PublisherHandler(EventServiceNetInf eventService) {
      this.eventService = eventService;
      publisherStorage = new ArrayList<PublisherNetInf>();
   }

   @Override
   public void receivedMessage(NetInfMessage message, Communicator communicator) {
      LOG.trace(null);

      if (message instanceof ESFEventMessage) {
         PublisherNetInf publisherNetInf = getByCommunicator(communicator);

         // Create a new publisher in case of a new connection.
         if (publisherNetInf == null) {
            LOG.debug("Creating a new PublisherNetInf for new communicator");

            publisherNetInf = eventService.createPublisherNetInf();
            publisherNetInf.setCommunicator(communicator);
            addPublisherNetInf(publisherNetInf);
         }

         publisherNetInf.processEventMessage((ESFEventMessage) message);
      } else {
         LOG.error("Publisher send a not suitable message of type '" + message.getClass() + "' over the communicator '"
               + communicator + "'");
      }
   }

   public void addPublisherNetInf(PublisherNetInf publisherNetInf) {
      if (!publisherStorage.contains(publisherNetInf)) {
         publisherStorage.add(publisherNetInf);
      }
   }

   public List<PublisherNetInf> getAllPublisherNetInf() {
      // Must return a copy.
      return new ArrayList<PublisherNetInf>(publisherStorage);
   }

   public void removePublisherNetInf(PublisherNetInf publisherNetInf) {
      LOG.trace(null);

      boolean remove = publisherStorage.remove(publisherNetInf);

      if (remove) {
         LOG.debug("Removed publisherNetInf");
      } else {
         LOG.error("Could not remove publisherNetInf, since not present");
      }
   }

   private PublisherNetInf getByCommunicator(Communicator communicator) {
      PublisherNetInf result = null;

      for (PublisherNetInf publisherNetInf : publisherStorage) {
         if (communicator.equals(publisherNetInf.getCommunicator())) {
            result = publisherNetInf;
            break;
         }
      }

      return result;
   }
}
