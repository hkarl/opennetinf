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
package netinf.node.access.rest;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.translation.DatamodelTranslator;
import netinf.node.access.rest.resources.BOResource;
import netinf.node.access.rest.resources.IOResource;
import netinf.node.access.rest.resources.SearchResource;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Extractor;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;

/**
 * Application providing a router for NetInf resources.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class RESTApplication extends Application {

   /** Connection to a NetInfNode */
   private NetInfNodeConnection nodeConnection;
   /** Implementation of a DatamodelFacotry */
   private DatamodelFactory datamodelFactory;
   /** Implementation of DatamodelTranslator */
   private DatamodelTranslator datamodelTranslator;

   public RESTApplication(NetInfNodeConnection connection, DatamodelFactory factory, DatamodelTranslator translator) {
      nodeConnection = connection;
      datamodelFactory = factory;
      datamodelTranslator = translator;
   }

   public NetInfNodeConnection getNodeConnection() {
      return nodeConnection;
   }

   public DatamodelFactory getDatamodelFactory() {
      return datamodelFactory;
   }

   public DatamodelTranslator getDatamodelTranslator() {
      return datamodelTranslator;
   }

   @Override
   public Restlet createInboundRoot() {
      Router router = new Router(getContext());

      // IO routing
      String targetIO1 = "{rh}/io?hash_of_pk={hashOfPK}&hash_of_pk_ident={hashOfPKIdent}&version_kind={versionKind}&unique_label={uniqueLabel}&version_number={versionNumber}";
      Redirector redirectorIO1 = new Redirector(getContext(), targetIO1, Redirector.MODE_CLIENT_TEMPORARY);
      Extractor extractorIO1 = new Extractor(getContext(), redirectorIO1);
      router.attach(
            "/io/ni:HASH_OF_PK={hashOfPK}~HASH_OF_PK_IDENT={hashOfPKIdent}~VERSION_KIND={versionKind}~UNIQUE_LABEL={uniqueLabel}~VERSION_NUMBER={versionNumber}",
            extractorIO1);

      String targetIO2 = "{rh}/io?hash_of_pk={hashOfPK}&hash_of_pk_ident={hashOfPKIdent}&version_kind={versionKind}&unique_label={uniqueLabel}";
      Redirector redirectorIO2 = new Redirector(getContext(), targetIO2, Redirector.MODE_CLIENT_TEMPORARY);
      Extractor extractorIO2 = new Extractor(getContext(), redirectorIO2);
      router.attach(
            "/io/ni:HASH_OF_PK={hashOfPK}~HASH_OF_PK_IDENT={hashOfPKIdent}~VERSION_KIND={versionKind}~UNIQUE_LABEL={uniqueLabel}",
            extractorIO2);

      String targetIO3 = "{rh}/io?hash_of_pk={hashOfPK}&hash_of_pk_ident={hashOfPKIdent}&version_kind={versionKind}";
      Redirector redirectorIO3 = new Redirector(getContext(), targetIO3, Redirector.MODE_CLIENT_TEMPORARY);
      Extractor extractorIO3 = new Extractor(getContext(), redirectorIO3);
      router.attach("/io/ni:HASH_OF_PK={hashOfPK}~HASH_OF_PK_IDENT={hashOfPKIdent}~VERSION_KIND={versionKind}", extractorIO3);

      router.attach("/io", IOResource.class);

      // BO routing
      String targetBO1 = "{rh}/bo?hash_of_pk={hashOfPK}&hash_of_pk_ident={hashOfPKIdent}&version_kind={versionKind}&unique_label={uniqueLabel}&version_number={versionNumber}";
      Redirector redirectorBO1 = new Redirector(getContext(), targetBO1, Redirector.MODE_CLIENT_TEMPORARY);
      Extractor extractorBO1 = new Extractor(getContext(), redirectorBO1);
      router.attach(
            "/ni:HASH_OF_PK={hashOfPK}~HASH_OF_PK_IDENT={hashOfPKIdent}~VERSION_KIND={versionKind}~UNIQUE_LABEL={uniqueLabel}~VERSION_NUMBER={versionNumber}",
            extractorBO1);

      String targetBO2 = "{rh}/bo?hash_of_pk={hashOfPK}&hash_of_pk_ident={hashOfPKIdent}&version_kind={versionKind}&unique_label={uniqueLabel}";
      Redirector redirectorBO2 = new Redirector(getContext(), targetBO2, Redirector.MODE_CLIENT_TEMPORARY);
      Extractor extractorBO2 = new Extractor(getContext(), redirectorBO2);
      router.attach(
            "/ni:HASH_OF_PK={hashOfPK}~HASH_OF_PK_IDENT={hashOfPKIdent}~VERSION_KIND={versionKind}~UNIQUE_LABEL={uniqueLabel}",
            extractorBO2);

      String targetBO3 = "{rh}/bo?hash_of_pk={hashOfPK}&hash_of_pk_ident={hashOfPKIdent}&version_kind={versionKind}";
      Redirector redirectorBO3 = new Redirector(getContext(), targetBO3, Redirector.MODE_CLIENT_TEMPORARY);
      Extractor extractorBO3 = new Extractor(getContext(), redirectorBO3);
      router.attach("/ni:HASH_OF_PK={hashOfPK}~HASH_OF_PK_IDENT={hashOfPKIdent}~VERSION_KIND={versionKind}", extractorBO3);

      router.attach("/bo", BOResource.class);

      // Search routing
      String targetSearch = "{rh}/search";
      Redirector redirectorSearch = new Redirector(getContext(), targetSearch, Redirector.MODE_CLIENT_TEMPORARY);
      router.attach("/", redirectorSearch);

      router.attach("/search", SearchResource.class);

      return router;
   }

}
