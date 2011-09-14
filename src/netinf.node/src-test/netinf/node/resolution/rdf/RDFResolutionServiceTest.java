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
package netinf.node.resolution.rdf;

import java.util.Properties;

import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.utils.Utils;
import netinf.node.rdf.ClearSDBDatabase;
import netinf.node.resolution.AbstractResolutionServiceTest;
import netinf.node.resolution.InformationObjectHelper;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Unit tests for {@link RDFResolutionService}
 * 
 * @author PG Augnet 2, University of Paderborn
 * @see AbstractResolutionServiceTest
 * @see RDFResolutionServiceTestModule
 */
public class RDFResolutionServiceTest extends AbstractResolutionServiceTest {

   private static final String CONFIGS_TESTING_PROPERTIES = "../configs/testing.properties";

   @Before
   public void setUp() throws Exception {
      PropertyConfigurator.configure(Utils.loadProperties(CONFIGS_TESTING_PROPERTIES));
      ClearSDBDatabase.main(null);

      final Properties properties = Utils.loadProperties(CONFIGS_TESTING_PROPERTIES);
      final Injector injector = Guice.createInjector(new RDFResolutionServiceTestModule(properties));
      ioHelper = injector.getInstance(InformationObjectHelper.class);
      resolutionService = injector.getInstance(RDFResolutionService.class);
   }

   @After
   public void tearDown() {
      RDFResolutionService rdfService = (RDFResolutionService) resolutionService;
      rdfService.tearDown();
   }

   @Test
   public void testPutGetComplex() {
      final InformationObject io = ioHelper.getUniqueIOWithDummyAttributeAndSubAttributes();
      resolutionService.put(io);
      Assert.assertEquals(io, resolutionService.get(io.getIdentifier()));
   }

   // This test case is rewritten for the rdf rs because it doesn`t use an event service

   @Override
   public void testDelete() throws NetInfCheckedException {
      resolutionService.put(ioHelper.getDummyIO());
      resolutionService.delete(ioHelper.getDummyIO().getIdentifier());
      Assert.assertNull(resolutionService.get(ioHelper.getDummyIO().getIdentifier()));
   }

   // ********************************
   // The following test cases of the AbstractResolutionServiceTest are not supported by the RDF RS (RDF RS doesn't use Event
   // Service). Therefore they are overwritten.
   // ***********

   @Override
   public void testPutWithFailingEventPublisher() {

   }

   @Override
   public void testPutWithEventPublisher() {

   }

}
