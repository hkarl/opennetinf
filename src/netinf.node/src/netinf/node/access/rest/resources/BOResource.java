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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.utils.DatamodelUtils;
import netinf.node.transferdispatcher.TransferDispatcher;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * Resource to retrieve a BO by requesting a NetInf identifier.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class BOResource extends NetInfResource {

   private static final Logger LOG = Logger.getLogger(BOResource.class);

   private String hashOfPK;
   private String hashOfPKIdent;
   private String versionKind;
   private String uniqueLabel;
   private String versionNumber;

   /**
    * Initializes the context of a BOResource.
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
    * Handler for a GET-requests.
    * 
    * @return InputRepresentation if DO was found
    */
   @Get
   public Representation retrieveBO() {
      Identifier identifier = createIdentifier(hashOfPK, hashOfPKIdent, versionKind, uniqueLabel, versionNumber);

      InformationObject io = null;
      try {
         io = getNodeConnection().getIO(identifier);
      } catch (NetInfCheckedException e) {
         LOG.warn("Could not get IO", e);
      }

      if (io != null) {
         List<Attribute> locators = io.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
         if (!locators.isEmpty()) {
            try {
               if (io instanceof DataObject) {
                  TransferDispatcher tsDispatcher = TransferDispatcher.getInstance();

                  final InputStream inStream = tsDispatcher.getStream((DataObject) io);
                  MediaType mdType = new MediaType(DatamodelUtils.getContentType(io));

                  // return new InputRepresentation(inStream, mdType);
                  return new OutputRepresentation(mdType) {
                     @Override
                     public void write(OutputStream outStream) throws IOException {
                        try {
                           IOUtils.copy(inStream, outStream);
                        } finally {
                           IOUtils.closeQuietly(inStream);
                           IOUtils.closeQuietly(outStream);
                        }

                     }
                  };
               }
            } catch (IOException ioe) {
               LOG.warn("Could not open URL connection", ioe);
               // continue;
            } catch (Exception e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
      }
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
   }
}
