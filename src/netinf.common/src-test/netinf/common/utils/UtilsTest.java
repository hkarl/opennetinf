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
package netinf.common.utils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;
import java.util.Random;

import netinf.common.application.module.SecuredApplicationModule;
import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.log.module.LogModule;
import netinf.common.security.identity.IdentityManager;
import netinf.common.security.impl.module.SecurityModule;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

/**
 * The Class UtilsTest.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class UtilsTest {
   public static final String NETINFNODE_PROPERTIES = "../configs/testing/netinfnode_testing.properties";
   private DatamodelFactory factory;

   @Before
   public void setUp() throws Exception {
      final Properties properties = Utils.loadProperties(NETINFNODE_PROPERTIES);
      Guice.createInjector(new LogModule(properties));
      Injector createInjector = Guice.createInjector(new SecurityModule(), new DatamodelImplModule(), new AbstractModule() {

         @Override
         protected void configure() {
            Names.bindProperties(binder(), properties);
            bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class);
         }
      });
      factory = createInjector.getInstance(DatamodelFactory.class);

   }

   @Test
   public void testByteString() {
      byte[] bytes = new byte[50];
      new Random().nextBytes(bytes);
      String bytesToString = Utils.bytesToString(bytes);
      byte[] bytesToStringToBytes = Utils.stringToBytes(bytesToString);
      String bytesToStringToBytesToString = Utils.bytesToString(bytesToStringToBytes);
      Assert.assertArrayEquals(bytes, bytesToStringToBytes);
      Assert.assertEquals(bytesToString, bytesToStringToBytesToString);
   }

   @Test
   public void testPropertySerialisation() {
      Attribute property = createTestProperty();

      Attribute deserialisedProperty = ((Attribute) Utils.unserializeJavaObject(Utils.stringToBytes(Utils.bytesToString(property
            .serializeToBytes()))));
      Assert.assertEquals(property.getIdentification(), deserialisedProperty.getIdentification());
      Assert.assertEquals(property.getValueRaw(), deserialisedProperty.getValueRaw());
   }

   @Test
   public void testStringToPublicAndPrivateKey() {

      String applicationProperties = "../configs/createIOs.properties";

      Injector injector;
      IdentityManager identityManager;

      SecuredApplicationModule createIOsModule = new SecuredApplicationModule(applicationProperties);
      injector = Guice.createInjector(createIOsModule);
      final Properties properties = Utils.loadProperties(applicationProperties);

      identityManager = injector.getInstance(IdentityManager.class);
      identityManager.setFilePath("../configs/Identities/publicKeyFile.pkf");

      PublicKey pk = Utils.stringToPublicKey(properties.getProperty("publicKeyIdentity2"));

      String pkString = Utils.objectToString(pk);
      PublicKey pk2 = Utils.stringToPublicKey(pkString);

      Assert.assertEquals(pk, pk2);

      identityManager.setFilePath("../configs/Identities/privateKeyFile.pkf");

      PrivateKey pvk = Utils.stringToPrivateKey(properties.getProperty("privateKeyIdentity2"));
      String pvkString = Utils.objectToString(pvk);

      PrivateKey pvk2 = Utils.stringToPrivateKey(pvkString);

      Assert.assertEquals(pvk, pvk2);
   }

   @Test
   public void testGetObjectFromRaw() {

      String testType = "Boolean";
      String testEncoded = "true";

      Assert.assertEquals(true, ValueUtils.getObjectFromRaw(testType, testEncoded));

      testEncoded = "2";
      testType = "Byte";
      byte b = 2;
      Assert.assertEquals(b, ValueUtils.getObjectFromRaw(testType, testEncoded));

      testEncoded = "123.123";
      testType = "Float";
      float f = (float) 123.123;
      Assert.assertEquals(f, ValueUtils.getObjectFromRaw(testType, testEncoded));

      testEncoded = "123.12345678";
      testType = "Double";
      double d = 123.12345678;
      Assert.assertEquals(d, ValueUtils.getObjectFromRaw(testType, testEncoded));

      testEncoded = "abcdv";
      testType = "Character";
      Character c = 'a';
      Assert.assertEquals(c, ValueUtils.getObjectFromRaw(testType, testEncoded));

      testEncoded = "1234567890";
      testType = "Long";
      long l = 1234567890;
      Assert.assertEquals(l, ValueUtils.getObjectFromRaw(testType, testEncoded));

      testEncoded = "123";
      testType = "Short";
      short s = 123;
      Assert.assertEquals(s, ValueUtils.getObjectFromRaw(testType, testEncoded));

   }

   @After
   public void tearDown() throws Exception {
   }

   public Attribute createTestProperty() {
      String name = "Test-Property";
      String content = RandomStringUtils.random(500);

      Attribute property = factory.createAttribute(name, content);

      return property;
   }

}
