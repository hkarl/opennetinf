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

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A simple holder for the pieces of inforamtion needed for the database.
 * 
 * @author PG Augnet 2, University of Paderborn.
 */
public class DatabaseData {

   private String host;
   private String port;
   private String databaseName;
   private String user;
   private String password;

   @Inject
   public DatabaseData(@Named("database.host") String host, @Named("database.port") String port,
         @Named("database.name") String databaseName, @Named("database.user") String user,
         @Named("database.password") String password) {
      this.host = host;
      this.port = port;
      this.databaseName = databaseName;
      this.user = user;
      this.password = password;
   }

   public DatabaseData() {
      host = null;
      port = null;
      databaseName = null;
      user = null;
      password = null;
   }

   public String getHost() {
      return host;
   }

   public String getPort() {
      return port;
   }

   public String getDatabaseName() {
      return databaseName;
   }

   public String getUser() {
      return user;
   }

   public String getPassword() {
      return password;
   }

   public void setHost(String host) {
      this.host = host;
   }

   public void setPort(String port) {
      this.port = port;
   }

   public void setDatabaseName(String database) {
      databaseName = database;
   }

   public void setUser(String username) {
      user = username;
   }

   public void setPassword(String password) {
      this.password = password;
   }
}
