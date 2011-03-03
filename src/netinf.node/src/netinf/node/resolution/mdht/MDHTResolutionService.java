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
package netinf.node.resolution.mdht;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.List;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.datamodel.translation.DatamodelTranslator;
import netinf.node.resolution.AbstractResolutionService;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * @author PG NetInf 3, University of Paderborn
 */
public class MDHTResolutionService extends AbstractResolutionService {

   private static final int RESOLUTION_SERVICE_PRIORITY = 70;
   private static final String RESOLUTION_SERVICE_NAME = "MDHT Resolution Service";

   private static final Logger LOG = Logger.getLogger(MDHTResolutionService.class);
   private DatamodelFactory datamodelFactory;
   private DatamodelTranslator translator;

   private static final String STORAGE_FOLDER = "../configs/storage/";

   // table of mdht levels
   private Hashtable<Integer, DHT> levels = new Hashtable<Integer, DHT>();

   // runtime-dummyStorage for IOs
   private Hashtable<String, InformationObject> storage = new Hashtable<String, InformationObject>();

   @Inject
   public void setDatamodelFactory(DatamodelFactory factory) {
      datamodelFactory = factory;
   }

   @Inject
   public void setDataModelTranslator(DatamodelTranslator translator) {
      this.translator = translator;
   }

   // temp properties: (later to configs/properties)
   private int numberOfLevels = 3;
   private String joinNode = "10.10.10.2"; // A joins nobody, otherwise address of the node to join
   private int joinAtLevel = 0; // 0 -> join nobody
   private String myHost = "10.10.10.1";
   private int basePort = 2000;

   /**
    * Constructor
    */
   public MDHTResolutionService() {
      super();
      LOG.info("Creating resolution service");

      // create necessary levels
      int levelsToCreate = joinAtLevel - 1; // how many levels to create
      for (int i = 1; i <= levelsToCreate; i++) {
         LOG.info("Create DHT ring at level" + i);
         levels.put(i, this.joinRing(myHost, basePort, i));
      }

      // joining
      if (joinAtLevel > 0) {
         while (joinAtLevel <= numberOfLevels) {
            LOG.info("Join node " + joinNode + "on level " + joinAtLevel);
            levels.put(joinAtLevel, this.joinRing(joinNode, basePort, joinAtLevel));
            joinAtLevel++;
         }
      }
   }

   private DHT joinRing(String host, int port, int atLevel) {
      DHT dht = this.createDHT();
      // 2000 + 1 = 2001 -> port of first level
      InetSocketAddress bootAddress = new InetSocketAddress(host, port + atLevel);
      dht.joinRing(bootAddress);

      return dht;
   }

   /**
    * wrapper for creating a DHT ring
    * 
    * @return DHT
    */
   private DHT createDHT() {
      return new FreePastryDHT();
   }

   /*
    * (non-Javadoc)
    * @see netinf.node.resolution.ResolutionService#get(netinf.common.datamodel.Identifier)
    */
   @Override
   public InformationObject get(Identifier identifier) {

      return storage.get(identifier.toString());

      // // From Storage (eddy)
      // InformationObject io = null;
      // try {
      // io = this.getIOFromStorage(identifier);
      // } catch (IOException e) {
      // // TODO Auto-generated catch block
      // e.printStackTrace();
      // }
      // return io;
   }

   /*
    * (non-Javadoc)
    * @see netinf.node.resolution.ResolutionService#getAllVersions(netinf.common.datamodel.Identifier)
    */
   @Override
   public List<Identifier> getAllVersions(Identifier identifier) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * Default put: put on every level
    */
   @Override
   public void put(InformationObject io) {
      for (int i = 1; i <= numberOfLevels; i++) {
         DHT ring = levels.get(i);
         String ip = ring.getResponsibleNode(io.getIdentifier());
         // TODO store io on node of this IP, RPC?
      }

      // store on every level
      this.storeIO(io);
   }

   private void put(InformationObject io, int fromLevel, int toLevel) {
      // DHT ring = levels.get(fromLevel);
      // String ipOfNode = ring.getResponsibleNode(io.getIdentifier());
      //
      // if (fromLevel + 1 <= toLevel) {
      //
      // }
   }

   /**
    * gets the IO from the storage by a given Identifier
    * 
    * @param id
    *           Identifier of InformaionObject
    * @return the corresponding IO or null
    * @throws IOException
    */
   private InformationObject getIOFromStorage(Identifier id) throws IOException {
      // TODO create folder "storage" if does not exist
      InformationObject result = null;
      File dir = new File(STORAGE_FOLDER);

      if (dir.isDirectory()) {
         String[] filenames = dir.list();
         for (String filename : filenames) {
            if (filename == getFilename(id)) {
               byte[] fileContent = getBytesFromFile(STORAGE_FOLDER + filename);
               result = datamodelFactory.createInformationObjectFromBytes(fileContent);
            }
         }
      }

      return result;
   }

   /**
    * Stores a given InformationObject
    * 
    * @param io
    *           the InformationObject that has to be stored
    */
   private void storeIO(InformationObject io) {
      storage.put(io.getIdentifier().toString(), io);

      // try {
      // FileWriter fstream = new FileWriter(STORAGE_FOLDER + getFilename(io));
      // BufferedWriter out = new BufferedWriter(fstream);
      // out.write(io.serializeToBytes().toString());
      // out.close();
      // } catch (Exception e) {
      // LOG.error("File could not be stored: " + e.getMessage());
      // }
   }

   /**
    * Gets the filename corresponding to an IO
    * 
    * @param io
    *           the given IO
    * @return Filename of the given IO
    */
   private String getFilename(InformationObject io) {
      return io.getIdentifier().toString() + ".txt";
   }

   private String getFilename(Identifier id) {
      return id.toString() + ".txt";
   }

   private byte[] getBytesFromFile(String filename) throws IOException {
      File file = new File(filename);
      InputStream is = new FileInputStream(file);
      long length = file.length();

      if (length > Integer.MAX_VALUE) {
         // File is too large
      }

      // Create the byte array to hold the data
      byte[] bytes = new byte[(int) length];

      // Read in the bytes
      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
         offset += numRead;
      }

      // Ensure all the bytes have been read in
      if (offset < bytes.length) {
         throw new IOException("Could not completely read file " + file.getName());
      }

      // Close the input stream and return bytes
      is.close();
      return bytes;
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
    * (non-Javadoc)
    * @see netinf.node.resolution.ResolutionService#describe()
    */
   @Override
   public String describe() {
      return "a multi-level distributed hashtable system";
   }

   /*
    * (non-Javadoc)
    * @see netinf.node.resolution.AbstractResolutionService#createIdentityObject()
    */
   @Override
   protected ResolutionServiceIdentityObject createIdentityObject() {
      ResolutionServiceIdentityObject identity = datamodelFactory.createDatamodelObject(ResolutionServiceIdentityObject.class);
      identity.setName(RESOLUTION_SERVICE_NAME);
      identity.setDefaultPriority(70);
      identity.setDescription("This is a mdht resolution service running on "); // TODO ? ...on what?
      return identity;
   }

}
