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
package netinf.common.communication;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Inject;

/**
 * Tests the MessageEncoderXML
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class MessageEncoderXMLTest extends MessageEncoderTest {
   @Override
   @Before
   @Inject
   public void setup() {
      super.setup();
      setMessageEncoder(getInjector().getInstance(MessageEncoderXML.class));
   }

   @Override
   @Test
   //@Ignore("Not Implemented")
   public void testRSPutRequest() {
	   
	 super.testGetUniqueEncoderID();  
   }
   
   

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testRSPutResponse() {

   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testRSGetNameRequest() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testRSGetNameResponse() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testRSGetPriorityRequest() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testRSGetPriorityResponse() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testRSGetServicesRequest() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testRSGetServicesResponse() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testESFRegistrationRequest() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testESFRegistrationResponse() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testESFSubscriptionRequest() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testESFSubscriptionResponse() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testESFUnsubscriptionRequest() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testESFUnsubscriptionResponse() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testTCGetServicesRequest() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testTCGetServicesResponse() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testSCGetByQueryTemplateRequest() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testSCGetBySPARQLRequest() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testSCSearchResponse() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testSCGetTimeoutAndNewSearchIDRequest() {
   }

   @Override
   @Test
   @Ignore("Not Implemented")
   public void testSCGetTimeoutAndNewSearchIDResponse() {
   }
}
