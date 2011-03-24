package netinf.node.resolution.mdht;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Hashtable;
import java.util.List;


import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.datamodel.translation.DatamodelTranslator;
import netinf.common.log.demo.DemoLevel;
import netinf.node.resolution.AbstractResolutionService;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.IdFactory;
import rice.p2p.past.PastContentHandle;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Multi-level Distributed Hash tables - Resolution Service
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class MDHTResolutionService extends AbstractResolutionService {

   private static final Logger LOG = Logger.getLogger(MDHTResolutionService.class);
   private DatamodelFactory datamodelFactory;
   private DatamodelTranslator translator;

   // server for remote-method-invocation (put/get on remote node)
   private RMIServerStub rmiServer;
   
   // delegator class for building delegates
   private final Delegator MY_DEL = new Delegator();

   // table of MDHT levels
   private Hashtable<Integer, DHT> levels = new Hashtable<Integer, DHT>();

   // runtime-dummyStorage for IOs
   private Hashtable<String, InformationObject> storage = new Hashtable<String, InformationObject>();

   // property-fields
   private int numberOfLevels;
   private String joinNode;
   private int joinAtLevel;
   private int basePort;
   private Hashtable<Identifier, PastContentHandle> storedContentHandles;
   
   // event handlers
   private EventDelegate passToNextLevelHandler;
   
   
   

   @Inject
   public void setDatamodelFactory(DatamodelFactory factory) {
      datamodelFactory = factory;
   }

   @Inject
   public void setDataModelTranslator(DatamodelTranslator translator) {
      this.translator = translator;
   }

   /**
    * Constructor
    */
   @Inject
   public MDHTResolutionService(@Named("numberOfLevels") final String myNumberOfLevels,
         @Named("joinNode") final String myJoinNode, @Named("joinAtLevel") final String myJoinAtLevel,
         @Named("basePort") final String myBasePort) {
      super();

      // setting fields
      this.numberOfLevels = Integer.parseInt(myNumberOfLevels);
      this.joinNode = myJoinNode;
      this.joinAtLevel = Integer.parseInt(myJoinAtLevel);
      this.basePort = Integer.parseInt(myBasePort);
      this.storedContentHandles = new Hashtable<Identifier, PastContentHandle>();
      
      LOG.log(DemoLevel.DEMO, "(MDHT ) Starting MDHT RS with " + numberOfLevels + " levels");
      
      //Event wireup section
      this.passToNextLevelHandler = MY_DEL.build(this, "moveUpInRing"); 

      // create necessary levels
      if (joinAtLevel == 0) { // create all levels
         for (int i = 1; i <= numberOfLevels; i++) {
            LOG.log(DemoLevel.DEMO, "(MDHT ) Creating DHT ring at level " + i);
            levels.put(i, createDHT(42, basePort + i));
         }
      } else if (joinAtLevel > 0) { // only levels below the join-level
         int levelsToCreate = joinAtLevel - 1; // how many levels to create
         for (int i = 1; i <= levelsToCreate; i++) {
            LOG.log(DemoLevel.DEMO, "(MDHT ) Creating DHT ring at level " + i);
            levels.put(i, createDHT(42, basePort + i));
         }
      }

      // joining
      if (joinAtLevel > 0) {
         while (joinAtLevel <= numberOfLevels) {
            LOG.log(DemoLevel.DEMO, "(MDHT ) Joining node " + joinNode + " on level " + joinAtLevel);
            InetAddress bootstrap = null;
            try {
               bootstrap = InetAddress.getByName(joinNode);
            } catch (UnknownHostException e) {
               LOG.error("Could not get address of to-join-node");
            }
            levels.put(joinAtLevel, createDHT(42, bootstrap, basePort + joinAtLevel));
            joinAtLevel++;
         }
      }

   }

   /**
    * Initializes the RMI server. provides remote put/get
    */
   @SuppressWarnings("unused")
   @Inject
   private void initRMIServer(DatamodelFactory factory) {
      LOG.log(DemoLevel.DEMO, "(MDHT ) Initializing RMI Server");

      try {
         LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
      } catch (RemoteException e) {
         LOG.error(e.getMessage());
      }

      try {
         rmiServer = new RMIServerStub(this, factory);
         Naming.rebind("MDHTServer", rmiServer);

      } catch (RemoteException e) {
         LOG.error(e.getMessage());
      } catch (MalformedURLException e) {
         LOG.error(e.getMessage());
      }
   }

   /**
    * wrapper for creating a DHT ring
    * 
    * @return DHT
    */
   private DHT createDHT(int id, InetAddress bootstrapAddress, int port) {
      return new FreePastryDHT(id, bootstrapAddress, port, this);
   }

   private DHT createDHT(int id, int port) {
      return new FreePastryDHT(id, null, port, this);
   }

   /*
    * (non-Javadoc)
    * @see netinf.node.resolution.ResolutionService#get(netinf.common.datamodel.Identifier)
    */
   @Override
   public InformationObject get(Identifier identifier) {
      LOG.info("(MDHT ) Getting IO with Identifier " + identifier);

      Identifier identN = translator.toImpl(identifier);
      LOG.trace(null);
      return get(identN, 1, numberOfLevels);
   }

   public InformationObject get(Identifier ident, int level, int maxLevel) {
      LOG.trace(null);
      DHT ring = levels.get(level);
      LOG.trace(null);
      if (ring != null) {
         //InetSocketAddress address = ring.getResponsibleNode(ident);
         LOG.log(DemoLevel.DEMO, "(MDHT-GET) Retrieving content from " + ident);
         PastContentHandle contentKey = null;
         contentKey = this.storedContentHandles.get(ident);
	 ring.get(contentKey);
         return ring.getReturnedIOFromPast();
         //return getRemote(address.getAddress().getHostAddress(), ident, level, maxLevel);
      }
      return null;
   }

   private InformationObject getRemote(String address, Identifier ident, int level, int maxLevel) {
      try {
         Remote remoteObj = Naming.lookup("//" + address + "/MDHTServer");
         LOG.trace(null);
         RemoteRS stub = (RemoteRS) remoteObj;
         LOG.trace(null);
         return stub.getRemote(ident.serializeToBytes(), level, maxLevel);

      } catch (MalformedURLException e) {
         LOG.error(e.getMessage());
      } catch (RemoteException e) {
         LOG.error(e.getMessage());
      } catch (NotBoundException e) {
         LOG.error(e.getMessage());
      }

      return null;
   }

   /*
    * (non-Javadoc)
    * @see netinf.node.resolution.ResolutionService#getAllVersions(netinf.common.datamodel.Identifier)
    */
   @Override
   public List<Identifier> getAllVersions(Identifier identifier) {
      LOG.info("(MDHT ) Getting all Versions with Identifier " + identifier);

      // TODO Auto-generated method stub
      return null;
   }

   /*
    * Default put: put on every level
    */
   @Override
   public void put(InformationObject io) {
      LOG.log(DemoLevel.DEMO, "(MDHT ) Putting IO with Identifier: " + io.getIdentifier() + " on all levels");
      InformationObject ioN = translator.toImpl(io);
      
      // try {
      // validateIOForPut(ioN);
      // } catch (IllegalArgumentException ex) {
      // throw new NetInfResolutionException("Trying to put unvalid information object", ex);
      // }

      this.put(ioN, 1, numberOfLevels);
   }

   /**
    * @param address
    * @param io
    * @param fromLevel
    * @param toLevel
    */
   private void putRemote(String address, InformationObject io, int fromLevel, int toLevel) {
      try {
         // System.setSecurityManager(new RMISecurityManager());
         // requires additional policies

         Remote remoteObj = Naming.lookup("//" + address + "/MDHTServer");
         LOG.debug(null);
         RemoteRS stub = (RemoteRS) remoteObj;
         LOG.debug(null);
         stub.putRemote(io.serializeToBytes(), fromLevel, toLevel);
         LOG.debug(null);
      } catch (MalformedURLException e) {
         LOG.error(e.getMessage());
      } catch (RemoteException e) {
         LOG.error(e.getMessage());
      } catch (NotBoundException e) {
         LOG.error(e.getMessage());
      }
   }

   public void put(InformationObject io, int fromLevel, int toLevel) {
      // ring of this level
      DHT ring = levels.get(fromLevel);
      //InetSocketAddress address = ring.getResponsibleNode(io);
      LOG.log(DemoLevel.DEMO, "(MDHT ) Putting io in " + fromLevel);
      //putRemote(address.getAddress().getHostAddress(), io, fromLevel, toLevel);
      this.storedContentHandles.put(io.getIdentifier(), ring.put(io));
   }

   /**
    * Stores a given InformationObject
    * 
    * @param io
    *           the InformationObject that has to be stored
    */
   void storeIO(InformationObject io) {
      if (!storage.contains(io.getIdentifier().toString())) {
         storage.put(io.getIdentifier().toString(), io);
      }
   }

   InformationObject getFromStorage(Identifier ident) {
      LOG.info("getFromStorage" + ident);
      return storage.get(ident.toString());
   }

   /*
    * (non-Javadoc)
    * @see netinf.node.resolution.ResolutionService#delete(netinf.common.datamodel.Identifier)
    */
   @Override
   public void delete(Identifier identifier) {
      // TODO Auto-generated method stub
   }

   /*
    * Description of this RS
    */
   @Override
   public String describe() {
      return "a mdht";
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

   public Thread getPastryNode() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * @see netinf.node.resolution.AbstractResolutionService#setIdFactory(rice.p2p.commonapi.IdFactory)
    */
   @Override
   public void setIdFactory(IdFactory idFactory) {
      super.setIdFactory(idFactory);
   }
   
   /*
    * Section for the signaling interface used by DHT child rings to notify the parent of various events 
    */   
   public void signalPassToNextLevel()
   {
      if(this.passToNextLevelHandler != null)
      {
	 // Execute the action associated with this event handler. No handling code should be written directly
	 // in this method. Otherwise a velociraptor will eat you. You have been warned.
	 this.passToNextLevelHandler.action();
      }
      else
      {
	 LOG.warn("No event handler for this event was registered in the MDHT. Signal will remain DHT local");
      }
   }

   public void moveUpInRing()
   {
      LOG.debug("Moving up in ring");
   }
}
