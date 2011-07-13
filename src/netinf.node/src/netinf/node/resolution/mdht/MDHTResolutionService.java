package netinf.node.resolution.mdht;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import netinf.common.communication.AtomicMessage;
import netinf.common.communication.Connection;
import netinf.common.communication.MessageEncoderXML;
import netinf.common.communication.TCPConnection;
import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.datamodel.rdf.DatamodelFactoryRdf;
import netinf.common.datamodel.translation.DatamodelTranslator;
import netinf.common.exceptions.NetInfResolutionException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.messages.RSMDHTAck;
import netinf.node.cache.network.NetworkCache;
import netinf.node.cache.peerside.PeerSideCache;
import netinf.node.resolution.AbstractResolutionService;
import netinf.node.resolution.mdht.dht.DHT;
import netinf.node.resolution.mdht.dht.DHTConfiguration;
import netinf.node.resolution.mdht.dht.pastry.FreePastryDHT;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;

import com.google.inject.Inject;

/**
 * Multi-level Distributed Hash table - Resolution Service
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class MDHTResolutionService extends AbstractResolutionService {

   private static final Logger LOG = Logger.getLogger(MDHTResolutionService.class);
   private static final String IDENTIFIER = "ni:name=value";
   private static final short NO_CACHING = 0;
   private static final short USE_NETWORK_CACHE = 1;
   private static final short USE_PEERSIDE_CACHE = 2;
   private DatamodelFactory datamodelFactory;
   private NetworkCache networkCache;
   private PeerSideCache peerSideCache;
   //private Map<Integer,InformationObject> openRequests;
   //private DatamodelFactoryRdf localRdfFactory;
   private MessageEncoderXML localXmlEncoder;
   private InetAddress localNodeIp;
   private ExecutorService executorService;
   
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

   @Inject(optional = true)
   public void setPeersideCache(PeerSideCache cache) {
      peerSideCache = cache;
      LOG.info("(MDHT ) MDHT node connected with Peer-side Cache");
   }
   @Inject
   public void setDatamodelFactory(DatamodelFactory factory) {
      datamodelFactory = factory;
   }

   /**
    * Constructor
    */
   @Inject
   
   public MDHTResolutionService(List<DHTConfiguration> configs, DatamodelFactoryRdf rdfFactory, MessageEncoderXML xmlEncoder) {
      super();
      //this.localRdfFactory = rdfFactory;
      this.localXmlEncoder = xmlEncoder;
      this.executorService = Executors.newFixedThreadPool(2);
      // Initialize requests hash
      //this.openRequests = new Hashtable<Integer, InformationObject>();
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
      try {
    	  this.localNodeIp = InetAddress.getLocalHost(); 
      } catch (UnknownHostException e) {
    	  LOG.error("(MDHT ) Weird, could not obtain local IP");
      }
      LOG.log(DemoLevel.DEMO, "(MDHT ) Starting MDHT RS with " + configs.size() + " levels");
   }

   private DHT createDHT(DHTConfiguration config) throws IOException {
      return new FreePastryDHT(config, this);
   }

   @Override
   public InformationObject get(Identifier identifier) {
      LOG.info("(MDHT ) Getting IO with Identifier " + identifier);
//      this.openRequests.put(key, value)
      InformationObject result = get(identifier, 0);
      
      if (result != null) {
    	  short sCacheToUse = null == this.peerSideCache ? ( null == this.networkCache ? NO_CACHING : USE_NETWORK_CACHE ) : USE_PEERSIDE_CACHE;
          LOG.info("(MDHT ) Found IO");
          //Cache it too, maybe do that on a separate thread asynchronously?
          if (result instanceof DataObject ) {
          switch ( sCacheToUse ) {
          case USE_PEERSIDE_CACHE:
        	  LOG.info("(MDHT ) Using Peer-side Cache");
        	  try {
        		  PeerSideCache dummyCache = (PeerSideCache)AsyncRunner.getInstance(PeerSideCache.class, peerSideCache, this.executorService);
        		  //peerSideCache.cache(result);
        		  dummyCache.cache(result);
        	  } catch (Exception e) {
        		  LOG.error("Naja, hat nicht geklappt mit dem Cachen. Weitermachen.");
        		  return result;
        	  }
        	  break;
          case USE_NETWORK_CACHE:
        	  LOG.info("(MDHT ) Using Network Cache");
        	  try {
        		  NetworkCache dummyCache = (NetworkCache)AsyncRunner.getInstance(NetworkCache.class, networkCache, this.executorService);
        		  //peerSideCache.cache(result);
        		  dummyCache.cache((DataObject) result);
        	  } catch (Exception e) {
        		  LOG.error("Naja, hat nicht geklappt mit dem Cachen. Weitermachen.");
        		  return result;
        	  }
        	  break;
          case NO_CACHING:
        	  LOG.info("(MDHT ) Will not cache this result. No cache active");
        	  break;
           }
          }
          return result;
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
         dhts.get(level).put(informationObject, level, dhts.size() - 1, this.localNodeIp.getAddress());
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
         dhts.get(level).put(io, level, levels - 1, this.localNodeIp.getAddress());
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
   
   /***
    * get method to search for a specific IO by using a commonapi ID. This method is meant
    * to be called from within a child DHT and not from within the MDHT itself
    * @param id The NetInf identifier object to search for
    * @param level The Ring/Level where the query should start
    * @return The IO or {@code null} if not found in either ring
    */
   public InformationObject get(Id id, int level) {
	   	   
	   InformationObject result = null;
	   if (level >= this.dhts.size()) {
		   return result;
	   }
	   DHT crtLevel = dhts.get(level);
	   
	   if (crtLevel != null) {
		   result = crtLevel.get(id, level);
	   } 
	   return result;
   }
   /***
    * get method to search for a specific IO by using a corresponding NetInf Identifier. The
    * method is only to be used internally in the MDHT
    * @param id The NetInf identifier object to search for
    * @param level The Ring/Level where the query should start
    * @return The IO or {@code null} if not found in either ring
    */
   private InformationObject get(Identifier id, int level) {
	   
	   InformationObject result = null;
	   if (level >= this.dhts.size()) { 
		   return result; 
	   }
	   DHT crtLevel = dhts.get(level);
	   
	   if (crtLevel != null) {
		   result = crtLevel.get(id, level);
	   }
	   return result;
   }
   
   
   public void switchRingUpwards(int nextLevel) {
	   LOG.info("(MDHT) Switching ring from " + (nextLevel - 1) + " to " + nextLevel);
	   //TODO: Not yet implemented
   }
   public void sendRemoteAck(final InetAddress targetNodeAddr) {
	   
	   try {
		   int serverPort = 5000;
		   Socket socket = new Socket();
		   
		   //Timeout is 1 second
		   socket.bind(null);
		   socket.connect(new InetSocketAddress(targetNodeAddr, serverPort), 1000);
		   Connection conn = new TCPConnection(socket);
		   
		   //Build NetInf Message
		   RSMDHTAck mdhtAckMsg = new RSMDHTAck();
		   
		   mdhtAckMsg.setPrivateKey(IDENTIFIER);
		   //Send message
		   LOG.info("(MDHT) Sending ACK to sender");
		   conn.send(new AtomicMessage(MessageEncoderXML.ENCODER_ID, this.localXmlEncoder.encodeMessage(mdhtAckMsg)));
	 
	   } catch (IOException e) {
			LOG.error("(MDHT) Could not open remote node Socket to " + e.getMessage());
			
		   }
   }
}
