package netinf.node.resolution.mdht;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.datamodel.translation.DatamodelTranslator;
import netinf.common.exceptions.NetInfResolutionException;
import netinf.common.log.demo.DemoLevel;
import netinf.node.cache.network.NetworkCache;
import netinf.node.resolution.AbstractResolutionService;
import netinf.node.resolution.mdht.dht.DHT;
import netinf.node.resolution.mdht.dht.DHTConfiguration;
import netinf.node.resolution.mdht.dht.pastry.FreePastryDHT;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * Multi-level Distributed Hash table - Resolution Service
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class MDHTResolutionService extends AbstractResolutionService {

   private static final Logger LOG = Logger.getLogger(MDHTResolutionService.class);
   private DatamodelFactory datamodelFactory;
   private NetworkCache networkCache;
   
   // table of MDHT levels
   private Map<Integer, DHT> dhts = new Hashtable<Integer, DHT>();
   
   private DatamodelTranslator translator;

   @Inject
   public void setDatamodelTranslator(DatamodelTranslator translator) {
      this.translator = translator;
   }


   @Inject(optional = true)
   public void setCache(NetworkCache cache) {
      networkCache = cache;
      if (!networkCache.isConnected()) {
         networkCache = null;
         LOG.warn("(MDHT ) CachingModule loaded, but server not reachable...");
      } else {
         LOG.info("(MDHT ) MDHT node connected with cache server");
      }
   }

   @Inject
   public void setDatamodelFactory(DatamodelFactory factory) {
      datamodelFactory = factory;
   }

   /**
    * Constructor
    */
   @Inject
   public MDHTResolutionService(List<DHTConfiguration> configs) {
      super();
      // Create DHTs
      for (DHTConfiguration config : configs) {
         try {
            dhts.put(config.getLevel(), createDHT(config));
         } catch (IOException e) {
            LOG.error("(MDHT ) Could not create DHT at level " + config.getLevel() + ", break");
            break;
         }            
      }
      // Join each DHT
      for (int level : dhts.keySet()) {
         try {
            dhts.get(level).join();
         } catch (InterruptedException e) {
            LOG.error("(MDHT ) Could not join DHT at level " + level);
            LOG.debug(e);
         }
      }
      LOG.log(DemoLevel.DEMO, "(MDHT ) Starting MDHT RS with " + configs.size() + " levels");
   }

   private DHT createDHT(DHTConfiguration config) throws IOException {
      return new FreePastryDHT(config);
   }

   @Override
   public InformationObject get(Identifier identifier) {
      LOG.info("(MDHT ) Getting IO with Identifier " + identifier);
      for (int level = 0; level < dhts.size(); level++) {
         InformationObject result = dhts.get(level).get(identifier);
         if (result != null) {
            LOG.info("(MDHT ) Found IO at level " + level);
            return result;
         }
      }
      return null;
   }

   @Override
   public List<Identifier> getAllVersions(Identifier identifier) {
      LOG.info("(MDHT ) Getting all Versions with Identifier " + identifier);
      InformationObject io = get(identifier);
      if (io != null) {
        List<Identifier> ids = new ArrayList<Identifier>();
        ids.add(io.getIdentifier());
        return ids;
      } else {
        throw new NetInfResolutionException("Could not get all versions");
      }
   }

   /*
    * Default put: put on every level
    */
   @Override
   public void put(InformationObject io) {
      LOG.log(DemoLevel.DEMO, "(MDHT ) Putting IO with Identifier: " + io.getIdentifier() + " on all levels");
      InformationObject informationObject = this.translator.toImpl(io);
      try {
         validateIOForPut(informationObject);
      } catch (IllegalArgumentException ex) {
         throw new NetInfResolutionException("Trying to put invalid Information Object", ex);
      }
      
      for (int level = 0; level < dhts.size(); level++) {
         dhts.get(level).put(informationObject);
         LOG.info("(MDHT ) Put IO at level " + level);
      }
   }

   public void put(InformationObject io, int maxLevel) {
      LOG.log(DemoLevel.DEMO, "(MDHT ) Putting IO with Identifier: " + io.getIdentifier() + " upto level " + maxLevel);
      int levels = maxLevel;
      if (levels > dhts.size()) {
         levels = dhts.size();
      }
      for (int level = 0; level < levels; level++) {
         dhts.get(level).put(io);
         LOG.info("(MDHT ) Put IO at level " + level);
      }
   }

   @Override
   public void delete(Identifier identifier) {
      // TODO Auto-generated method stub
   }

   @Override
   public String describe() {
      return "MDHT Resolution Server";
   }

   /*
    * (non-Javadoc)
    * @see netinf.node.resolution.AbstractResolutionService#createIdentityObject()
    */
   @Override
   protected ResolutionServiceIdentityObject createIdentityObject() {
      ResolutionServiceIdentityObject identity = datamodelFactory.createDatamodelObject(ResolutionServiceIdentityObject.class);
      identity.setName("MDHT Resolution Service");
      identity.setDefaultPriority(70);
      identity.setDescription("This is a mdht resolution service running on "); // TODO ? ...on what?
      return identity;
   }
   
}
