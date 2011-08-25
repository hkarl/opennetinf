/*
 * Copyright (C) 2009,2010 University of Paderborn, Computer Networks Group
 * Copyright (C) 2009,2010 Christian Dannewitz <christian.dannewitz@upb.de>
 * Copyright (C) 2009,2010 Thorsten Biermann <thorsten.biermann@upb.de>
 * 
 * Copyright (C) 2009,2010 Eduard Bauer <edebauer@mail.upb.de>
 * Copyright (C) 2009,2010 Matthias Becker <matzeb@mail.upb.de>
 * Copyright (C) 2009,2010 Frederic Beister <azamir@zitmail.uni-paderborn.de>
 * Copyright (C) 2009,2010 Nadine Dertmann <ndertmann@gmx.de>
 * Copyright (C) 2009,2010 Michael Kionka <mkionka@mail.upb.de>
 * Copyright (C) 2009,2010 Mario Mohr <mmohr@mail.upb.de>
 * Copyright (C) 2009,2010 Felix Steffen <felix.steffen@gmx.de>
 * Copyright (C) 2009,2010 Sebastian Stey <sebstey@mail.upb.de>
 * Copyright (C) 2009,2010 Steffen Weber <stwe@mail.upb.de>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.node.search.rdf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.identity.SearchServiceIdentityObject;
import netinf.common.datamodel.rdf.DefinedRdfNames;
import netinf.common.exceptions.NetInfSearchException;
import netinf.common.search.DefinedQueryTemplates;
import netinf.common.utils.Utils;
import netinf.database.sdb.SDBStoreFactory;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.rdf.RDFResolutionService;
import netinf.node.search.SearchController;
import netinf.node.search.SearchService;
import netinf.node.search.esfconnector.SearchEsfConnector;
import netinf.node.search.impl.events.SearchServiceErrorEvent;
import netinf.node.search.impl.events.SearchServiceResultEvent;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

/**
 * The Search Service RDF is a {@link SearchService} implementation which searches within a local SDB database in which the data
 * is stored in RDF format.
 * <p>
 * Possibility 1: <br>
 * If this search service uses the same local database as the {@link RDFResolutionService}, it is able to search for information
 * objects which are stored in the resolution service.
 * <p>
 * Possibility 2:<br>
 * Using the {@link SearchEsfConnector} this search service is able to subscribe to (specific) information object (changes) at an
 * event service. In this way it is able to obtain knowledge about information objects within different {@link ResolutionService}s
 * if this resolution services publish their information objects to the event service.
 * 
 * @author PG Augnet 2, University of Paderborn
 * @see SearchService
 */
public class SearchServiceRDF implements SearchService {

   private static final Logger LOG = Logger.getLogger(SearchServiceRDF.class);

   private final DatamodelFactory datamodelFactory;

   private SearchServiceIdentityObject identityObject;
   private final String identifier;
   private final String privateKey;
   private final String publicKey;

   private SDBConnection conn;
   private Store store;
   private Dataset dataset;
   private Model model;

   private final SearchEsfConnector searchEsfConnector;

