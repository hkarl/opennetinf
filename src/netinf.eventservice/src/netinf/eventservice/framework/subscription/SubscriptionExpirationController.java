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
package netinf.eventservice.framework.subscription;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import netinf.common.datamodel.Identifier;
import netinf.eventservice.framework.EventServiceNetInf;
import netinf.eventservice.framework.SubscriberNetInf;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * This class is responsible of keeping consistency between the state within the main memory, and the database. This yields the
 * possibility of recovering from e failing event broker, when the state is additionally stored within the database.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
@SuppressWarnings("unchecked")
public class SubscriptionExpirationController {

   private static final Logger LOG = Logger.getLogger(SubscriptionExpirationController.class);

   private final EventServiceNetInf eventServiceNetInf;
   private UnsubscriberThread unsubscriberThread;

   private LinkedList<SubscriptionElement> subscriptions;

   // Constants
   private final String threadName;

   @Inject
   public SubscriptionExpirationController(EventServiceNetInf eventServiceNetInf,
         @Named("subscriber.expiration_controller.thread_name") String threadName) {
      this.eventServiceNetInf = eventServiceNetInf;
      this.threadName = threadName;
   }

   public boolean setup() {
      LOG.trace(null);

      subscriptions = new LinkedList<SubscriptionElement>();

      unsubscriberThread = new UnsubscriberThread(threadName);
      unsubscriberThread.start();

      return true;
   }

   public boolean tearDown() {
      LOG.trace(null);

      subscriptions = null;

      unsubscriberThread.stopUnsubscriberThread();

      return true;
   }

   public void printSortedList() {
      StringBuffer result = new StringBuffer();

      ListIterator<SubscriptionElement> listIterator = subscriptions.listIterator();
      while (listIterator.hasNext()) {
         SubscriptionElement se = listIterator.next();
         result.append(se + " <<< ");
      }

      LOG.debug("Sorting of SubscriptionElements: " + result.toString());
   }

   public synchronized void addSubscriptionMessage(Identifier eventContainerId, String subscriptionIdentification,
         long expiration) {
      LOG.trace(null);

      SubscriptionElement newSe = new SubscriptionElement(eventContainerId, subscriptionIdentification, expiration);

      int pos = addSubscriptionElement(newSe);

      if (pos == 0) {
         // Send a signal to the unsubscriper thread
         unsubscriberThread.interrupt();
      }
   }

   public synchronized void removeSubscriptionMessage(Identifier eventContainerId, String subscriptionIdentification) {
      LOG.trace(null);

      ListIterator<SubscriptionElement> iterSubscriptions = subscriptions.listIterator();
      SubscriptionElement selection = null;

      while (iterSubscriptions.hasNext()) {
         selection = iterSubscriptions.next();

         if (selection.getEventContainerID().equals(eventContainerId)
               && selection.getSubscriptionIdentification().endsWith(subscriptionIdentification)) {
            iterSubscriptions.remove();
            break;
         }
      }
   }

   private synchronized void informSubscriberNetInf(SubscriptionElement subscriptionElement) {
      LOG.trace(null);

      SubscriberNetInf subscriberNetInf = eventServiceNetInf.getSubscriberNetInf(subscriptionElement
            .getEventContainerID());

      // This should usually never happend.
      // Nevertheless, this increases robustness of our programm.
      if (subscriberNetInf != null) {

         // This will in turn call the removeSubscriptionMessage method of this class.
         subscriberNetInf.processUnsubscriptionWithSubscriptionIdentification(subscriptionElement
               .getSubscriptionIdentification());
      } else {
         // Otherwise just remove the SubscriptionElement
         LOG.error("Found SubscriptionElement that does not correspond to an SubscriberNetInf");
         subscriptions.remove(subscriptionElement);
      }
   }

   private synchronized SubscriptionElement getFrontSubscriptionElement() {
      if (subscriptions.size() > 0) {
         return subscriptions.getFirst();
      }

      return null;
   }

