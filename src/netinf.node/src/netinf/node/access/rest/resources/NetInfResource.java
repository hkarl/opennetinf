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

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.translation.DatamodelTranslator;
import netinf.node.access.rest.RESTApplication;

import org.restlet.resource.ServerResource;

/**
 * Abstract resource that provides a NetInfNodeConnection and a DatamodelFactory.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public abstract class NetInfResource extends ServerResource {

   /**
    * Yields a connection to a NetInfNode.
    * 
    * @return A NetInfNodeConnection of the parent application
    */
   protected NetInfNodeConnection getNodeConnection() {
      return ((RESTApplication) getApplication()).getNodeConnection();
   }

   /**
    * Yields an implementation of a DatamodelFactory.
    * 
    * @return A concrete DatamodelFactory
    */
   protected DatamodelFactory getDatamodelFactory() {
      return ((RESTApplication) getApplication()).getDatamodelFactory();
   }

   /**
    * Yields an implementation of a DatamodelTranslator.
    * 
    * @return A DatamodelTranslator
    */
   protected DatamodelTranslator getDatamodelTranslator() {
      return ((RESTApplication) getApplication()).getDatamodelTranslator();
   }

   /**
    * Creates a NetInf Identifier given a number of plain Strings.
    * 
    * @param hashOfPK
    *           Hash of Public Key
    * @param hashOfPKIdent
    *           Hash Algorithm
    * @param versionKind
    *           Version Kind
    * @param uniqueLabel
    *           Unique Label
    * @param versionNumber
    *           Version Number
    * @return NetInf Identifier
    */
   protected Identifier createIdentifier(String hashOfPK, String hashOfPKIdent, String versionKind, String uniqueLabel,
         String versionNumber) {
      Identifier identifier = getDatamodelFactory().createIdentifier();
      // HASH_OF_PK
      IdentifierLabel hashLabel = getDatamodelFactory().createIdentifierLabel();
      hashLabel.setLabelName(DefinedLabelName.HASH_OF_PK.toString());
      hashLabel.setLabelValue(hashOfPK);
      identifier.addIdentifierLabel(hashLabel);
      // HASH_OF_PK_IDENT
      IdentifierLabel hashIdentLabel = getDatamodelFactory().createIdentifierLabel();
      hashIdentLabel.setLabelName(DefinedLabelName.HASH_OF_PK_IDENT.toString());
      hashIdentLabel.setLabelValue(hashOfPKIdent.toUpperCase());
      identifier.addIdentifierLabel(hashIdentLabel);
      // VERSION_KIND
      IdentifierLabel kindLabel = getDatamodelFactory().createIdentifierLabel();
      kindLabel.setLabelName(DefinedLabelName.VERSION_KIND.toString());
      kindLabel.setLabelValue(versionKind.toUpperCase());
      identifier.addIdentifierLabel(kindLabel);
      // UNIQUE_LABEL
      if (uniqueLabel != null) {
         IdentifierLabel labelLabel = getDatamodelFactory().createIdentifierLabel();
         labelLabel.setLabelName(DefinedLabelName.UNIQUE_LABEL.toString());
         labelLabel.setLabelValue(uniqueLabel);
         identifier.addIdentifierLabel(labelLabel);
      }
      // VERSION_NUMBER
      if (versionKind.equals("VERSIONED")) {
         IdentifierLabel versionLabel = getDatamodelFactory().createIdentifierLabel();
         versionLabel.setLabelName(DefinedLabelName.VERSION_NUMBER.toString());
         versionLabel.setLabelValue(versionNumber);
         identifier.addIdentifierLabel(versionLabel);
      }
      return identifier;
   }

}
