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
package netinf.common.communication;

import java.util.Properties;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DatamodelTest;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.security.impl.module.SecurityModule;
import netinf.common.utils.Utils;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

/**
 * @author PG NetInf 3
 */
public class RemoteNodeConnectionTest {

   private static final String PROPERTIES_PATH = "../configs/testing.properties";
   public static final String NETINFNODE_PROPERTIES = "../configs/testing/netinfnode_testing.properties";

   private Injector injector;
   private Injector createInjector;
   private Properties properties;

   private DatamodelFactory datamodelFactory;
   private Identifier testIdentifier;
   private Identifier testIdentifier2;
   private Identifier testIdentity;
   private InformationObject testIO;
   private InformationObject oldTestIO; // Only for the ESFEventMessage test

   @Before
   public void setup() {
      this.properties = Utils.loadProperties(PROPERTIES_PATH);
      this.injector = Guice.createInjector(new CommonTestModule(this.properties));

      this.injector.getInstance(MessageEncoderProtobuf.class);
      this.datamodelFactory = this.injector.getInstance(DatamodelFactory.class);

      final Properties node2properties = Utils.loadProperties(NETINFNODE_PROPERTIES);
      // Guice.createInjector(new LogModule(node2properties));
      this.createInjector = Guice.createInjector(new SecurityModule(), new DatamodelImplModule(), new AbstractModule() {

         @Override
         protected void configure() {
            Names.bindProperties(binder(), node2properties);
            bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class);
         }
      });
      createInjector.getInstance(DatamodelFactory.class);

      IdentifierLabel testIdentifierLabel = this.datamodelFactory.createIdentifierLabel();
      testIdentifierLabel.setLabelName("Uni");
      testIdentifierLabel.setLabelValue("Paderborn");
      this.testIdentifier = this.datamodelFactory.createIdentifier();
      this.testIdentifier.addIdentifierLabel(testIdentifierLabel);

      IdentifierLabel testIdentifierLabel2 = this.datamodelFactory.createIdentifierLabel();
      testIdentifierLabel2.setLabelName("Universitaet");
      testIdentifierLabel2.setLabelValue("Moscow");
      this.testIdentifier2 = this.datamodelFactory.createIdentifier();
      this.testIdentifier2.addIdentifierLabel(testIdentifierLabel2);

      IdentifierLabel testIdentityLabel = this.datamodelFactory.createIdentifierLabel();
      testIdentityLabel.setLabelName("Chuck");
      testIdentityLabel.setLabelValue("Norris");
      this.testIdentity = this.datamodelFactory.createIdentifier();
      this.testIdentity.addIdentifierLabel(testIdentityLabel);

      // this.testIO = this.datamodelFactory.createInformationObject();
      // this.testIO.setIdentifier(this.testIdentifier);
      this.testIO = DatamodelTest.createDummyInformationObject(this.datamodelFactory);

      this.oldTestIO = this.datamodelFactory.createInformationObject();
      this.oldTestIO.setIdentifier(testIdentifier2);
   }

   protected Injector getInjector() {
      return this.injector;
   }

   /**
    * Testing the getIO method by passing an identifier to the getIO method
    */
   @Test
   public void testGetIOsByIdentifier() {
      RemoteNodeConnection rnc = this.createInjector.getInstance(RemoteNodeConnection.class);
      try {
         rnc.setHostAndPort("127.0.0.1", 5000);
         rnc.putIO(testIO);
         rnc.getIO(this.testIO.getIdentifier());
         rnc.tearDown();
      } catch (NetInfCheckedException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

}