   @Inject
   public SearchServiceRDF(final DatamodelFactory dmfactory, SDBStoreFactory sdbStoreFactory,
         @Named("search_rdf_identifier") final String identifier, @Named("search_rdf_privateKey") final String privateKey,
         @Named("search_rdf_publicKey") final String publicKey, @Named("search_rdf_connect_to_esf") final String connectToEsf,
         final SearchEsfConnector searchEsfConnector) {
      LOG.trace(null);

      this.datamodelFactory = dmfactory;

      this.identifier = identifier;
      this.privateKey = privateKey;
      this.publicKey = publicKey;

      this.searchEsfConnector = searchEsfConnector;

      this.conn = null;
      this.store = null;
      this.dataset = null;
      this.model = null;

      boolean ready = false;
      try {
         this.store = sdbStoreFactory.createStore();
         this.dataset = SDBFactory.connectDataset(this.store);
         this.model = SDBFactory.connectDefaultModel(this.store);
      } catch (Exception e) {
         LOG.error("The following error occured while trying to connect to SDB store: " + e.getMessage());
         LOG.error("Not connected to SDB store");
         return;
      }
      if (!this.store.isClosed()) {
         LOG.debug("Successfully connected to SDB store");
         ready = true;
      } else {
         LOG.error("Could not establish connection to SDB store");
      }

      if (ready) {
         if (connectToEsf.equals("true")) {
            LOG.debug("Start ESF Connector");
            this.searchEsfConnector.setName("esfConnector");
            this.searchEsfConnector.setIdentityIdentifier(getIdentityObject().getIdentifier());
            List<String> subscriptionIdentifications = new ArrayList<String>();
            List<String> subscriptionQueries = new ArrayList<String>();
            List<Long> subscriptionExpireTimes = new ArrayList<Long>();
            subscriptionIdentifications.add(0, "esfConnectorSubscription1");
            subscriptionQueries.add(0, "SELECT ?old ?new WHERE {?new <" + DefinedAttributeIdentification.REPRESENTS.getURI()
                  + "> \"Shop\".}");
            subscriptionExpireTimes.add(0, Long.valueOf(1000000000));
            subscriptionIdentifications.add(1, "esfConnectorSubscription2");
            subscriptionQueries.add(1, "SELECT ?old ?new WHERE {?new <" + DefinedAttributeIdentification.REPRESENTS.getURI()
                  + "> \"ShoppingList\".}");
            subscriptionExpireTimes.add(1, Long.valueOf(1000000000));
            this.searchEsfConnector.setInitialSubscriptionInformation(subscriptionIdentifications, subscriptionQueries,
                  subscriptionExpireTimes);
            this.searchEsfConnector.start();
         }
      }
   }

   @Override
   public void getBySPARQL(final String request, final int searchID, final SearchServiceIdentityObject searchServiceIdO,
         final SearchController callback) {
      LOG.trace(null);

      final String constructedQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX "
            + DefinedRdfNames.NETINF_NAMESPACE_NAME + ": <" + DefinedRdfNames.NETINF_RDF_SCHEMA_URI + "> "
            + "SELECT ?id WHERE {?bNode <" + DefinedRdfNames.POINTER_TO_IO + "> ?id. " + request + "}";

      LOG.info("Performing search with the following query: " + constructedQuery);

      Query query = null;
      try {
         query = QueryFactory.create(constructedQuery);
      } catch (QueryParseException e) {
         LOG.error("Invalid query syntax: " + e.toString());
         callback.handleSearchEvent(new SearchServiceErrorEvent("invalid query syntax: query: " + constructedQuery + ", error: "
               + e.getLocalizedMessage(), searchID, searchServiceIdO));
         return;
      }

      // perform search
      Set<Identifier> resultSet = new HashSet<Identifier>();
      try {
         resultSet = executeSearch(query);
      } catch (Exception e) {
         LOG.error("An error occured while executing search query or processing results: " + e.toString());
         callback.handleSearchEvent(new SearchServiceErrorEvent(
               "An error occured while executing search query or processing results", searchID, searchServiceIdO));
         return;
      }

      LOG.debug("finished search (id: " + searchID + "), handing over result to calling search controller");
      callback.handleSearchEvent(new SearchServiceResultEvent("search result of " + getIdentityObject().getName(), searchID,
            searchServiceIdO, resultSet));
   }

   private Set<Identifier> executeSearch(final Query query) {
      Set<Identifier> resultSet = new HashSet<Identifier>();
      this.model.enterCriticalSection(true);
      try {
         if (this.store == null || this.store.isClosed()) {
            throw new NetInfSearchException("Not connected to storage");
         }

         final QueryExecution qe = QueryExecutionFactory.create(query, this.dataset);
         try {
            final ResultSet rs = qe.execSelect();
            LOG.debug("Executed query successfully. Start processing query solutions");
            QuerySolution qs = null;
            RDFNode node = null;
            String idString = "";
            Identifier id = null;
            while (rs.hasNext()) {
               qs = rs.next();
               node = qs.get("id");
               idString = node.toString();
               id = this.datamodelFactory.createIdentifierFromString(idString);
               resultSet.add(id);
               LOG.trace("Processed query solution: " + idString);
            }
         } finally {
            qe.close();
         }
      } finally {
         this.model.leaveCriticalSection();
      }
      return resultSet;
   }

