package netinf.database.sdb;

import java.sql.SQLException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.util.StoreUtils;

/**
 * Factory providing SDB Stores connected to a database.
 * 
 * @author PG NetInf 3, University of Paderborn
 *
 */
@Singleton
public class SDBStoreFactory {
   
   private final LayoutType LAYOUT_TYPE = LayoutType.LayoutTripleNodesHash;
   
   private String dbType;
   private String dbName;
   private String host;
   private String user;
   private String password;
   private String driver;
   private String jdbcURL;
   
   private DatabaseType databaseType;
   
   @Inject
   public SDBStoreFactory(@Named("rdf.db.sdbType") String type, @Named("rdf.db.sdbName") String name) {
      this.dbType = type;
      this.dbName = name;
   }
   
   @Inject(optional = true)
   public void setHost(@Named("rdf.db.sdbHost") String host) {
      this.host = host;
   }
   
   @Inject(optional = true)
   public void setUser(@Named("rdf.db.sdbUser") String user) {
      this.user = user;
   }
   
   @Inject(optional = true)
   public void setPassword(@Named("rdf.db.sdbPassword") String pwd) {
      this.password = pwd;
   }
   
   @Inject(optional = true)
   public void setDriver(@Named("rdf.db.driver") String driver) {
      this.driver = driver;
   }
   
   @Inject(optional = true)
   public void setJdbcURL(@Named("rdf.db.jdbcURL") String jdbcURL) {
      this.jdbcURL = jdbcURL;
   }
   
   public Store createStore() throws SQLException {
      // Load driver once
      if (databaseType == null) {
         databaseType = DatabaseType.fetch(dbType);
         JDBC.loadDriver(JDBC.getDriver(databaseType));
      }
      // SDBConnection
      SDBConnectionDesc connDesc = SDBConnectionDesc.blank();
      connDesc.setName(dbName);
      connDesc.setType(dbType);
      connDesc.setHost(host);
      connDesc.setUser(user);
      connDesc.setPassword(password);
      connDesc.setDriver(driver);
      connDesc.setJdbcURL(jdbcURL);
      SDBConnection conn = SDBFactory.createConnection(connDesc);
      // StoreDesc
      StoreDesc storeDesc = new StoreDesc(LAYOUT_TYPE, databaseType);
      // Connect and format store
      Store store = SDBFactory.connectStore(conn, storeDesc);
      if (!StoreUtils.isFormatted(store)) {
         store.getTableFormatter().create();
      }
      return store;
   }
   
}