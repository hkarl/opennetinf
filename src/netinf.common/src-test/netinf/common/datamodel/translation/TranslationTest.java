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
package netinf.common.datamodel.translation;

import java.util.Properties;

import junit.framework.Assert;
import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DatamodelTest;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.rdf.module.DatamodelRdfModule;
import netinf.common.datamodel.translation.module.DatamodelTranslationModule;
import netinf.common.security.impl.module.SecurityModule;
import netinf.common.utils.Utils;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

/**
 * The inheritance is used in order to verify that it is still possible to use either Rdf or Impl directly.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class TranslationTest extends DatamodelTest {

   private static DatamodelTranslator datamodelTranslator;
   private static DatamodelFactory datamodelFactoryRdf;
   private InformationObject informationObjectRdf;
   private InformationObject informationObjectImpl;

   public static final String PROPERTIES = "../configs/testing.properties";

   @BeforeClass
   public static void setup() {
      final Properties properties = Utils.loadProperties(PROPERTIES);
      Injector injector = Guice.createInjector(new DatamodelTranslationModule(), new SecurityModule(), new DatamodelRdfModule(),
            new AbstractModule() {

               @Override
               protected void configure() {
                  Names.bindProperties(binder(), properties);
                  bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class);
               }
            });
      datamodelFactoryRdf = injector.getInstance(DatamodelFactory.class);
      datamodelTranslator = injector.getInstance(DatamodelTranslator.class);
   }

   @Before
   public void resetInformationObject() {
      informationObjectRdf = createDummyInformationObject(datamodelFactoryRdf);
      informationObjectImpl = datamodelTranslator.toImpl(informationObjectRdf);
   }

   @Test
   public void fromRdfToImpl() {
      InformationObject impl = datamodelTranslator.toImpl(informationObjectRdf);

      Assert.assertEquals(informationObjectRdf, impl);
      Assert.assertEquals(informationObjectImpl, impl);
   }

   @Test
   public void fromImplToRdf() {
      InformationObject rdf = datamodelTranslator.toRdf(informationObjectImpl);

      Assert.assertEquals(informationObjectImpl, rdf);
      Assert.assertEquals(informationObjectRdf, rdf);
   }

   @Override
   public void testSecureAttributeInOverall() {
      // this test does not run here
   }

   @Override
   public void testAddAuthorizedReader() {
      // this test does not run here
   }

   @Override
   public void testaddAuthorizedWriterToIO() {
      // this test does not run here
   }

}