   @Override
   public void getByQueryTemplate(final String type, final List<String> parameters, final int searchID,
         final SearchServiceIdentityObject searchServiceIdO, final SearchController callback) {
      LOG.trace(null);

      DefinedQueryTemplates typeEnum = DefinedQueryTemplates.getDefinedQueryTemplateName(type);
      if (typeEnum == null) {
         LOG.error("Template type '" + type + "' does not exist");
         callback.handleSearchEvent(new SearchServiceErrorEvent("Template type '" + type + "' does not exist", searchID,
               searchServiceIdO));
         return;
      }
      switch (typeEnum) {
      case POSITION_BASED_SHOP_IN_RADIUS_HAS_PRODUCT:
         processPositionBasedShopInRadiusHasProductSearch(parameters, searchID, searchServiceIdO, callback);
         break;
      default:
         LOG.warn("The query template '" + type + "' is not supported by this search service");
         callback.handleSearchEvent(new SearchServiceErrorEvent("The query template '" + type
               + "' is not supported by this search service", searchID, searchServiceIdO));
      }
   }

   @Override
   public boolean isReady() {
      LOG.trace(null);
      if (this.store == null || this.store.isClosed()) {
         return false;
      }
      return true;
   }

   @Override
   public SearchServiceIdentityObject getIdentityObject() {
      if (this.identityObject == null) {
         createIdentityObject();
      }
      return this.identityObject;
   }

   private void createIdentityObject() {
      SearchServiceIdentityObject idO = this.datamodelFactory.createSearchServiceIdentityObject();
      idO.setIdentifier(this.datamodelFactory.createIdentifierFromString(this.identifier));
      idO.setName("SearchServiceRdf");
      idO.setDescription("This Search Service can be used to search in the data store of a RDF Resolution Service.");
      idO.setPublicMasterKey(Utils.stringToPublicKey(this.publicKey));
      this.identityObject = idO;
   }

