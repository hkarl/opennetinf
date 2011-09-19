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
package netinf.eventservice.framework.module;

import java.util.Properties;

import netinf.access.TCPServer;
import netinf.common.communication.AsyncReceiveHandler;
import netinf.common.communication.Communicator;
import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.log.module.LogModule;
import netinf.common.security.impl.module.SecurityModule;
import netinf.eventservice.framework.PublisherHandler;
import netinf.eventservice.framework.SubscriberHandler;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * This is the module that is responsible for binding all the interfaces to implementations within the EventServiceFramework. It
 * relies on modules which bind the dataclasses correctly, and on the log module.<br>
 * <br>
 * Furthermore, this module is responsible of creating correct instance of {@link TCPServer} which are connected to the correct
 * {@link AsyncReceiveHandler}.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class EventServiceFrameworkModule extends AbstractModule {

   private final Properties properties;

   public EventServiceFrameworkModule(Properties properties) {
      this.properties = properties;
   }

   @Override
   protected void configure() {
      Names.bindProperties(binder(), properties);

      install(new LogModule(properties));
      install(new SecurityModule());
      install(new DatamodelImplModule());

      bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class).in(
            Singleton.class);

      bind(AsyncReceiveHandler.class).annotatedWith(Subscriber.class).to(SubscriberHandler.class).in(Singleton.class);
      bind(AsyncReceiveHandler.class).annotatedWith(Publisher.class).to(PublisherHandler.class).in(Singleton.class);
   }

   @Provides
   @Subscriber
   public TCPServer provideSubscriberServer(@Subscriber AsyncReceiveHandler asyncReceivehandler,
         @Named("subscriber.server_port") int port, Provider<Communicator> provider) {
      TCPServer tcpServer = new TCPServer(port);
      tcpServer.setAsyncReceiveHandler(asyncReceivehandler);
      tcpServer.injectProviderCommunicator(provider);
      return tcpServer;
   }

   @Provides
   @Publisher
   public TCPServer providePublisherServer(@Publisher AsyncReceiveHandler asyncReceivehandler,
         @Named("publisher.server_port") int port, Provider<Communicator> provider) {
      TCPServer tcpServer = new TCPServer(port);
      tcpServer.setAsyncReceiveHandler(asyncReceivehandler);
      tcpServer.injectProviderCommunicator(provider);
      return tcpServer;
   }

}
