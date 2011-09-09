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
package netinf.tools.iomanagement;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.datamodel.rdf.module.DatamodelRdfModule;
import netinf.common.security.impl.module.SecurityModule;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.martiansoftware.jsap.JSAPResult;

/**
 * Guice Module for the IO Management Tool
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IoManagementModule extends AbstractModule {
   /** property name for new datatypes */
   private static final String DTYPES = "dtypes";
   /** Log4j Logger component */
   private static final Logger log = Logger.getLogger(IoManagementModule.class);
   /** parsing results for command line options */
   private final JSAPResult options;

   /**
    * creates a new module that is configured via parsed command line
    * 
    * @param options
    *           parsed options to use
    */
   public IoManagementModule(JSAPResult options) {
      super();
      this.options = options;
      log.trace("enter/exit");
   }

   /*
    * (non-Javadoc)
    * @see com.google.inject.AbstractModule#configure()
    */
   @Override
   protected void configure() {
      log.trace(Constants.LOG_ENTER);
      bind(NetInfNodeConnection.class).to(RemoteNodeConnection.class).in(Singleton.class);

      String dtypes = "";
      if (this.options.getStringArray(DTYPES).length == 1) {
         dtypes = this.options.getStringArray(DTYPES)[0];
      }
      if (this.options.getStringArray(DTYPES).length > 1) {
         StringBuffer dtypeBuffer = new StringBuffer(this.options.getStringArray(DTYPES)[0]);
         for (int i = 1; i < this.options.getStringArray(DTYPES).length; i++) {
            dtypeBuffer.append(',');
            dtypeBuffer.append(this.options.getStringArray(DTYPES)[i]);
         }
         dtypes = dtypeBuffer.toString();
      }

      bind(String.class).annotatedWith(Names.named("cc.tcp.host")).toInstance(
            this.options.getInetAddress("host").getHostAddress());
      bind(String.class).annotatedWith(Names.named("communicator_host")).toInstance(
            this.options.getInetAddress("host").getHostAddress());
      bind(String.class).annotatedWith(Names.named("cc.tcp.port")).toInstance(Integer.toString(this.options.getInt("port")));
      bind(String.class).annotatedWith(Names.named("management_addDatatypes")).toInstance(dtypes);
      bind(String.class).annotatedWith(Names.named("communicator_format")).toInstance(this.options.getString("format"));

      log.debug("TOOL CONNECTION: " + this.options.getInetAddress("host").getHostAddress() + ":" + this.options.getInt("port"));

      install(new SecurityModule());
      bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class).in(
            Singleton.class);

      if (this.options.getString("format").equals("RDF")) {
         install(new DatamodelRdfModule());
      }
      if (this.options.getString("format").equals("JAVA")) {
         install(new DatamodelImplModule());
      }

      log.trace(Constants.LOG_EXIT);
   }
}
