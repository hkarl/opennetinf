package netinf.node.access.rest;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
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
 *
 */
public class RESTApplication extends Application {
   
   /** Conncection to a NetInfNode */
   private NetInfNodeConnection nodeConnection;
   /** Implementation of a DatamodelFacotry */
   private DatamodelFactory datamodelFactory;

   public RESTApplication(NetInfNodeConnection connection, DatamodelFactory factory) {
      this.nodeConnection = connection;
      this.datamodelFactory = factory;
   }
   
   public NetInfNodeConnection getNodeConnection() {
      return nodeConnection;
   }
   
   public DatamodelFactory getDatamodelFactory() {
      return datamodelFactory;
   }
   
   @Override
   public Restlet createInboundRoot() {
      Router router = new Router(getContext());
      
      // IO routing
      String targetIO1 = "/io?hash_of_pk={hashOfPK}&hash_of_pk_ident={hashOfPKIdent}&version_kind={versionKind}&unique_label={uniqueLabel}&version_number={versionNumber}";
      Redirector redirectorIO1 = new Redirector(getContext(), targetIO1, Redirector.MODE_CLIENT_TEMPORARY);
      Extractor extractorIO1 = new Extractor(getContext(), redirectorIO1);
      router.attach("/io/ni:HASH_OF_PK={hashOfPK}~HASH_OF_PK_IDENT={hashOfPKIdent}~VERSION_KIND={versionKind}~UNIQUE_LABEL={uniqueLabel}~VERSION_NUMBER={versionNumber}", extractorIO1);
      
      String targetIO2 = "/io?hash_of_pk={hashOfPK}&hash_of_pk_ident={hashOfPKIdent}&version_kind={versionKind}&unique_label={uniqueLabel}";
      Redirector redirectorIO2 = new Redirector(getContext(), targetIO2, Redirector.MODE_CLIENT_TEMPORARY);
      Extractor extractorIO2 = new Extractor(getContext(), redirectorIO2);
      router.attach("/io/ni:HASH_OF_PK={hashOfPK}~HASH_OF_PK_IDENT={hashOfPKIdent}~VERSION_KIND={versionKind}~UNIQUE_LABEL={uniqueLabel}", extractorIO2);
      
      String targetIO3 = "/io?hash_of_pk={hashOfPK}&hash_of_pk_ident={hashOfPKIdent}&version_kind={versionKind}";
      Redirector redirectorIO3 = new Redirector(getContext(), targetIO3, Redirector.MODE_CLIENT_TEMPORARY);
      Extractor extractorIO3 = new Extractor(getContext(), redirectorIO3);
      router.attach("/io/ni:HASH_OF_PK={hashOfPK}~HASH_OF_PK_IDENT={hashOfPKIdent}~VERSION_KIND={versionKind}", extractorIO3);
      
      router.attach("/io", IOResource.class);

      // BO routing
      String targetBO1 = "/bo?hash_of_pk={hashOfPK}&hash_of_pk_ident={hashOfPKIdent}&version_kind={versionKind}&unique_label={uniqueLabel}&version_number={versionNumber}";
      Redirector redirectorBO1 = new Redirector(getContext(), targetBO1, Redirector.MODE_CLIENT_TEMPORARY);
      Extractor extractorBO1 = new Extractor(getContext(), redirectorBO1);
      router.attach("/ni:HASH_OF_PK={hashOfPK}~HASH_OF_PK_IDENT={hashOfPKIdent}~VERSION_KIND={versionKind}~UNIQUE_LABEL={uniqueLabel}~VERSION_NUMBER={versionNumber}", extractorBO1);
      
      String targetBO2 = "/bo?hash_of_pk={hashOfPK}&hash_of_pk_ident={hashOfPKIdent}&version_kind={versionKind}&unique_label={uniqueLabel}";
      Redirector redirectorBO2 = new Redirector(getContext(), targetBO2, Redirector.MODE_CLIENT_TEMPORARY);
      Extractor extractorBO2 = new Extractor(getContext(), redirectorBO2);
      router.attach("/ni:HASH_OF_PK={hashOfPK}~HASH_OF_PK_IDENT={hashOfPKIdent}~VERSION_KIND={versionKind}~UNIQUE_LABEL={uniqueLabel}", extractorBO2);
      
      String targetBO3 = "/bo?hash_of_pk={hashOfPK}&hash_of_pk_ident={hashOfPKIdent}&version_kind={versionKind}";
      Redirector redirectorBO3 = new Redirector(getContext(), targetBO3, Redirector.MODE_CLIENT_TEMPORARY);
      Extractor extractorBO3 = new Extractor(getContext(), redirectorBO3);
      router.attach("/ni:HASH_OF_PK={hashOfPK}~HASH_OF_PK_IDENT={hashOfPKIdent}~VERSION_KIND={versionKind}", extractorBO3);
      
      router.attach("/bo", BOResource.class);

      // Search routing
      String targetSearch = "/search";
      Redirector redirectorSearch = new Redirector(getContext(), targetSearch, Redirector.MODE_CLIENT_TEMPORARY);
      router.attach("/", redirectorSearch);
      
      router.attach("/search", SearchResource.class);
      
      return router;
   }

}
