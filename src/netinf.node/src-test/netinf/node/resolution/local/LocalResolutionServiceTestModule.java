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
package netinf.node.resolution.local;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.security.impl.module.SecurityModule;
import rice.environment.Environment;
import rice.p2p.commonapi.IdFactory;
import rice.pastry.commonapi.PastryIdFactory;
import rice.persistence.MemoryStorage;
import rice.persistence.Storage;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

/**
 * A module for the local resolution service unit test.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class LocalResolutionServiceTestModule extends AbstractModule {

   @Override
   protected void configure() {
      Properties props = new Properties();
      try {
         props.load(new FileInputStream("../configs/testing.properties"));
      } catch (FileNotFoundException e) {

      } catch (IOException e) {

      }
      Names.bindProperties(binder(), props);
      bind(Properties.class).toInstance(props);
      install(new SecurityModule());
      bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class);
      install(new DatamodelImplModule());

      bind(IdFactory.class).to(PastryIdFactory.class);

   }

   @Provides
   Storage provideStorage(IdFactory idf) {
      return new MemoryStorage(idf);
   }

   @Provides
   Environment provideEnvironment() {
      return new Environment();
   }

   @Provides
   PastryIdFactory providePastryIdFactory(Environment env) {
      return new PastryIdFactory(env);
   }

}
