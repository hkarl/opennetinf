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
package netinf.node.resolution.pastry;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.datamodel.translation.DatamodelTranslator;
import netinf.common.exceptions.NetInfResolutionException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.node.resolution.AbstractResolutionService;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.pastry.past.IOPastContent;
import netinf.node.resolution.pastry.past.PastDeleteImpl;
import netinf.node.resolution.pastry.past.VersionPastContent;

import org.apache.log4j.Logger;

import rice.Continuation.ExternalContinuation;
import rice.p2p.commonapi.IdFactory;
import rice.p2p.past.Past;
import rice.p2p.past.PastContent;
import rice.pastry.PastryNode;

import com.google.inject.Inject;

/**
 * The Class PastryResolutionService.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class PastryResolutionService extends AbstractResolutionService implements ResolutionService {

   private static final int RESOLUTION_SERVICE_PRIORITY = 70;
   private static final String RESOLUTION_SERVICE_NAME = "Pastry distributed Resolution Service";
   private static final Logger LOG = Logger.getLogger(PastryResolutionService.class);
   private PastryNode pastryNode;
   private Past past;
   private DatamodelFactory datamodelFactory;
   private DatamodelTranslator translator;

   @Inject
   public void setDatamodelFactory(DatamodelFactory factory) {
      datamodelFactory = factory;
   }

   @Inject
   public void setDataModelTranslator(DatamodelTranslator translator) {
      this.translator = translator;
   }

   @Inject
   public PastryResolutionService(PastNodePair pair, IdFactory idFactory) {
      super();
      setIdFactory(idFactory);
      LOG.info("Creating resolution service");
      pastryNode = pair.getNode();
      past = pair.getPast();
      bootPastryNode();
   }

   private void bootPastryNode() {
      LOG.debug("Starting Pastry node of resolution Service");
      InetSocketAddress bootstrapAddress = getBootstrapAddress();
      pastryNode.boot(bootstrapAddress);
      waitForStartupOfNode();
      LOG.info("Finished starting pastry node" + pastryNode);
   }

   private InetSocketAddress getBootstrapAddress() {
      InetSocketAddress bootstrapAddress = null;
      try {
         bootstrapAddress = pastryNode.getEnvironment().getParameters().getInetSocketAddress("pastry.bootupaddress");
      } catch (UnknownHostException e) {
         LOG.warn("Could not resolve bootup address. Will initiate new Ring ", e);
      }
      return bootstrapAddress;
   }

   private void waitForStartupOfNode() {
      synchronized (pastryNode) {
         while (!(pastryNode.isReady() || pastryNode.joinFailed())) {
            try {
               pastryNode.wait(500);
            } catch (InterruptedException e) {
               throw new NetInfUncheckedException("Pastry thread is interrupted", e);
            }
            if (pastryNode.joinFailed()) {
               throw new NetInfUncheckedException("Could not join the Pastry ring.  Reason:" + pastryNode.joinFailedReason());
            }
         }
      }
   }

   @Override
   public void delete(Identifier identifier) {
      Identifier transIdentifier = translator.toImpl(identifier);
      if (transIdentifier.isVersioned()) {
         throw new NetInfResolutionException("Trying to delete versioned io");
      }

      // TODO Check if we really need to get the IO before we delete it. Maybe it would be better to take an IO as parameter.
      InformationObject ioToDelete = get(transIdentifier);

      if (ioToDelete == null) {
         // Nothing more to do if the io does not exist
         return;
      }

      ExternalContinuation<Object, Exception> command = new ExternalContinuation<Object, Exception>();

      ((PastDeleteImpl) past).delete(buildId(transIdentifier), command);

      command.sleep();

      if (command.getException() != null) {
         throw new NetInfResolutionException("Could not delete Identifier ", command.getException());
      }
      if (command.getResult() != null && !((Boolean[]) command.getResult())[0]) {
         throw new NetInfResolutionException("Could not delete Identifier ");
      }

      publishDelete(ioToDelete);
   }

   @Override
   public InformationObject get(Identifier identifier) {
      identifier = translator.toImpl(identifier);
      Identifier idToLookup = getIdToLookup(identifier);
      ExternalContinuation<PastContent, Exception> command = new ExternalContinuation<PastContent, Exception>();
      past.lookup(buildId(idToLookup), command);
      command.sleep();

      if (command.exceptionThrown()) {
         throw new NetInfResolutionException("Could not get Information object", command.getException());
      }
      if (command.getResult() == null) {
         return null;
      } else if (command.getResult() instanceof IOPastContent) {
         IOPastContent content = (IOPastContent) command.getResult();
         return content.getInformationObject();
      } else {
         throw new NetInfUncheckedException("Received no Object of type Information Object");
      }
   }

   @Override
   public List<Identifier> getAllVersions(Identifier identifier) {
      identifier = translator.toImpl(identifier);
      if (!identifier.isVersioned()) {
         throw new NetInfResolutionException("Trying to get versions for unversioned identifier");
      }
      ExternalContinuation<PastContent, Exception> command = new ExternalContinuation<PastContent, Exception>();
      Identifier idWithoutVersion = createIdentifierWithoutVersion(identifier);
      past.lookup(buildId(idWithoutVersion), command);
      command.sleep();
      if (command.exceptionThrown()) {
         throw new NetInfResolutionException("Could not get Information object", command.getException());
      }
      if (command.getResult() == null) {
         return null;
      } else if (command.getResult() instanceof VersionPastContent) {
         VersionPastContent content = (VersionPastContent) command.getResult();
         return content.getIdentifiers();
      } else {
         throw new NetInfResolutionException("Received no Object of type Information Object");
      }
   }

   @Override
   public void put(InformationObject informationObject) {
      informationObject = translator.toImpl(informationObject);
      try {
         validateIOForPut(informationObject);
      } catch (IllegalArgumentException ex) {
         throw new NetInfResolutionException("Trying to put unvalid information object", ex);
      }

      // check if this is a creating or modifying put
      InformationObject oldIo = get(informationObject.getIdentifier());
      boolean modifyingPut;
      if (oldIo == null) {
         modifyingPut = false;
      } else {
         modifyingPut = true;
      }

      putInformationObject(informationObject);
      if (informationObject.getIdentifier().isVersioned()) {
         putVersionInformation(informationObject);
      }

      if (modifyingPut) {
         publishPut(oldIo, informationObject);
      } else {
         publishPut(null, informationObject);
      }
   }

   private void putVersionInformation(InformationObject informationObject) {
      ExternalContinuation<Boolean[], Exception> versionInsertCallback = new ExternalContinuation<Boolean[], Exception>();
      Identifier unversionedIdentifier = createIdentifierWithoutVersion(informationObject.getIdentifier());
      VersionPastContent vpc = new VersionPastContent(informationObject.getIdentifier(), buildId(unversionedIdentifier));
      past.insert(vpc, versionInsertCallback);
      versionInsertCallback.sleep();
      if (versionInsertCallback.getException() != null) {
         throw new NetInfResolutionException("Could not insert version information", versionInsertCallback.getException());
      }
   }

   private void putInformationObject(InformationObject informationObject) {
      IOPastContent content = new IOPastContent(informationObject, buildId(informationObject));
      ExternalContinuation<Boolean[], Exception> insertCallback = new ExternalContinuation<Boolean[], Exception>();
      past.insert(content, insertCallback);
      insertCallback.sleep();
      if (insertCallback.exceptionThrown()) {
         throw new NetInfResolutionException("Could not insert Information Object", insertCallback.getException());
      }
   }

   @Override
   protected void finalize() throws Throwable {
      LOG.info("PastryResoltionService is destroyed");
      if (pastryNode != null) {
         pastryNode.destroy();
      }
      super.finalize();
   }

   public PastryNode getPastryNode() {
      return pastryNode;
   }

   public void setPastryNode(PastryNode pastryNode) {
      this.pastryNode = pastryNode;
   }

   public Past getPast() {
      return past;
   }

   public void setPast(Past past) {
      this.past = past;
   }

   @Override
   protected ResolutionServiceIdentityObject createIdentityObject() {
      ResolutionServiceIdentityObject identity = datamodelFactory.createDatamodelObject(ResolutionServiceIdentityObject.class);
      identity.setName(RESOLUTION_SERVICE_NAME);
      identity.setDefaultPriority(70);
      identity.setDescription("This is a pastry resolution service running on " + getBootstrapAddress());
      return identity;
   }

   @Override
   public String describe() {
      return "a distributed pastry system";
   }

}
