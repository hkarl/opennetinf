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
package netinf.common.security.impl;

import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.AUTHORIZED_WRITERS;
import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.IDENTITY_REVOKED;
import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.ORIGIN;
import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.OWNER;
import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.PUBLIC_KEY;
import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.SINGLE_WRITER_REQUIRED;
import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.THIRD_PARTY_SIGNATURE;
import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.WRITER;
import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.WRITER_OF_GROUP;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Iterator;
import java.util.List;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.security.IdentityVerification;
import netinf.common.security.IdentityVerificationResult;
import netinf.common.security.SignatureAlgorithm;
import netinf.common.security.identity.IdentityManager;
import netinf.common.security.impl.module.SecurityModule.Security;
import netinf.common.utils.Utils;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * The Class IdentityVerificationImpl. {@link IdentityVerificationImpl} implements the {@link IdentityVerification} interface.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IdentityVerificationImpl implements IdentityVerification {

   private static final Logger LOG = Logger.getLogger(IntegrityImpl.class);

   private final NetInfNodeConnection nodeConnection;
   private final DatamodelFactory dmFactory;
   private final SignatureAlgorithm signatureAlgorithm;

   @Inject
   public IdentityVerificationImpl(DatamodelFactory dmfactory, @Security NetInfNodeConnection nodeConnection,
         IdentityManager identityManager, SignatureAlgorithm sigAlg) {
      this.nodeConnection = nodeConnection;
      // this.nodeConnection.setSerializeFormat(SerializeFormat.JAVA);
      this.dmFactory = dmfactory;
      this.signatureAlgorithm = sigAlg;

      LOG.debug("Identity Verification Module created.");
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.IdentityVerification#isIdentityTrusted(netinf.common.datamodel.identity.IdentityObject)
    */
   @SuppressWarnings("unchecked")
   @Override
   public IdentityVerificationResult isIdentityTrusted(IdentityObject ido) throws NetInfCheckedException {
      // Postponed. Not tested and developed further since priority changed.
      // TODO: get the right id of the user who wants to check if he trusts an identity
      final String idOfUser = "12345";

      // check whether there is an IdO at all
      if (ido == null) {
         LOG.error("No Identity Object given.");
         throw new NetInfUncheckedException("No IdentityObject given.");
      }

      // check for revocation
      Attribute revoked = ido.getSingleAttribute(IDENTITY_REVOKED.getURI());
      if (revoked != null) {
         LOG.warn("Identity is revoked");
         return IdentityVerificationResult.IDENTITY_VERIFICATION_FAILED;
      }

      // check whether identity is trusted at all
      Attribute thirdParty = ido.getSingleAttribute(THIRD_PARTY_SIGNATURE.getURI());
      if (thirdParty == null) {
         LOG.warn("Identity is not trusted at all.");
         return IdentityVerificationResult.IDENTITY_VERIFICATION_FAILED;
      }

      List<IdentityObject> thirdparties = (List<IdentityObject>) thirdParty;
      for (Iterator it = thirdparties.iterator(); it.hasNext();) {
         if (((String) it.next()).equals(idOfUser)) {
            Attribute idrevoked = (Attribute) it.next();
            if (idrevoked == null) {
               return IdentityVerificationResult.IDENTITY_VERIFICATION_SUCCEEDED;
            }
         }
      }

      // recursive call
      for (Iterator it2 = thirdparties.iterator(); it2.hasNext();) {
         return isIdentityTrusted((IdentityObject) it2.next());
      }

      return IdentityVerificationResult.IDENTITY_VERIFICATION_FAILED;
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.IdentityVerification#isIOVerifiedByOwner(netinf.common.datamodel.InformationObject)
    */
   @Override
   public IdentityVerificationResult isIOVerifiedByOwner(InformationObject io) {
      // check whether there is an IO at all
      if (io == null) {
         LOG.error("No Information Object given.");
         throw new NetInfUncheckedException("No InformationObject given.");
      }

      // check whether owner is given (i.e., OWNER Property is present)
      String valueOwner = null;
      if (io instanceof IdentityObject) {
         valueOwner = io.getIdentifier().toString() + IntegrityImpl.PATH_SEPERATOR
         + DefinedAttributeIdentification.PUBLIC_KEY.getURI();
      } else {
         valueOwner = io.getSingleAttribute(OWNER.getURI()).getValue(String.class);
      }

      if (valueOwner == null) {
         String error = "Owner is missing.";
         io.addAttribute(identityVerificationFailedAttribute(error));
         LOG.warn(error);
         return IdentityVerificationResult.IDENTITY_NOT_VERIFIABLE;
      }

      // Check whether authorized writers list is signed by owner if it exists
      Attribute authorizedWriters = io.getSingleAttribute(AUTHORIZED_WRITERS.getURI());
      if (authorizedWriters != null) {
         Attribute signerOfWriters = authorizedWriters.getSingleSubattribute(WRITER.getURI());
         if (!signerOfWriters.getValue(String.class).equals(valueOwner)) {
            String error = "List of authorized writers is not signed by the owner.";
            io.addAttribute(identityVerificationFailedAttribute(error));
            LOG.warn(error);
            return IdentityVerificationResult.IDENTITY_VERIFICATION_FAILED;
         }
      }

      // Check whether origin is signed by owner if it exists
      Attribute origin = io.getSingleAttribute(ORIGIN.getURI());
      if (origin != null) {
         Attribute signerOfOrigin = origin.getSingleSubattribute(WRITER.getURI());
         if (!signerOfOrigin.getValue(String.class).equals(valueOwner)) {
            String error = "Origin is not signed by the owner.";
            io.addAttribute(identityVerificationFailedAttribute(error));
            LOG.warn(error);
            return IdentityVerificationResult.IDENTITY_VERIFICATION_FAILED;
         }
      }

      // Check whether flag that single writer is required is signed by owner if it exists
      Attribute singleWriterRequired = io.getSingleAttribute(SINGLE_WRITER_REQUIRED.getURI());
      if (singleWriterRequired != null) {
         Attribute signerOfSingleFlag = singleWriterRequired.getSingleSubattribute(WRITER.getURI());
         if (!signerOfSingleFlag.getValue(String.class).equals(valueOwner)) {
            String error = "Flag whether single writer is required is not signed by the owner.";
            io.addAttribute(identityVerificationFailedAttribute(error));
            LOG.warn(error);
            return IdentityVerificationResult.IDENTITY_VERIFICATION_FAILED;
         }
      }
      LOG.debug("Identity Verification Check succeeded. IO is verified by owner.");
      return IdentityVerificationResult.IDENTITY_VERIFICATION_SUCCEEDED;
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.IdentityVerification#isOwnerVerified(netinf.common.datamodel.InformationObject)
    */
   @Override
   public IdentityVerificationResult isOwnerVerified(InformationObject io) throws NetInfCheckedException,
   NoSuchAlgorithmException {

      // check whether there is an IO at all
      if (io == null) {
         LOG.error("No Information Object given.");
         throw new NetInfUncheckedException("No InformationObject given.");
      }

      // check whether public key is given (i.e., PUBLIC_KEY Property is present)
      Attribute publickey = io.getSingleAttribute(PUBLIC_KEY.getURI());
      if (publickey == null) {
         String error = "Public key is missing.";
         io.addAttribute(identityVerificationFailedAttribute(error));
         LOG.warn(error);
         return IdentityVerificationResult.IDENTITY_NOT_VERIFIABLE;
      }

      // get hash of public key in Identifier
      String pkInId = io.getIdentifier().getIdentifierLabel("HASH_OF_PK").getLabelValue();
      String pkInIdHashtype = io.getIdentifier().getIdentifierLabel("HASH_OF_PK_IDENT").getLabelValue();

      // hash and compare hashed public keys
      String pkHashed = "";
      try {
         pkHashed = this.signatureAlgorithm.hash(publickey.getValueRaw(), pkInIdHashtype);
      } catch (NoSuchAlgorithmException e) {
         LOG.error("Unable to hash public key. " + e.getMessage());
      }

      if (!pkHashed.equals(pkInId)) {
         String error = "Owner is not verified.";
         io.addAttribute(identityVerificationFailedAttribute(error));
         LOG.warn(error);
         return IdentityVerificationResult.IDENTITY_VERIFICATION_FAILED;
      }

      // check whether owner is given (i.e., OWNER Property is present) and verify owner authentication
      if (io instanceof IdentityObject) {
         LOG.debug("Identity Verification Check succeeded. Owner is verified.");
         return IdentityVerificationResult.IDENTITY_VERIFICATION_SUCCEEDED;
      } else {
         Attribute owner = io.getSingleAttribute(OWNER.getURI());
         if (owner == null) {
            String error = "Owner attribute is missing.";
            io.addAttribute(identityVerificationFailedAttribute(error));
            LOG.warn(error);
            return IdentityVerificationResult.IDENTITY_NOT_VERIFIABLE;
         }

         String ownerIdentifierString = owner.getValue(String.class);
         ownerIdentifierString = ownerIdentifierString.substring(0, ownerIdentifierString.indexOf(IntegrityImpl.PATH_SEPERATOR));

         Identifier identifier = this.dmFactory.createIdentifierFromString(ownerIdentifierString);

         InformationObject informationObject = this.nodeConnection.getIO(identifier);
         if (informationObject instanceof IdentityObject) {
            PublicKey masterkey = ((IdentityObject) informationObject).getPublicMasterKey();
            PublicKey pubkey = Utils.stringToPublicKey(publickey.getValue(String.class));
            if (pubkey.equals(masterkey)) {
               LOG.debug("Identity Verification Check succeeded. Owner is verified.");
               return IdentityVerificationResult.IDENTITY_VERIFICATION_SUCCEEDED;
            }
         }
      }
      String error = "Owner is not verified.";
      io.addAttribute(identityVerificationFailedAttribute(error));
      LOG.warn(error);
      return IdentityVerificationResult.IDENTITY_VERIFICATION_FAILED;
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.IdentityVerification#isWriterVerified(netinf.common.datamodel.InformationObject)
    */
   @Override
   public IdentityVerificationResult isWriterVerified(InformationObject io) throws NetInfCheckedException {

      // check whether there is an IO at all
      if (io == null) {
         LOG.error("No Information Object given.");
         throw new NetInfUncheckedException("No InformationObject given.");
      }

      // check whether writer is given (i.e., WRITER Property is present)
      Attribute writer = io.getSingleAttribute(WRITER.getURI());
      if (writer == null) {
         String error = "Writer attribute is missing.";
         io.addAttribute(identityVerificationFailedAttribute(error));
         LOG.warn(error);
         return IdentityVerificationResult.IDENTITY_NOT_VERIFIABLE;
      }

      // check whether authorized writers list is given (i.e., AUTHORIZED_WRITERS Property is present)
      List<Attribute> authorizedWriters = io.getAttribute(AUTHORIZED_WRITERS.getURI());
      if (authorizedWriters == null) {
         String error = "List of authorized writers is missing.";
         io.addAttribute(identityVerificationFailedAttribute(error));
         LOG.warn(error);
         return IdentityVerificationResult.IDENTITY_NOT_VERIFIABLE;
      }

      // check whether single writer exists if it is required (i.e., SINGLE_WRITER_REQUIRED Property is true)
      Attribute singleWriterRequired = io.getSingleAttribute(SINGLE_WRITER_REQUIRED.getURI());
      if (singleWriterRequired != null) {
         if (singleWriterRequired.getValue(String.class).equals(true)) {
            Attribute singleWriter = io.getSingleAttribute(WRITER_OF_GROUP.getURI());
            if (singleWriter == null) {
               String error = "Single writer of group is missing.";
               io.addAttribute(identityVerificationFailedAttribute(error));
               LOG.warn(error);
               return IdentityVerificationResult.IDENTITY_NOT_VERIFIABLE;
            }
         }
      }

      // Check whether writer is member of authorized writers
      List<String> writers = io.getWriterPaths();
      String writerValue = writer.getValue(String.class);
      if (writers.contains(writerValue)) {
         LOG.debug("Identity Verification Check succeeded. Writer is verified.");
         return IdentityVerificationResult.IDENTITY_VERIFICATION_SUCCEEDED;
      }
      String error = "Writer is not verified.";
      io.addAttribute(identityVerificationFailedAttribute(error));
      LOG.warn(error);
      return IdentityVerificationResult.IDENTITY_VERIFICATION_FAILED;
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.IdentityVerification#isWriterVerified(netinf.common.datamodel.attribute.Attribute)
    */
   @Override
   public IdentityVerificationResult isWriterVerified(Attribute property) throws NetInfCheckedException {
      // check whether there is a property at all
      if (property == null) {
         LOG.error("No Attribute given.");
         throw new NetInfUncheckedException("No Attribute given.");
      }

      // check whether writer is given (i.e., WRITER Property is present)
      Attribute writer = property.getSingleSubattribute(WRITER.getURI());
      if (writer == null) {
         String error = "Writer attribute is missing.";
         property.addSubattribute(identityVerificationFailedAttribute(error));
         LOG.warn(error);
         return IdentityVerificationResult.IDENTITY_NOT_VERIFIABLE;
      }

      // check whether authorized writers list is given for the IO of the Attribute (i.e., AUTHORIZED_WRITERS Property is present)
      InformationObject io = property.getInformationObject();
      Attribute authorizedWriters = io.getSingleAttribute(AUTHORIZED_WRITERS.getURI());
      if (authorizedWriters == null) {
         String error = "List of authorized writers is missing.";
         property.addSubattribute(identityVerificationFailedAttribute(error));
         LOG.warn(error);
         return IdentityVerificationResult.IDENTITY_NOT_VERIFIABLE;
      }

      // check whether single writer is required (i.e., SINGLE_WRITER_REQUIRED Property is true)
      Attribute singleWriterRequired = property.getSingleSubattribute(SINGLE_WRITER_REQUIRED.getURI());
      if (singleWriterRequired != null) {
         if (singleWriterRequired.getValue(String.class).equals(true)) {
            Attribute singleWriter = property.getSingleSubattribute(WRITER_OF_GROUP.getURI());
            if (singleWriter == null) {
               String error = "Single writer of group is missing.";
               property.addSubattribute(identityVerificationFailedAttribute(error));
               LOG.warn(error);
               return IdentityVerificationResult.IDENTITY_NOT_VERIFIABLE;
            }
         }
      }

      // Check whether writer is member of authorized writers
      List<String> writers = io.getWriterPaths();
      String writerValue = writer.getValue(String.class);
      if (writers.contains(writerValue)) {
         LOG.debug("Identity Verification Check succeeded. Writer is verified.");
         return IdentityVerificationResult.IDENTITY_VERIFICATION_SUCCEEDED;
      }
      String error = "Writer is not verified.";
      property.addSubattribute(identityVerificationFailedAttribute(error));
      LOG.warn(error);
      return IdentityVerificationResult.IDENTITY_VERIFICATION_FAILED;
   }

   /**
    * Create an attribute indicating that there was an Identity Verification Error
    * 
    * @param error
    *           Error message
    * @return Attribute containing the error message
    */
   private Attribute identityVerificationFailedAttribute(String error) {
      return this.dmFactory.createAttribute(DefinedAttributeIdentification.IDENTITY_VERIFICATION_FAILED.getURI(), error);
   }

}
