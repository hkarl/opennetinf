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
package netinf.node.resolution.rdf;

import java.util.ArrayList;
import java.util.List;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.datamodel.rdf.DatamodelFactoryRdf;
import netinf.common.datamodel.rdf.DefinedRdfNames;
import netinf.common.datamodel.translation.DatamodelTranslator;
import netinf.common.exceptions.NetInfResolutionException;
import netinf.node.resolution.AbstractResolutionService;
import netinf.node.resolution.ResolutionService;
import netinf.node.search.rdf.SearchServiceRDF;

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
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * The RDF Resolution Service stores the information objects in their RDF representation in a local SDB database.
 * <p>
 * If the {@link SearchServiceRDF} is configured to use the same database, it can be used to search for information objects within
 * this resolution service.
 * <p>
 * Note: This resolution service does not publish put() or delete() operations to an event service.
 * 
 * @author PG Augnet 2, University of Paderborn
 * @see ResolutionService
 * @see AbstractResolutionService
 */
public class RDFResolutionService extends AbstractResolutionService implements ResolutionService {

   private static final Logger LOG = Logger.getLogger(RDFResolutionService.class);

   private final DatamodelFactory overallDatamodelFactory;

   private SDBConnection conn;
   private Store store;
   private Dataset dataset;
   private Model model;

   private final DatamodelTranslator datamodelTranslator;

   private final DatamodelFactoryRdf datamodelFactoryRdf;

   @Inject
   public RDFResolutionService(final DatamodelFactory dmfactory, final DatamodelTranslator datamodelTranslator,
         @Named("resolution_rdf_db_host") final String dbHost, @Named("resolution_rdf_db_port") final String dbPort,
         @Named("resolution_rdf_db_dbname") final String dbName, @Named("resolution_rdf_db_user") final String dbUser,
         @Named("resolution_rdf_db_pw") final String dbPassword, @Named("resolution_rdf_db_layout") final String dbLayout,
         @Named("resolution_rdf_db_type") final String dbType, @Named("resolution_rdf_db_driver") final String dbDriver) {
      super();
      LOG.trace(null);
      this.overallDatamodelFactory = dmfactory;
      this.datamodelTranslator = datamodelTranslator;
      this.datamodelFactoryRdf = datamodelTranslator.getDatamodelFactoryRdf();

      this.conn = null;
      this.store = null;
      this.dataset = null;
      this.model = null;

      if (dbType.equals("MySQL")) {
         try {
            final StoreDesc storeDesc = new StoreDesc(dbLayout, dbType);
            JDBC.loadDriver(dbDriver);
            final String jdbcURL = "jdbc:" + dbType.toLowerCase() + "://" + dbHost + ":" + dbPort + "/" + dbName;
            this.conn = new SDBConnection(jdbcURL, dbUser, dbPassword);
            this.store = SDBFactory.connectStore(conn, storeDesc);
            this.dataset = SDBFactory.connectDataset(this.store);
            this.model = SDBFactory.connectDefaultModel(this.store);
            this.model.setNsPrefix(DefinedRdfNames.NETINF_NAMESPACE_NAME, DefinedRdfNames.NETINF_RDF_SCHEMA_URI);
         } catch (Exception e) {
            LOG.error("The following error occured while trying to connect to SDB store: " + e.getMessage());
            LOG.error("Not connected to SDB store");
            return;
         }
         if (!this.store.isClosed()) {
            LOG.debug("Successfully connected to SDB store");
         } else {
            LOG.error("Could not establish connection to SDB store");
         }
      } else {
         LOG.error("Database type '" + dbType + "' not supported");
      }
   }

   @Override
   public void delete(final Identifier identifier) {
      LOG.trace(null);
      LOG.info("Deleting IO with identifier: " + identifier.toString());

      if (identifier.isVersioned()) {
         throw new NetInfResolutionException("Cannot delete versioned IO");
      }

      boolean ioToDeleteExists = false;
      Resource ioToDeleteResource = null;
      Resource transportResource;

      // get resource of information object to delete
      this.model.enterCriticalSection(false);
      try {
         if (this.store == null || this.store.isClosed()) {
            throw new NetInfResolutionException("Not connected to SDB store");
         }

         final ResIterator resIter = this.model.listSubjectsWithProperty(DatamodelFactoryRdf
               .getProperty(DefinedRdfNames.POINTER_TO_IO));

         while (resIter.hasNext()) {
            transportResource = resIter.next();

            List<Statement> ioStatements = transportResource.listProperties(
                  DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IO)).toList();

            if (ioStatements.size() == 1) {
               Resource ioResource = (Resource) ioStatements.get(0).getObject();

               if (ioResource.getURI() != null && ioResource.getURI().equals(identifier.toString())) {
                  List<Statement> typeStatements = transportResource.listProperties(
                        DatamodelFactoryRdf.getProperty(DefinedRdfNames.IO_TYPE)).toList();

                  if (typeStatements.size() == 1) {
                     ioToDeleteResource = ioResource;
                     ioToDeleteExists = true;
                     // remove type information of the io from the model
                     this.model.remove(typeStatements.get(0));
                     // remove transport resource from the model
                     this.model.remove(ioStatements.get(0));
                     break;
                  }
               }

            }
         }
         if (ioToDeleteExists) {
            // Now delete all the attributes
            removeProperties(ioToDeleteResource, this.model);
         }
      } finally {
         this.model.leaveCriticalSection();
      }

