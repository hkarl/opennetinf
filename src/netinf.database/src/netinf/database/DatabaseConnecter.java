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
package netinf.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * This class encapsulates the natural sql-{@link Connection}, and sets up this connection appropriatelly.
 * 
 * @author PG Augnet 2, University of Paderborn.
 */
public class DatabaseConnecter {

   private static final Logger LOG = Logger.getLogger(DatabaseConnecter.class);

   private final DatabaseData databaseData;
   private Connection connection;

   @Inject
   public DatabaseConnecter(DatabaseData databaseData) throws ClassNotFoundException, SQLException {
      LOG.trace(null);

      this.databaseData = databaseData;
   }

   public boolean setup() {
      LOG.trace(null);

      boolean result = true;

      try {
         // Register the JDBC driver for MySQL.
         Class.forName("com.mysql.jdbc.Driver");
         LOG.debug("Successfully loaded database driver");
      } catch (ClassNotFoundException e) {
         LOG.error("Could not find jdbc driver for mysql database", e);

         result = false;
      }

      String host = databaseData.getHost();
      String port = databaseData.getPort();
      String db = databaseData.getDatabaseName();
      String user = databaseData.getUser();
      String password = databaseData.getPassword();

      String urlToDatabase = createURLToDatabase(host, port, db);
      try {
         connection = DriverManager.getConnection(urlToDatabase, user, password);
         LOG.debug("Successfully connected to database '" + db + "'");
      } catch (SQLException e) {
         LOG.error("Could not connect to database '" + db + "' with username '" + user + "' and password '" + password + "'", e);

         result = false;
      }

      return result;
   }

   public Connection getConnection() {
      return connection;
   }

   public void close() throws SQLException {
      LOG.debug("The connection was closed");
      connection.close();
   }

   private String createURLToDatabase(String host, String port, String dbName) {
      String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
      return url;
   }
}
