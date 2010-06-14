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
package netinf.node.api;

import java.io.IOException;
import java.util.Properties;

import junit.framework.Assert;
import netinf.common.communication.Communicator;
import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.messages.RSPutRequest;
import netinf.common.messages.RSPutResponse;
import netinf.common.utils.Utils;
import netinf.node.resolution.InformationObjectHelper;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * The Class AccessTest.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class AccessTest {

   protected static InformationObjectHelper ioHelper;

   private static final String CONFIGS_TESTING_PROPERTIES = "../configs/testing.properties";
   private static Injector injector;
   private static Communicator communicator;

   @BeforeClass
   public static void configure() throws IOException {
      Properties properties = Utils.loadProperties(CONFIGS_TESTING_PROPERTIES);
      injector = Guice.createInjector(new AccessTestModule(properties));
      ioHelper = injector.getInstance(InformationObjectHelper.class);

      communicator = injector.getInstance(Communicator.class);
      communicator.setup("localhost", Integer.parseInt(properties.getProperty("access.tcp.port")));
   }

   @Test
   public void testCreateInformationObject() throws NetInfCheckedException {
      InformationObject informationObject = ioHelper.createUniqueVersionedIO();

      RSPutRequest putRequest = new RSPutRequest(informationObject);
      communicator.send(putRequest);
      RSPutResponse result = (RSPutResponse) communicator.receive();

      Assert.assertEquals(result.getErrorMessage(), null);
   }

   @Test
   public void testGetInformationObject() {
      // TODO Implement
   }

   @Test
   public void testDeleteInformationObject() {
      // TODO Implement
   }

}