   /**
    * processes the search requests which use the template {@link DefinedQueryTemplates#POSITION_BASED_SHOP_IN_RADIUS_HAS_PRODUCT}
    * 
    * @param parameters
    * @param searchID
    * @param searchServiceIdO
    * @param callback
    */
   private void processPositionBasedShopInRadiusHasProductSearch(final List<String> parameters, final int searchID,
         final SearchServiceIdentityObject searchServiceIdO, final SearchController callback) {
      LOG.trace(null);

      // get the parameters
      double latParam = 0.0;
      double longParam = 0.0;
      int radiusParam = 0;
      String product = null;
      String[] products = null;
      String parseErrors = "Some parameters were not properly set: ";
      boolean parseOK = true;

      try {
         latParam = Double.parseDouble(parameters.get(0));
      } catch (IndexOutOfBoundsException ioobe) {
         parseOK = false;
         parseErrors = parseErrors + "Latitude: not specified; ";
      } catch (NullPointerException npe) {
         parseOK = false;
         parseErrors = parseErrors + "Latitude: not specified; ";
      } catch (NumberFormatException e) {
         parseOK = false;
         parseErrors = parseErrors + "Latitude: not of datatype 'Double'; ";
      }

      try {
         longParam = Double.parseDouble(parameters.get(1));
      } catch (IndexOutOfBoundsException ioobe) {
         parseOK = false;
         parseErrors = parseErrors + "Longitude: not specified; ";
      } catch (NullPointerException npe) {
         parseOK = false;
         parseErrors = parseErrors + "Longitude: not specified; ";
      } catch (NumberFormatException e) {
         parseOK = false;
         parseErrors = parseErrors + "Longitude: not of datatype 'Double'; ";
      }

      try {
         radiusParam = Integer.parseInt(parameters.get(2));
      } catch (IndexOutOfBoundsException ioobe) {
         parseOK = false;
         parseErrors = parseErrors + "Radius: not specified; ";
      } catch (NullPointerException npe) {
         parseOK = false;
         parseErrors = parseErrors + "Radius: not specified; ";
      } catch (NumberFormatException e) {
         parseOK = false;
         parseErrors = parseErrors + "Radius: not of datatype 'Integer'; ";
      }

      try {
         product = parameters.get(3);
         // separate products

      } catch (IndexOutOfBoundsException e) {
         parseOK = false;
         parseErrors = parseErrors + "Products: not specified; ";
      }
      if (product == null) {
         parseErrors = parseErrors + "Products: not specified; ";
      } else {
         products = product.split(";");
         if (products.length == 0) {
            parseErrors = parseErrors + "Products: not specified; ";
         }
      }

      if (parseOK) {
         LOG.info("Performing search using query template 'positionBasedShopInRadiusHasProduct' with the parameters: Latitude="
               + latParam + ", Longitude=" + longParam + ", Radius=" + radiusParam + "m, Products=" + product);
      } else {
         LOG.error(parseErrors);
         callback.handleSearchEvent(new SearchServiceErrorEvent(parseErrors, searchID, searchServiceIdO));
         return;
      }

      // construct sparql query for coarse rectangle search
      String constructedQuery = null;

      // FIXME insert filters for longitude and latitude to reduce result size

      constructedQuery = "SELECT ?identifier ?lat ?long WHERE {";

      constructedQuery += "{?identifier <" + DefinedAttributeIdentification.REPRESENTS.getURI() + "> ?blank_a. ?blank_a <"
            + DefinedRdfNames.ATTRIBUTE_VALUE + "> 'String:Shop'.  ?identifier <"
            + DefinedAttributeIdentification.GEO_LAT.getURI() + "> ?blank_b. ?blank_b <" + DefinedRdfNames.ATTRIBUTE_VALUE
            + "> ?lat. ?identifier <" + DefinedAttributeIdentification.GEO_LONG.getURI() + "> ?blank_c. ?blank_c <"
            + DefinedRdfNames.ATTRIBUTE_VALUE + "> ?long. ?identifier <" + DefinedAttributeIdentification.PRODUCT.getURI()
            + "> ?blank_d. ?blank_d <" + DefinedRdfNames.ATTRIBUTE_VALUE + "> 'String:" + products[0] + "'. ?blank_d <"
            + DefinedAttributeIdentification.AMOUNT.getURI() + "> ?blank_e. ?blank_e <" + DefinedRdfNames.ATTRIBUTE_VALUE
            + "> ?amount FILTER regex (?amount, \"Integer:[1-9]\")}";

      // add further products to query in case they exist
      if (products.length > 1) {
         int i = 0;
         // eddy: was productCount = 1 before
         for (int productCount = 0; productCount < products.length; productCount++) {
            constructedQuery += " UNION { ?identifier <" + DefinedAttributeIdentification.REPRESENTS.getURI() + "> ?blank_" + i
                  + ". ?blank_" + i + " <" + DefinedRdfNames.ATTRIBUTE_VALUE + "> 'String:Shop'.";
            i++;
            constructedQuery += " ?identifier <" + DefinedAttributeIdentification.GEO_LAT.getURI() + "> ?blank_" + i + ". "
                  + "?blank_" + i + " <" + DefinedRdfNames.ATTRIBUTE_VALUE + "> ?lat.";
            i++;
            constructedQuery += " ?identifier <" + DefinedAttributeIdentification.GEO_LONG.getURI() + "> ?blank_" + i + ". "
                  + "?blank_" + i + " <" + DefinedRdfNames.ATTRIBUTE_VALUE + "> ?long.";
            i++;
            constructedQuery += " ?identifier <" + DefinedAttributeIdentification.PRODUCT.getURI() + "> ?blank_" + i + ". "
                  + "?blank_" + i + " <" + DefinedRdfNames.ATTRIBUTE_VALUE + "> 'String:" + products[productCount] + "'. ?blank_"
                  + i + " <" + DefinedAttributeIdentification.AMOUNT.getURI();
            i++;
            constructedQuery += "> ?blank_" + i + ". ?blank_" + i + " <" + DefinedRdfNames.ATTRIBUTE_VALUE + "> ?amount_" + i
                  + " FILTER regex (?amount_" + i + ", \"Integer:[1-9]\")}";
         }
      }

      constructedQuery = constructedQuery + "}";

      LOG.debug("Performing coarse grain search with the following query: " + constructedQuery);

      // generate query
      Query query = null;
      try {
         query = QueryFactory.create(constructedQuery);
      } catch (QueryParseException e) {
         LOG.warn("Invalid query syntax: " + e.toString());
         callback.handleSearchEvent(new SearchServiceErrorEvent("syntax of generated query is invalid", searchID,
               searchServiceIdO));
         return;
      }

      // perform search
      Vector<Vector<String>> results = new Vector<Vector<String>>();
      this.model.enterCriticalSection(true);
      try {
         if (this.store == null || this.store.isClosed()) {
            LOG.error("Not connected to SDB store");
            callback.handleSearchEvent(new SearchServiceErrorEvent("Not connected to storage", searchID, searchServiceIdO));
            return;
         }

         final QueryExecution qe = QueryExecutionFactory.create(query, this.dataset);
         try {
            final ResultSet rs = qe.execSelect();
            LOG.debug("Executed query successfully. Start processing query solutions");
            QuerySolution qs = null;
            RDFNode node = null;
            String idStr = "";
            String latStr = "";
            String longStr = "";
            Vector<String> solution;
            while (rs.hasNext()) {
               solution = new Vector<String>();
               qs = rs.next();
               node = qs.get("identifier");
               idStr = node.toString();
               solution.add(idStr);
               node = qs.get("lat");
               latStr = node.toString();
               solution.add(latStr);
               node = qs.get("long");
               longStr = node.toString();
               solution.add(longStr);
               results.add(solution);
               LOG.trace("Processed query solution: Identifier: " + idStr + ", Latitude: " + latStr + ", Longitude: " + longStr);
            }
         } catch (Exception e) {
            LOG.error("An error occured while processing search results: " + e.toString());
            callback.handleSearchEvent(new SearchServiceErrorEvent("An error occured while processing search results", searchID,
                  searchServiceIdO));
            return;
         } finally {
            qe.close();
         }
      } catch (Exception e) {
         LOG.error("An error occured while executing search query: " + e.toString());
         callback.handleSearchEvent(new SearchServiceErrorEvent("An error occured while executing search query", searchID,
               searchServiceIdO));
         return;
      } finally {
         this.model.leaveCriticalSection();
      }

      Set<Identifier> resultSet = new HashSet<Identifier>();

      // fine grain radius search
      double latRes = 0.0;
      double longRes = 0.0;
      Identifier identifier = null;
      double radiusParamInKM = radiusParam / 1000.0;

      for (Vector<String> result : results) {
         String lt = result.get(1).substring(7);
         try {
            latRes = Double.parseDouble(lt);
         } catch (NumberFormatException nfe) {
            LOG.error("Received latitude value from database could not be transformed into double-value. Value was: " + lt);
            continue;
         }

         String lng = result.get(2).substring(7);
         try {
            longRes = Double.parseDouble(lng);
         } catch (NumberFormatException nfe) {
            LOG.error("Received longitude value from database could not be transformed into double-value. Value was: " + lng);
            continue;
         }

         LOG.debug("Perform fine grain distance computation for: Latitude: " + latRes + ", Longitude: " + longRes
               + "Identifier: " + result.get(0));

         // FIXME: 1.8 is optimized for Paderborn
         if (radiusParamInKM > 1.8 * distance(latParam, longParam, latRes, longRes)) {
            LOG.debug("Distance smaller than radius (" + radiusParamInKM + ") => add identifier to result set.  ");
            identifier = this.datamodelFactory.createIdentifierFromString(result.get(0));
            resultSet.add(identifier);
         }
      }

      LOG.debug("finished search (id: " + searchID + "), handing over result to calling search controller");
      callback.handleSearchEvent(new SearchServiceResultEvent("search result of " + getIdentityObject().getName(), searchID,
            searchServiceIdO, resultSet));
   }

