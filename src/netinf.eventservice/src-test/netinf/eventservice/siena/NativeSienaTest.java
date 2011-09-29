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
package netinf.eventservice.siena;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notifiable;
import siena.Notification;
import siena.Op;
import siena.SienaException;

/**
 * This class is intended to test the event service Siena itself.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class NativeSienaTest {

   private static boolean receivedNotification = false;
   private HierarchicalDispatcher esSiena;
   private Notification notification;
   private Notifiable subscriber;

   @Before
   public void initializeEventService() {
      esSiena = new HierarchicalDispatcher();

      notification = new Notification();
      notification.putAttribute("STRING", "value");
      notification.putAttribute("INT", 25);
      notification.putAttribute("BOOLEAN", true);

      subscriber = new Notifiable() {

         @Override
         public void notify(Notification n) throws SienaException {
            TestCase.assertEquals(notification, n);
            receivedNotification = true;
         }

         @Override
         public void notify(Notification[] s) throws SienaException {
         }
      };
   }

   @Test
   public void testSimpleMatching() {
      receivedNotification = false;

      Filter subscription = new Filter();
      subscription.addConstraint("STRING", Op.EQ, "value");

      try {
         esSiena.subscribe(subscription, subscriber);
         esSiena.publish(notification);

      } catch (SienaException e) {
         e.printStackTrace();
      }

      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

      TestCase.assertTrue(receivedNotification);
      esSiena.unsubscribe(subscription, subscriber);
   }

   @Test
   public void testComplexMatching() {
      receivedNotification = false;

      Filter subscription = new Filter();
      subscription.addConstraint("STRING", Op.SS, "lue");
      subscription.addConstraint("INT", Op.LT, 50);
      subscription.addConstraint("BOOLEAN", Op.EQ, true);

      try {
         esSiena.subscribe(subscription, subscriber);
         esSiena.publish(notification);

      } catch (SienaException e) {
         e.printStackTrace();
      }

      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

      TestCase.assertTrue(receivedNotification);
      esSiena.unsubscribe(subscription, subscriber);
   }

   @Test
   public void testNotMatching() {
      receivedNotification = false;

      Filter subscription = new Filter();
      subscription.addConstraint("STRING", Op.EQ, "something");

      try {
         esSiena.subscribe(subscription, subscriber);
         esSiena.publish(notification);

      } catch (SienaException e) {
         e.printStackTrace();
      }

      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

      TestCase.assertFalse(receivedNotification);
      esSiena.unsubscribe(subscription, subscriber);
   }

}
