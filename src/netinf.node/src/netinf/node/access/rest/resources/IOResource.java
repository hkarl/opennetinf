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
package netinf.node.access.rest.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import netinf.common.datamodel.DeleteMode;
import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfCheckedException;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

/**
 * Resource to display an IO by requesting a NetInf identifier.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class IOResource extends NetInfResource {

   private static final Logger LOG = Logger.getLogger(IOResource.class);

   private String hashOfPK;
   private String hashOfPKIdent;
   private String uniqueLabel;
   private String versionKind;
   private String versionNumber;

   /**
    * Initializes the context of a IOResource.
    */
   @Override
   protected void doInit() {
      super.doInit();
      hashOfPK = getQuery().getFirstValue("HASH_OF_PK", true);
      hashOfPKIdent = getQuery().getFirstValue("HASH_OF_PK_IDENT", true);
      uniqueLabel = getQuery().getFirstValue("UNIQUE_LABEL", true);
      versionKind = getQuery().getFirstValue("VERSION_KIND", true);
      versionNumber = getQuery().getFirstValue("VERSION_NUMBER", true);
   }

   /**
    * Handler for GET-requests.
    * 
    * @return Simple String representation of an IO
    */
   @Get
   public Representation showIO() {
      InformationObject io = null;
      try {
         io = getNodeConnection().getIO(createIdentifier(hashOfPK, hashOfPKIdent, versionKind, uniqueLabel, versionNumber));
      } catch (NetInfCheckedException e) {
         LOG.warn("Could not get IO with given identifier", e);
      }

      if (io != null) {
         // convert to RDF
         io = getDatamodelTranslator().toRdf(io);
         byte[] ioByteArray = io.serializeToBytes();
         InputStream is = new ByteArrayInputStream(ioByteArray);
         InputRepresentation iRep = new InputRepresentation(is, MediaType.APPLICATION_XML);
         return iRep;
      } else {
         throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
      }
   }

   /**
    * Handler for DELETE-requests.
    */
   @Delete
   public void deleteIO() {
      InformationObject io = null;
      try {
         io = getNodeConnection().getIO(createIdentifier(hashOfPK, hashOfPKIdent, versionKind, uniqueLabel, versionNumber));
      } catch (NetInfCheckedException e) {
         LOG.warn("Could not get IO with given identifier", e);
      }

      if (io != null) {
         try {
            getNodeConnection().deleteIO(io, DeleteMode.DELETE_DATA);
            setStatus(Status.SUCCESS_NO_CONTENT);
            return;
         } catch (NetInfCheckedException e) {
            e.printStackTrace();
         }
      }
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
   }

   /**
    * Handler for PUT-requests.
    * 
    * @param content
    *           IO as String
    */
   @Put
   public void putIO(String content) {
      byte[] serializedIO = content.getBytes();
      InformationObject io = getDatamodelFactory().createInformationObjectFromBytes(serializedIO);
      if (!io.getIdentifier().equals(createIdentifier(hashOfPK, hashOfPKIdent, versionKind, uniqueLabel, versionNumber))) {
         throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
      }
      try {
         getNodeConnection().putIO(io);
         setStatus(Status.SUCCESS_NO_CONTENT);
      } catch (NetInfCheckedException e) {
         LOG.warn("Could not put IO", e);
         throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
      }
   }

   /**
    * Handler for POST-requests.
    * 
    * @param content
    *           IO as String
    */
   @Post
   public void postIO(String content) {
      byte[] serializedIO = content.getBytes();
      InformationObject io = getDatamodelFactory().createInformationObjectFromBytes(serializedIO);
      try {
         getNodeConnection().putIO(io);
         setStatus(Status.SUCCESS_CREATED);
         setLocationRef("/io/" + io.getIdentifier().toString());
      } catch (NetInfCheckedException e) {
         LOG.warn("Could not put IO", e);
         throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
      }
   }

}