   /**
    * Adds the <code>SubscriptionElement</code> to the correct position.
    * 
    * @param subscriptionElement
    * @return
    */
   private synchronized int addSubscriptionElement(SubscriptionElement subscriptionElement) {
      LOG.trace(null);

      // Find the variable before which the new subscription element has to be inserted
      ListIterator<SubscriptionElement> iterSubscriptions = subscriptions.listIterator();

      int pos = 0;
      boolean notLast = true;

      notLast = iterSubscriptions.hasNext();

      while (notLast) {
         SubscriptionElement current = iterSubscriptions.next();

         // Thus we will get the first one that is creator in the
         // current variable
         if (current.getExpires() > subscriptionElement.getExpires()) {
            break;
         }

         // Does not have to be inserted directly at the beginning
         pos++;
         notLast = iterSubscriptions.hasNext();
      }

      if (notLast) {
         // This try catch block is important to BEFORE the element
         // that is larger, in order to insert the according
         // element.
         try {
            iterSubscriptions.previous();
         } catch (NoSuchElementException e) {
            LOG.debug("Inserting new element at the beginning");
            // Not important
            // This happens, when the first element is inserted.
         }
      }

      // Finally, insert the subscription.
      iterSubscriptions.add(subscriptionElement);

      return pos;
   }

   /**
    * The Class UnsubscriberThread.
    * 
    * @author PG Augnet 2, University of Paderborn
    */
   private class UnsubscriberThread extends Thread {

      private boolean stopped;

      public UnsubscriberThread(String name) {
         super(name);

         stopped = false;
      }

      public void stopUnsubscriberThread() {
         LOG.trace(null);
         stopped = true;
         interrupt();
      }

      @Override
      public void run() {
         LOG.trace(null);

         while (!stopped) {
            SubscriptionElement frontSubscriptionElement = getFrontSubscriptionElement();

            try {
               if (frontSubscriptionElement != null) {
                  long timetoSleep = frontSubscriptionElement.getExpires() - System.currentTimeMillis();

                  if (timetoSleep > 0) {
                     LOG.debug("UnsubscriberThread goes to sleep for '" + timetoSleep
                           + "' milliseconds for EventContainerId '" + frontSubscriptionElement.getEventContainerID()
                           + "' and subscriptionIdentification '"
                           + frontSubscriptionElement.getSubscriptionIdentification() + "'");

                     Thread.sleep(timetoSleep);
                  }

                  // If we were not woken up, we can remove the current element
                  // But previously verify that we are still on the front element
                  if (frontSubscriptionElement == getFrontSubscriptionElement()) {

                     LOG.debug("SubscriptionMessage expired. EventContainerId '"
                           + frontSubscriptionElement.getEventContainerID() + "' and subscriptionIdentification '"
                           + frontSubscriptionElement.getSubscriptionIdentification() + "'");

                     informSubscriberNetInf(frontSubscriptionElement);
                  }
               } else {
                  LOG.debug("No subscription message to wait for, going to sleep");
                  // Just wait infinitely long. You will
                  // be woken up some time in the future

                  // FIXME: Is there a better solution than to sleep infinitely long?
                  Thread.sleep(Long.MAX_VALUE);
               }
            } catch (InterruptedException e) {
               LOG.debug("Woken up, something happend, going to check what happend.");
               // Nothing bad happend, you were just woken up!
               // Go on working with the next subscription element
            }
         }

         LOG.debug("Exiting the thread responsible for expiration");
      }
   }

   /**
    * The Class SubscriptionElement.
    * 
    * @author PG Augnet 2, University of Paderborn
    */
   private class SubscriptionElement {
      private final Identifier eventContainerID;
      private final String subscriptionIdentification;
      private final long expires;

      public SubscriptionElement(Identifier eventContainerID, String subscriptionIdentification, long expires) {
         this.eventContainerID = eventContainerID;
         this.subscriptionIdentification = subscriptionIdentification;
         this.expires = expires;
      }

      public Identifier getEventContainerID() {
         return eventContainerID;
      }

      public String getSubscriptionIdentification() {
         return subscriptionIdentification;
      }

      public long getExpires() {
         return expires;
      }

      @Override
      public String toString() {
         return "EventContainerId '" + eventContainerID + "', " + "SubscriptionIdentification '"
               + subscriptionIdentification + "', " + "expires'" + expires + "'";
      }

   }

}