   /**
    * computes the distance between two geo positions
    * 
    * @param lat1
    *           latitude value of geo position 1
    * @param lon1
    *           longitude value of geo position 1
    * @param lat2
    *           latitude value of geo position 2
    * @param lon2
    *           longitude value of geo position 2
    * @return the distance between the two geo positions in kilometers
    */
   private double distance(final double lat1, final double lon1, final double lat2, final double lon2) {
      double theta = lon1 - lon2;
      double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1))
            * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
      dist = Math.acos(dist);
      dist = Math.toDegrees(dist);
      dist = dist * 60 * 1.1515;
      dist = dist * 1.609344;
      LOG.debug("The distance is " + dist + " kilometers");
      return (dist);
   }

   @Override
   public String describe() {
      return this.store != null ? "the RDF database " + this.store.getConnection() : "an RDF database";
   }

   /**
    * Shuts down this search service and its {@link SearchEsfConnector} if is running
    */
   public void tearDown() {
      LOG.trace(null);
      this.model.enterCriticalSection(false);
      try {
         if (this.dataset != null) {
            this.dataset.close();
         }
         if (this.model != null) {
            this.model.close();
         }
         if (this.store != null) {
            this.store.close();
         }
         if (this.conn != null) {
            this.conn.close();
         }
         if (this.searchEsfConnector != null) {
            this.searchEsfConnector.tearDown();
         }
      } finally {
         this.model.leaveCriticalSection();
      }
   }
}
