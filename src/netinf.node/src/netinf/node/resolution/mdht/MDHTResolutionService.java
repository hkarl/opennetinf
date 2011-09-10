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

import netinf.common.communication.AtomicMessage;
import netinf.common.communication.Connection;
import netinf.common.communication.MessageEncoderXML;
import netinf.common.communication.TCPConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.datamodel.rdf.DatamodelFactoryRdf;
import netinf.common.datamodel.translation.DatamodelTranslator;
import netinf.common.exceptions.NetInfResolutionException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.messages.RSMDHTAck;
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

   /***
    * Logger object - used by log4j.
    */
   private static final Logger LOG = Logger.getLogger(MDHTResolutionService.class);
   
   
   private static final String IDENTIFIER = "ni:name=value";
   /**
    * The data model factory for IOs. This is injected via Guice. 
    */
   private DatamodelFactory datamodelFactory;
   
   private MessageEncoderXML localXmlEncoder;
   private InetAddress localNodeIp;
   
   /**
    *  The table of MDHT levels. Each level is a DHT object.
    */
   private Map<Integer, DHT> dhts = new Hashtable<Integer, DHT>();

   private DatamodelTranslator translator;

   /**
    * Inject the DatamodelTranslator via Google Guice. This is needed for
    * Java/Rdf format change.
    * @param translator The translator object.
    */
   @Inject
   public void setDatamodelTranslator(DatamodelTranslator translator) {
      this.translator = translator;
   }

   /**
    * Inject the DatamodelFactory via Google Guice.
    * @param factory The factory object.
    */
   @Inject
   public void setDatamodelFactory(DatamodelFactory factory) {
      datamodelFactory = factory;
   }

   /**
    * Parameterized constructor. This is also used by Guice when instantiating the class.
    * @param configs The list of configuration objects. See DHTConfiguration.
    * @param rdfFactory The injected DatamodelFactory for Rdf serialization.
    * @param xmlEncoder The message encoder object, XML format.
    */
   @Inject
   public MDHTResolutionService(List<DHTConfiguration> configs, DatamodelFactoryRdf rdfFactory, MessageEncoderXML xmlEncoder) {
      super();
      // this.localRdfFactory = rdfFactory;
      localXmlEncoder = xmlEncoder;
      
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
         localNodeIp = InetAddress.getLocalHost();
      } catch (UnknownHostException e) {
    	 // This happens sometimes on linux hosts with raw IPs, just use hostnames most of the time. 
         LOG.error("(MDHT ) Weird, could not obtain local IP");
      }
      LOG.log(DemoLevel.DEMO, "(MDHT ) Starting MDHT RS with " + configs.size() + " levels");
   }

   private DHT createDHT(DHTConfiguration config) throws IOException {
      return new FreePastryDHT(config, this);
   }

   /**
    * This is the intercepted get-Method, the entry point of every request. 
    */
   @Override
   public InformationObject get(Identifier identifier) {
      LOG.info("(MDHT ) Getting IO with Identifier " + identifier);
      
      return get(identifier, 0);
   }

   /**
    * This is the intercepted getAllVersions-Method.
    */
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

   /**
    * Default put: put an IO on every level.
    */
   @Override
   public void put(InformationObject io) {
      LOG.log(DemoLevel.DEMO, "(MDHT ) Putting IO with Identifier: " + io.getIdentifier() + " on all levels");
      InformationObject informationObject = translator.toImpl(io);

      try {
         validateIOForPut(informationObject);
      } catch (IllegalArgumentException ex) {
         throw new NetInfResolutionException("Trying to put invalid Information Object", ex);
      }

      // take level into account, if available
      int upToThisLevel = getLevel(io);

      int nLevels = dhts.size() - 1;
      
      for (int level = 0; level < upToThisLevel; level++) {
         dhts.get(level).put(informationObject, level, nLevels, localNodeIp.getAddress());
         LOG.log(DemoLevel.DEMO, "(MDHT ) Put IO at level " + level);
      }
   }

   /**
    * Provides the level value of the mdht_level attribute of the IO.
    * 
    * @param io
    *           The InformationObject.
    * @return The level as integer if given, otherwise the maximum level.
    */
   private int getLevel(InformationObject io) {
      int retValue = dhts.size();
      List<Attribute> attributes = io.getAttribute(DefinedAttributeIdentification.MDHT_LEVEL.getURI());
      if (attributes.isEmpty()) { // Use default size if the above-mentioned attribute is not defined
         return retValue;
      }
      for (Attribute attr : attributes) {
         Integer intValue = attr.getValue(Integer.class);
         if (intValue != null) {
            retValue = intValue > retValue ? retValue : intValue; // Check to see if the given value more than maximum allowed
            break;
         }
      }
      return retValue;
   }

   /**
    * A variant of the put-Method, where the IO is only registered up to a specified level. Level numbering
    * starts at 0.
    * @param io The Information Object to store.
    * @param maxLevel The level number up to which the IO should be stored.
    */
   public void put(InformationObject io, int maxLevel) {
      LOG.log(DemoLevel.DEMO, "(MDHT ) Putting IO with Identifier: " + io.getIdentifier() + " up to level " + maxLevel);
      int levels = maxLevel;
      
      // Limit the possible puts in case of a wrong/invalid parameter
      if (levels > dhts.size()) {
         levels = dhts.size();
      }
      
      for (int level = 0; level < levels; level++) {
         dhts.get(level).put(io, level, levels - 1, localNodeIp.getAddress());
         LOG.log(DemoLevel.DEMO, "(MDHT ) Put IO at level " + level);
      }
   }

   
   @Override
   /**
    * The delete method is not implemented/not relevant. Guaranteed deletes from a DHT may be impossible
    * to achieve. Should be, however, handled in future implementations. 
    * @see /netinf.node/src/netinf/node/resolution/pastry/past/PastDeleteImpl.java For the implementation
    * of the last Project Group.
    */
   public void delete(Identifier identifier) {
   }

   /**
    * A textual description of the service itself.
    */
   @Override
   public String describe() {
      return "MDHT Resolution System";
   }

   /**
    * Create the IdentityObject for this resolution service describing itself and its location.
    */
   @Override
   protected ResolutionServiceIdentityObject createIdentityObject() {
      ResolutionServiceIdentityObject identity = datamodelFactory.createDatamodelObject(ResolutionServiceIdentityObject.class);
      identity.setName("MDHT Resolution Service");
      identity.setDefaultPriority(70);
      identity.setDescription("This is the MDHT resolution service running on " + localNodeIp.getHostName());
      return identity;
   }

   /***
    * get method to search for a specific IO by using a commonapi ID. This method is meant to be called from within a child DHT
    * and not from within the MDHT itself
    * 
    * @param id
    *           The NetInf identifier object to search for
    * @param level
    *           The Ring/Level where the query should start
    * @return The IO or {@code null} if not found in either ring
    */
   public InformationObject get(Id id, int level) {

      InformationObject result = null;
      if (level >= dhts.size()) {
         return result;
      }
      DHT crtLevel = dhts.get(level);

      if (crtLevel != null) {
         result = crtLevel.get(id, level);
      }
      return result;
   }

   /***
    * get method to search for a specific IO by using a corresponding NetInf Identifier. The method is only to be used internally
    * in the MDHT
    * 
    * @param id
    *           The NetInf identifier object to search for
    * @param level
    *           The Ring/Level where the query should start
    * @return The IO or {@code null} if not found in either ring
    */
   private InformationObject get(Identifier id, int level) {

      InformationObject result = null;
      if (level >= dhts.size()) {
         return result;
      }
      DHT crtLevel = dhts.get(level);

      if (crtLevel != null) {
         result = crtLevel.get(id, level);
      }
      return result;
   }

   /**
    * This method has mostly debugging purposes, it just displays a message when the current MDHT
    * node receives a signal from the underlying DHT that it needs to search a level upwards.
    * @param nextLevel The next level. Remember that level numbering starts at 0.
    */
   public void switchRingUpwards(int nextLevel) {
      LOG.info("(MDHT) Switching ring from " + (nextLevel - 1) + " to " + nextLevel);
   }

   public void sendRemoteAck(final InetAddress targetNodeAddr) {
      try {
         int serverPort = 5000;
         Socket socket = new Socket();

         // Timeout is 1 second
         socket.bind(null);
         socket.connect(new InetSocketAddress(targetNodeAddr, serverPort), 1000);
         Connection conn = new TCPConnection(socket);

         // Build NetInf Message
         RSMDHTAck mdhtAckMsg = new RSMDHTAck();

         mdhtAckMsg.setPrivateKey(IDENTIFIER);
         // Send message
         LOG.info("(MDHT) Sending ACK to sender");
         conn.send(new AtomicMessage(MessageEncoderXML.ENCODER_ID, localXmlEncoder.encodeMessage(mdhtAckMsg)));

      } catch (IOException e) {
         LOG.error("(MDHT) Could not open remote node Socket to " + e.getMessage());
      }
   }

}
