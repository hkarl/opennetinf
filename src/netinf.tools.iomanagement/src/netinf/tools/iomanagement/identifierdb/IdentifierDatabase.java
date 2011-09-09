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
package netinf.tools.iomanagement.identifierdb;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import netinf.common.datamodel.Identifier;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.utils.DatamodelUtils;
import netinf.tools.iomanagement.Constants;

import org.apache.log4j.Logger;

/**
 * an identifier database located in the users home-directory (e.g. /home/user/.netinf)
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IdentifierDatabase {
   /** Log4j Logger component */
   private static final Logger log = Logger.getLogger(IdentifierDatabase.class);

   /** name of the Identifier Database */
   private static final String DB_NAME = "ManagementToolIdentifiers";
   /** name of the driver used to open the database */
   private static final String DRIVER_NAME = "org.apache.derby.jdbc.EmbeddedDriver";

   /** prepared String for table creation */
   private static final String STR_CREATE_IDENTIFIER_TABLE = "CREATE TABLE APP.IDENT (ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), IDENTIF LONG VARCHAR, TYP VARCHAR(3))";
   /** prepared String for Identifier retrieval */
   private static final String STR_GET_IDENTIFIERS = "SELECT ID, IDENTIF, TYP FROM APP.IDENT ORDER BY TYP ASC";
   /** prepared String for Identifier insertion */
   private static final String STM_INSERT = "INSERT INTO APP.IDENT (IDENTIF, TYP) VALUES (?, ?)";

   /** connection to the database */
   private Connection dbConnection;
   /** is this database component connected? */
   private boolean isConnected;

   /**
    * connects this IdentifierDatabase to its SQL database
    * 
    * @return true iff connection was established
    */
   public boolean connect() {
      log.trace(Constants.LOG_ENTER);

      try {
         this.dbConnection = DriverManager.getConnection(getDatabaseUrl());
         this.isConnected = this.dbConnection != null;
      } catch (SQLException ex) {
         this.isConnected = false;
      }

      log.trace(Constants.LOG_EXIT);
      return isConnected();
   }

   /**
    * creates an empty database
    * 
    * @return true iff creation was successful
    */
   private boolean createDatabase() {
      log.trace(Constants.LOG_ENTER);

      boolean bCreated = false;
      this.dbConnection = null;

      String dbUrl = getDatabaseUrl();
      Properties dbProperties = new Properties();
      dbProperties.put("create", "true");

      try {
         log.info("No Identifer Database found, creating it");
         this.dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
         bCreated = createTables();
      } catch (SQLException ex) {
         log.error("Error while creating DB ", ex);
      } finally {
         try {
            if (this.dbConnection != null) {
               this.dbConnection.close();
            }
         } catch (SQLException ex) {
            log.error("Error closing SQL Connection: " + ex.getMessage());
         }
      }

      dbProperties.remove("create");

      log.trace(Constants.LOG_EXIT);
      return bCreated;
   }

   /**
    * creates the tables in the database
    * 
    * @return true iff creation was successful
    */
   private boolean createTables() {
      log.trace(Constants.LOG_ENTER);

      boolean bCreatedTables = false;

      Statement statement = null;

      try {
         statement = this.dbConnection.createStatement();
         statement.execute(STR_CREATE_IDENTIFIER_TABLE);
         statement.close();
         bCreatedTables = true;
      } catch (SQLException ex) {
         ex.printStackTrace();
      } finally {
         try {
            if (statement != null) {
               statement.close();
            }
         } catch (SQLException ex) {
            log.error("Error closing SQL Statement: " + ex);
         }
      }

      log.trace(Constants.LOG_EXIT);
      return bCreatedTables;
   }

   /**
    * @return true iff database exists
    */
   private boolean dbExists() {
      log.trace(Constants.LOG_ENTER);

      boolean bExists = false;
      String dbLocation = getDatabaseLocation();
      File dbFileDir = new File(dbLocation);
      if (dbFileDir.exists()) {
         bExists = true;
      }

      log.trace(Constants.LOG_EXIT);
      return bExists;
   }

   /**
    * disconnects this IdentifierDatabase from the SQL database
    */
   public void disconnect() {
      log.trace(Constants.LOG_ENTER);

      if (isConnected()) {
         String dbUrl = getDatabaseUrl();
         Properties dbProperties = new Properties();
         dbProperties.put("shutdown", "true");
         try {
            DriverManager.getConnection(dbUrl, dbProperties);
         } catch (SQLException ex) {
            log.error("SQL error in IdentifierDatabase: " + ex.getMessage());
         }
         this.isConnected = false;
      }

      log.trace(Constants.LOG_EXIT);
   }

   /**
    * @return (system) location of database
    */
   public String getDatabaseLocation() {
      log.trace(Constants.LOG_ENTER);

      String dbLocation = System.getProperty("derby.system.home") + "/" + IdentifierDatabase.DB_NAME;

      log.trace(Constants.LOG_EXIT);
      return dbLocation;
   }

   /**
    * @return URL of database
    */
   public String getDatabaseUrl() {
      log.trace(Constants.LOG_ENTER);

      String dbUrl = "jdbc:derby:" + IdentifierDatabase.DB_NAME;

      log.trace(Constants.LOG_EXIT);
      return dbUrl;
   }

   /**
    * returns all identifiers with a given type, valid types are <b>cre</b>(ated yourself)/<b>sea</b>(rched for)/<b>man</b>(ually
    * added)
    * 
    * @return identifiers with given type
    */
   public List<String[]> getIdentifiersWithType() {
      log.trace(Constants.LOG_ENTER);

      if (!isConnected()) {
         connect();
      }
      if (isConnected()) {

         List<String[]> listEntries = new ArrayList<String[]>();
         ResultSet results = null;

         Statement queryStatement = null;
         try {
            queryStatement = this.dbConnection.createStatement();
            results = queryStatement.executeQuery(STR_GET_IDENTIFIERS);
            while (results.next()) {
               String identifierString = results.getString(2);
               String type = results.getString(3);

               if (DatamodelUtils.isValidIdentifierString(identifierString)) {
                  String[] entry = { type, identifierString };
                  listEntries.add(entry);
               }
            }
         } catch (SQLException sqle) {
            log.error("Error while retrieving Identifiers from database " + sqle);
         } finally {
            try {
               if (queryStatement != null) {
                  queryStatement.close();
               }
               if (results != null) {
                  results.close();
               }
            } catch (SQLException ex) {
               log.error("Error closing SQL Object: " + ex);
            }
         }

         log.trace(Constants.LOG_EXIT);
         return listEntries;
      }
      log.error("Could not retrieve identifiers from database");
      log.trace(Constants.LOG_EXIT);
      return null;

   }

   /**
    * initializes the database
    */
   public void init() {
      setDBSystemDir();
      loadDatabaseDriver(DRIVER_NAME);

      log.log(DemoLevel.DEMO, "(GUI  ) I'm connecting to my user's identifier database");
      if (!dbExists()) {
         log.log(DemoLevel.DEMO, "(GUI  ) My user has no identifier database, creating one");
         createDatabase();
      }
   }

   /**
    * @return true iff this IdentifierDatabase is connected to the SQL database
    */
   public boolean isConnected() {
      return this.isConnected;
   }

   /**
    * loads the SQL database driver
    * 
    * @param driverName
    *           name of the driver
    */
   private void loadDatabaseDriver(String driverName) {
      log.trace(Constants.LOG_ENTER);

      try {
         Class.forName(driverName);
      } catch (ClassNotFoundException ex) {
         log.error("Unable to load database driver ", ex);
      }

      log.trace(Constants.LOG_EXIT);
   }

   /**
    * adds an identifier to the database, valid types are <b>cre</b>(ated yourself)/<b>sea</b>(rched for)/<b>man</b>(ually added)
    * 
    * @param identifier
    *           identifier to add
    * @param type
    *           type (cre/sea/man)
    * @return true iff it worked
    */
   public boolean putIdentifier(Identifier identifier, String type) {
      log.trace(Constants.LOG_ENTER);

      if (!isConnected()) {
         connect();
      }
      if (isConnected()) {

         if ("cre".equals(type) || "sea".equals(type) || "man".equals(type)) {
            ResultSet resultset = null;
            try {

               for (String[] ident : getIdentifiersWithType()) {
                  if (ident[1].equals(identifier.toString())) {
                     log.debug("Identifier " + identifier + " already in Database");
                     log.trace(Constants.LOG_EXIT);
                     return false;
                  }
               }

               PreparedStatement stmtSaveNewRecord = this.dbConnection.prepareStatement(STM_INSERT,
                     Statement.RETURN_GENERATED_KEYS);
               stmtSaveNewRecord.clearParameters();
               stmtSaveNewRecord.setString(1, identifier.toString());
               stmtSaveNewRecord.setString(2, type);
               stmtSaveNewRecord.executeUpdate();
               resultset = stmtSaveNewRecord.getGeneratedKeys();
               if (resultset.next()) {
                  log.info("added " + identifier + " to list of known identifiers");
               }

            } catch (SQLException sqle) {
               sqle.printStackTrace();
            } finally {
               try {
                  if (resultset != null) {
                     resultset.close();
                  }
               } catch (SQLException ex) {
                  log.error("Error closing SQL ResultSet: " + ex);
               }
            }

         } else {
            log.error("Identifier type " + type + " not allowed in database");
         }
      }
      return true;
   }

   /**
    * deletes a given identifier from the database
    * 
    * @param deleteIdentifierString
    *           identifier to delete (String representation)
    */
   public void remove(String deleteIdentifierString) {
      try {
         this.dbConnection.prepareStatement("DELETE FROM APP.IDENT WHERE IDENTIF LIKE '" + deleteIdentifierString + "'")
         .execute();
      } catch (SQLException e) {
         throw new NetInfUncheckedException(e);
      }

   }

   /**
    * sets the location for the database
    */
   private void setDBSystemDir() {
      log.trace(Constants.LOG_ENTER);

      String userHomeDir = System.getProperty("user.home", ".");
      String systemDir = userHomeDir + "/.netinf";
      System.setProperty("derby.system.home", systemDir);

      File fileSystemDir = new File(systemDir);
      fileSystemDir.mkdir();

      log.trace(Constants.LOG_EXIT);
   }

}
