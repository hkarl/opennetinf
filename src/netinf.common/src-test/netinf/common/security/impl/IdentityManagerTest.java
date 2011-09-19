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
package netinf.common.security.impl;

import java.security.PrivateKey;
import java.util.Properties;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.security.identity.IdentityManager;
import netinf.common.security.impl.module.SecurityModule;
import netinf.common.utils.Utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 * The Class IdentityManagerTest.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IdentityManagerTest {

   public static final String NETINFNODE_PROPERTIES = "../configs/testing/netinfnode_testing.properties";

   private IdentityManager identityManager;

   @BeforeClass
   public static void setupClass() {

   }

   @Before
   public void setupTest() {
      final Properties properties = Utils.loadProperties(NETINFNODE_PROPERTIES);
      Injector injector = Guice.createInjector(new DatamodelImplModule(), new SecurityModule(), new AbstractModule() {

         @Override
         protected void configure() {
            Names.bindProperties(binder(), properties);
            bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class)
                  .in(Singleton.class);
         }
      });

      identityManager = injector.getInstance(IdentityManager.class);
   }

   @Test
   public void testCreateIdentity() throws NetInfCheckedException {
      identityManager.createNewMasterIdentity();

      String localIdentity = identityManager.getLocalIdentity();

      identityManager.getPrivateKey(localIdentity);
   }

   @Test
   public void testGetIdentity() throws NetInfCheckedException {
      String localIdentity = null;

      localIdentity = identityManager.getLocalIdentity();

      Assert.assertNotNull(localIdentity);

      PrivateKey privKey = null;
      privKey = identityManager.getPrivateKey(localIdentity);

      Assert.assertNotNull(privKey);
   }
}
