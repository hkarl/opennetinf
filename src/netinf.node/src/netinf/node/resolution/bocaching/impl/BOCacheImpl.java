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
package netinf.node.resolution.bocaching.impl;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.security.Hashing;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;
import netinf.node.resolution.bocaching.BOCache;
import netinf.node.transferdispatcher.TransferDispatcher;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * The Class BOCacheImpl.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class BOCacheImpl implements BOCache {

   private static final Logger LOG = Logger.getLogger(BOCacheImpl.class);

   private final HTTPFileServer server;
   private final Set<String> cached;
   private TransferDispatcher transferDispatcher;

   @Inject
   public BOCacheImpl(HTTPFileServer server) throws NetInfCheckedException {
      super();
      this.server = server;
      this.server.start();
      cached = new HashSet<String>();
      rebuildCache();

      transferDispatcher = TransferDispatcher.getInstance();
   }

   @Override
   public boolean cache(DataObject dataObject) {
      String hash = DatamodelUtils.getHash(dataObject);
      String directory = server.getDirectory(); // .replace('\\', '/')

      if (!directory.endsWith(File.separator)) {
         directory += File.separator;
      }
      if (hash == null) {
         LOG.info("DataObject has no Hash and will not be cached");
         return false;
      }

      if (!contains(dataObject)) {
         LOG.log(DemoLevel.DEMO, "(BOCache ) Cache file...");
         List<Attribute> locators = dataObject.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
         for (Attribute attr : locators) {
            DataInputStream fis = null;
            String url = attr.getValue(String.class);
            try {
               String destination = directory + hash + ".tmp";
               transferDispatcher.getStreamAndSave(url, destination, true);

               // start reading
               fis = new DataInputStream(new FileInputStream(destination));

               // skip manually added content-type
               int skipSize = fis.readInt();
               for (int i = 0; i < skipSize; i++) {
                  fis.read();
               }

               byte[] hashBytes = Hashing.hashSHA1(fis);
               IOUtils.closeQuietly(fis);
               if (hash.equalsIgnoreCase(Utils.hexStringFromBytes(hashBytes))) {
                  LOG.info("Hash of downloaded file is valid: " + url);
                  LOG.log(DemoLevel.DEMO, "(NODE ) Hash of downloaded file is valid. Will be cached.");
                  File old = new File(destination);
                  File newFile = new File(destination.substring(0, destination.lastIndexOf('.')));
                  old.renameTo(newFile);
                  addLocator(dataObject);
                  cached.add(newFile.getName());

                  return true;
               } else {
                  LOG.log(DemoLevel.DEMO, "(NODE ) Hash of downloaded file is invalid. Trying next locator");
                  LOG.warn("Hash of downloaded file is not valid: " + url);
                  LOG.warn("Trying next locator");
               }

            } catch (FileNotFoundException ex) {
               LOG.warn("Error downloading:" + url);
            } catch (IOException e) {
               LOG.warn("Error hashing:" + url);
            } catch (Exception e) {
               LOG.warn("Error hashing, but file was OK: " + url);
               // e.printStackTrace();
            } finally {
               IOUtils.closeQuietly(fis);
               rebuildCache();
            }
         }
         LOG.warn("Could not find reliable source to cache: " + dataObject);
         return false;
      } else {
         LOG.log(DemoLevel.DEMO, "(NODE ) DataObject has already been cached. Adding locator.");
         addLocator(dataObject);
         return true;
      }
   }

   @Override
   public boolean contains(DataObject dataObject) {
      String hash = DatamodelUtils.getHash(dataObject);
      LOG.debug("Hash of dataobject is " + hash);
      if (hash == null) {
         return false;
      } else {
         return cached.contains(hash);
      }
   }

   private void rebuildCache() {
      File serverDir = new File(server.getDirectory());
      if (serverDir.isDirectory()) {
         File[] files = serverDir.listFiles();
         for (File f : files) {
            if (f.isFile()) {
               if (f.getName().endsWith(".tmp")) {
                  f.delete();
               } else {
                  LOG.debug("Adding '" + f.getName() + "' to cache");
                  cached.add(f.getName());
               }
            }
         }
      }

   }

   private void addLocator(DataObject dataObject) {
      String hash = DatamodelUtils.getHash(dataObject);
      Attribute attribute = dataObject.getDatamodelFactory().createAttribute();
      attribute.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
      attribute.setIdentification(DefinedAttributeIdentification.HTTP_URL.getURI());
      attribute.setValue(server.getUrlForHash(hash));
      Attribute cacheMarker = dataObject.getDatamodelFactory().createAttribute();
      cacheMarker.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      cacheMarker.setIdentification(DefinedAttributeIdentification.CACHE.getURI());
      cacheMarker.setValue("true");
      attribute.addSubattribute(cacheMarker);
      // Do not add the same locator twice
      if (!dataObject.getAttributes().contains(attribute)) {
         dataObject.addAttribute(attribute);
      }
   }

}