      if (!ioToDeleteExists) {
         LOG.debug("IO to delete does not exist");
      }

   }

   private void removeProperties(final RDFNode nodeToDelete, final Model modelToDeleteFrom) {
      LOG.trace(null);
      if (nodeToDelete.canAs(Resource.class)) {
         final Resource res = nodeToDelete.as(Resource.class);
         final StmtIterator stmtIter = res.listProperties();
         Statement stmt = null;
         while (stmtIter.hasNext()) {
            stmt = stmtIter.next();
            removeProperties(stmt.getObject(), modelToDeleteFrom);
            // delete statement from model
            modelToDeleteFrom.remove(stmt);
         }
      } else if (nodeToDelete.canAs(Literal.class)) {
         // nothing to do
      } else {
         throw new NetInfResolutionException("Found incompatible RDFNode while deleting information object from database");
      }
   }

   @Override
   public InformationObject get(final Identifier identifier) {
      LOG.trace(null);
      LOG.info("Getting IO with identifier: " + identifier.toString());

      // FIXME change to SPARQL query if possible: current solution presumably has no good performance

      Model resultModel = null;
      InformationObject resultIO = null;
      boolean ioFound = false;

      // get resource of requested Information Object
      this.model.enterCriticalSection(true);
      try {
         if (this.store == null || this.store.isClosed()) {
            throw new NetInfResolutionException("Not connected to SDB store");
         }

         final Identifier idToLookup = getIdToLookup(identifier);

         final ResIterator resIter = this.model.listSubjectsWithProperty(DatamodelFactoryRdf
               .getProperty(DefinedRdfNames.POINTER_TO_IO));
         Resource resultIoResource = null;

         while (resIter.hasNext()) {
            Resource transportResource = resIter.next();

            List<Statement> ioStatements = transportResource.listProperties(
                  DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IO)).toList();

            if (ioStatements.size() == 1) {
               Resource ioResource = (Resource) ioStatements.get(0).getObject();

               if (ioResource.getURI() != null && ioResource.getURI().equals(idToLookup.toString())) {
                  List<Statement> typeStatements = transportResource.listProperties(
                        DatamodelFactoryRdf.getProperty(DefinedRdfNames.IO_TYPE)).toList();

                  if (typeStatements.size() == 1) {
                     RDFNode node = typeStatements.get(0).getObject();

                     resultModel = this.datamodelFactoryRdf.createModelForInformationObject(node.toString());
                     resultIoResource = ioResource;
                     ioFound = true;
                     break;
                  }
               }

            }
         }

         if (ioFound) {
            // First set Identifier
            Resource createdIoResource = (Resource) resultModel.listObjectsOfProperty(
                  DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IO)).toList().get(0);
            ResourceUtils.renameResource(createdIoResource, resultIoResource.getURI());

            // Now set all the attributes, beginning be the resultModel
            addProperties(resultIoResource, resultModel);
         }
      } finally {
         this.model.leaveCriticalSection();
      }

      // And finally create the according information object
      if (ioFound) {
         InformationObject ioRdf = this.datamodelFactoryRdf.createInformationObjectFromModel(resultModel);

         if (this.overallDatamodelFactory == this.datamodelFactoryRdf) {
            resultIO = ioRdf;
         } else {
            // Translation necessary
            resultIO = this.datamodelTranslator.toImpl(ioRdf);
         }
      }

      return resultIO;
   }

   private void addProperties(final RDFNode nodeToCopy, final Model resultModel) {
      LOG.trace(null);
      if (nodeToCopy.canAs(Resource.class)) {
         final Resource res = nodeToCopy.as(Resource.class);
         final StmtIterator stmtIter = res.listProperties();
         Statement stmt = null;
         while (stmtIter.hasNext()) {
            stmt = stmtIter.next();
            resultModel.add(stmt);
            addProperties(stmt.getObject(), resultModel);
         }
      } else if (nodeToCopy.canAs(Literal.class)) {
         // Literal already added to model in a former step
      } else {
         throw new NetInfResolutionException("Found incompatible RDFNode while retrieving Information Object from database");
      }
   }

   @Override
   public List<Identifier> getAllVersions(final Identifier identifier) {
      LOG.trace(null);
      LOG.info("Getting all versions for identifier: " + identifier.toString());

      if (this.store == null || this.store.isClosed()) {
         throw new NetInfResolutionException("Not connected to SDB store");
      }

      if (!identifier.isVersioned()) {
         throw new NetInfResolutionException("Trying to get versions for unversioned identifier");
      }
      Identifier idWithoutVersion = createIdentifierWithoutVersion(identifier);
      List<Identifier> identifierList = new ArrayList<Identifier>();

      // construct sparql query
      String queryStr = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT ?member WHERE { <" + idWithoutVersion
            + "> rdfs:member ?member }";

      LOG.debug("Used query: " + queryStr);

      Query query = null;
      try {
         query = QueryFactory.create(queryStr);
      } catch (QueryParseException e) {
         throw new NetInfResolutionException(e);
      }

      this.model.enterCriticalSection(true);
      try {
         final QueryExecution qe = QueryExecutionFactory.create(query, this.dataset);
         try {
            final ResultSet rs = qe.execSelect();
            LOG.debug("Executed query successfully. Start processing query solutions");
            QuerySolution qs = null;
            RDFNode node = null;
            Identifier id = null;
            while (rs.hasNext()) {
               qs = rs.next();
               node = qs.get("member");
               id = this.overallDatamodelFactory.createIdentifierFromString(node.toString());
               identifierList.add(id);
               LOG.trace("Processed query solution: " + node.toString());
            }
         } finally {
            qe.close();
         }
      } finally {
         this.model.leaveCriticalSection();
      }

      if (!identifierList.isEmpty()) {
         return identifierList;
      } else {
         LOG.debug("No versions found");
         return null;
      }
   }

   @Override
   public void put(final InformationObject informationObject) {
      LOG.trace(null);
      LOG.info("Putting Information Object with identifier: " + informationObject.getIdentifier().toString());

      try {
         validateIOForPut(informationObject);
      } catch (IllegalArgumentException ex) {
         throw new NetInfResolutionException("Trying to put invalid Information Object", ex);
      }

      this.model.enterCriticalSection(false);
      try {

         if (this.store == null || this.store.isClosed()) {
            throw new NetInfResolutionException("Not connected to SDB store");
         }

         // if this is a modification of an information object, delete the old version first
         if (!informationObject.getIdentifier().isVersioned()) {
            final InformationObject ioToDelete = get(informationObject.getIdentifier());
            if (ioToDelete != null) {
               LOG.debug("This is a put for modification: old information object will be deleted first");
               if (ioToDelete.getDatamodelFactory() == this.datamodelFactoryRdf) {
                  this.model.remove(((Resource) ioToDelete.getWrappedObject()).getModel());
               } else {
                  // We have to translate that first
                  InformationObject rdf = this.datamodelTranslator.toRdf(ioToDelete);
                  this.model.remove(((Resource) rdf.getWrappedObject()).getModel());
               }
            }
         }

         // now put the new information object
         LOG.debug("Store information object");
         if (informationObject.getDatamodelFactory() == this.datamodelFactoryRdf) {
            this.model.add(((Resource) informationObject.getWrappedObject()).getModel());
         } else {
            // We have to translate that first
            InformationObject rdf = this.datamodelTranslator.toRdf(informationObject);
            this.model.add(((Resource) rdf.getWrappedObject()).getModel());
         }

         if (informationObject.getIdentifier().isVersioned()) {
            LOG.debug("Identifier is versioned => add to version list");
            Identifier idWithoutVersion = createIdentifierWithoutVersion(informationObject.getIdentifier());
            addToVersionList(idWithoutVersion, informationObject.getIdentifier());
         }
      } finally {
         this.model.leaveCriticalSection();
      }

   }

   private void addToVersionList(final Identifier idWithoutVersion, final Identifier identifierToAdd) {
      LOG.trace(null);
      // create sequence or if it already exists, get the existing one
      Seq sequence = this.model.createSeq(idWithoutVersion.toString());
      // add property; has an effect iff the sequence has just been created with the former command
      sequence.addProperty(RDF.value, DefinedRdfNames.NETINF_RDF_SCHEMA_URI + "idVersionList");
      sequence.add(identifierToAdd.toString());
   }

   @Override
   protected ResolutionServiceIdentityObject createIdentityObject() {
      ResolutionServiceIdentityObject identity = this.overallDatamodelFactory
            .createDatamodelObject(ResolutionServiceIdentityObject.class);
      identity.setName("RDFResolutionService");
      identity.setDefaultPriority(100);
      identity.setDescription("This is a rdf resolution service");
      return identity;
   }

   @Override
   public String describe() {
      return this.store != null ? "the RDF database " + this.store.getConnection() : "an RDF database";
   }

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
      } finally {
         this.model.leaveCriticalSection();
      }
   }
}
