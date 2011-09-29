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

import java.security.NoSuchAlgorithmException;
import java.util.List;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfCheckedSecurityException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.security.Cryptography;
import netinf.common.security.IdentityVerification;
import netinf.common.security.IdentityVerificationResult;
import netinf.common.security.Integrity;
import netinf.common.security.IntegrityResult;
import netinf.common.security.SecurityManager;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * Implements the {@link SecurityManager} Interface. Subsumes all relevant security processes (i.e., encryption and
 * integrity/identity verification)
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class SecurityManagerImpl implements SecurityManager {

   private static final Logger LOG = Logger.getLogger(CryptographyImpl.class);
   private final DatamodelFactory factory;
   private final Cryptography cryptography;
   private final Integrity integrity;
   private final IdentityVerification identityVerification;
   private final boolean suppressCorruptedIOs = false;

   @Inject
   public SecurityManagerImpl(DatamodelFactory factory, Cryptography cryptography, Integrity integrity,
         IdentityVerification identityVerification) {
      LOG.trace(null);
      this.factory = factory;
      this.cryptography = cryptography;
      this.integrity = integrity;
      this.identityVerification = identityVerification;
   }

   @Override
   public InformationObject checkIncommingInformationObject(InformationObject informationObject, String userName,
         String privateKey) throws NetInfCheckedException, NoSuchAlgorithmException {

      if (userName != null && privateKey != null) {
         LOG.info("(SEC  ) Performing (incoming) security-check for " + informationObject.describe()
               + " for user " + userName);
         InformationObject checkedIO = this.factory.copyObject(informationObject);
         checkedIO = decryptInformationObjectRecursively(informationObject, userName, privateKey);
         checkedIO = verifyInformationObjectRecursively(checkedIO);
         return checkedIO;
      } else {
         return checkIncommingInformationObject(informationObject, false);
      }

   }

   @Override
   public InformationObject checkIncommingInformationObject(InformationObject informationObject, boolean receiverIsTrusted)
   throws NetInfCheckedException, NoSuchAlgorithmException {
	  LOG.info("(SEC  ) Performing (incoming) security-check for " + informationObject.describe()
            + " - its receiver is " + (receiverIsTrusted ? "" : "un") + "trusted");
      InformationObject checkedIO = this.factory.copyObject(informationObject);
      checkedIO = decryptInformationObjectRecursively(informationObject, null, null);
      checkedIO = verifyInformationObjectRecursively(checkedIO);
      if (receiverIsTrusted) {
         return checkedIO;
      } else {
         return informationObject;
      }

   }

   @Override
   public InformationObject checkOutgoingInformationObject(InformationObject informationObject, String userName, String privKey)
   throws NetInfCheckedException, NoSuchAlgorithmException {
      if (userName != null && privKey != null) {
    	 LOG.info("(SEC  ) Performing (outgoing) security-check for " + informationObject.describe()
               + " for user " + userName);
         InformationObject checkedIO = this.factory.copyObject(informationObject);
         checkedIO = handleSignCommandsRecursively(informationObject, userName, privKey);
         checkedIO = verifyInformationObjectRecursively(checkedIO);
         checkedIO = handleReaderListsRecursively(checkedIO, true);
         return checkedIO;
      } else {
         return checkOutgoingInformationObject(informationObject, false);
      }
   }

   @Override
   public InformationObject checkOutgoingInformationObject(InformationObject informationObject, boolean senderIsTrusted)
   throws NetInfCheckedException, NoSuchAlgorithmException {
	   LOG.info("(SEC  ) Performing (outgoing) security-check for " + informationObject.describe()
            + " - its sender is " + (senderIsTrusted ? "" : "un") + "trusted");
      InformationObject checkedIO = this.factory.copyObject(informationObject);
      if (senderIsTrusted) {
         checkedIO = handleSignCommandsRecursively(informationObject, null, null);
      } else {
         checkedIO = removeSignCommandsRecursively(informationObject);
      }
      checkedIO = verifyInformationObjectRecursively(checkedIO);
      checkedIO = handleReaderListsRecursively(checkedIO, senderIsTrusted);
      return checkedIO;
   }

   private InformationObject decryptInformationObjectRecursively(InformationObject informationObject, String userName,
         String privateKey) {
      InformationObject newInformationObject = this.factory.createDatamodelObject(informationObject.getClass());
      newInformationObject.setIdentifier(this.factory.copyObject(informationObject.getIdentifier()));
      InformationObject oldInformationObject = informationObject;
      boolean wasEncrypted = false;

      if (informationObject.getSingleAttribute(DefinedAttributeIdentification.ENCRYPTED_INFORMATION_OBJECT.getURI()) != null) {
         try {
            oldInformationObject = this.cryptography.decrypt(oldInformationObject, userName, privateKey);
            wasEncrypted = true;
         } catch (NetInfCheckedSecurityException securityException) {
            LOG.warn("Unable to decrypt InformationObject: " + securityException.getMessage());
         }
      }

      List<Attribute> attributes = oldInformationObject.getAttributes();
      for (Attribute attribute : attributes) {
         // Ede: The following line is necessary, since otherwise it might be that one attribute is removed from one information
         // object automatically, when it is added the another InformationObject.
         Attribute copiedAttribute = this.factory.copyObject(attribute);
         Attribute newAttribute = decryptAttributesRecursively(copiedAttribute, wasEncrypted, userName, privateKey);
         // if a new Attribute was created the old one gets replaced.
         newInformationObject.addAttribute(newAttribute);
      }
      return newInformationObject;
   }

   private Attribute decryptAttributesRecursively(Attribute attribute, boolean superAttributeWasEncrypted, String userName,
         String privateKey) {
      Attribute newAttribute = attribute;
      boolean wasEncrypted = false;

      if (attribute.getIdentification().equalsIgnoreCase(DefinedAttributeIdentification.ENCRYPTED_CONTENT.getURI())) {
         try {
            newAttribute = this.cryptography.decrypt(attribute, userName, privateKey);
            wasEncrypted = true;
         } catch (NetInfCheckedSecurityException securityException) {
            LOG.warn("Unable to decrypt attribute: " + securityException.getMessage());
            return attribute;
         }
      } else if (!superAttributeWasEncrypted
            && attribute.getIdentification().equalsIgnoreCase(DefinedAttributeIdentification.ENCRYPTED_CONTENT.getURI())) {
         return null;
      }

      List<Attribute> subattributes = newAttribute.getSubattributes();
      for (Attribute subattribute : subattributes) {
         Attribute newSubattribute = decryptAttributesRecursively(subattribute, wasEncrypted, userName, privateKey);
         // if a new Attribute was created the old one gets replaced.
         if (newSubattribute != subattribute) {
            newAttribute.removeSubattribute(subattribute);
            if (newSubattribute != null) {
               newAttribute.addSubattribute(newSubattribute);
            }
         }
      }
      return newAttribute;
   }

   private InformationObject verifyInformationObjectRecursively(InformationObject informationObject)
   throws NetInfCheckedException, NoSuchAlgorithmException {
      // Result of Verification should not be present before IO has been verified. Delete such information
      if (informationObject.getSingleAttribute(DefinedAttributeIdentification.SIGNATURE_VERIFICATION_FAILED.getURI()) != null) {
         informationObject.removeAttribute(DefinedAttributeIdentification.SIGNATURE_VERIFICATION_FAILED.getURI());
      }

      if (informationObject.getSingleAttribute(DefinedAttributeIdentification.SIGNATURE.getURI()) != null) {
         // A Signature-Attribute without a corresponding SignatureIdentigication attribute is invalid. It is deleted.
         if (informationObject.getSingleAttribute(DefinedAttributeIdentification.SIGNATURE.getURI()).getSingleSubattribute(
               DefinedAttributeIdentification.SIGNATURE_IDENTIFICATION.getURI()) != null) {
            informationObject.removeAttribute(DefinedAttributeIdentification.SIGNATURE.getURI());
            // A Writer-Attribute without a corresponding signature attribute is an instruction to sign if possible. An incoming
            // object should have no instructions since the sender is not trusted. The instruction is deleted.
            if (informationObject.getSingleAttribute(DefinedAttributeIdentification.WRITER.getURI()) != null) {
               informationObject.removeAttribute(DefinedAttributeIdentification.WRITER.getURI());
            }
         } else {

            IntegrityResult result = this.integrity.isSignatureValid(informationObject);
            if (this.suppressCorruptedIOs) {
               switch (result) {
               case INTEGRITY_CHECK_FAIL:
                  throw new NetInfCheckedSecurityException("Integrity check failed.");
               case INTEGRITY_NOT_TESTABLE:
                  throw new NetInfCheckedSecurityException("Integrity not testable.");
               case INTEGRITY_CHECK_SUCCEEDED:
                  break;
               default:
                  throw new NetInfCheckedSecurityException("Integrity check result unknown.");
               }
            }
            // TODO wait for convenient methods to create IOs
            verifyIdenties(informationObject);
         }
      } else {
         // A Writer-Attribute without a corresponding signature attribute is an instruction to sign if possible. An incoming
         // object should have no instructions since the sender is not trusted. The instruction is deleted.
         if (informationObject.getSingleAttribute(DefinedAttributeIdentification.WRITER.getURI()) != null) {
            informationObject.removeAttribute(DefinedAttributeIdentification.WRITER.getURI());
         }
      }

      List<Attribute> attributes = informationObject.getAttributes();
      for (Attribute attribute : attributes) {
         Attribute newAttribute = verifyAttributesRecursively(attribute);
         // if a new Attribute was created the old one gets replaced.
         if (newAttribute != attribute) {
            informationObject.removeAttribute(attribute);
            if (newAttribute != null) {
               informationObject.addAttribute(newAttribute);
            }
         }
      }
      return informationObject;
   }

   private Attribute verifyAttributesRecursively(Attribute attribute) throws NetInfCheckedException {
      // Result of Verification should not be present before Attribute has been verified. Delete such information
      if (attribute.getSingleSubattribute(DefinedAttributeIdentification.SIGNATURE_VERIFICATION_FAILED.getURI()) != null) {
         attribute.removeSubattribute(DefinedAttributeIdentification.SIGNATURE_VERIFICATION_FAILED.getURI());
      }

      if (attribute.getSingleSubattribute(DefinedAttributeIdentification.SIGNATURE.getURI()) != null) {
         // A Signature-Attribute without a corresponding SignatureIdentification-Subattribute is invalid. Remove
         // Signature.
         if (attribute.getSingleSubattribute(DefinedAttributeIdentification.SIGNATURE.getURI()).getSingleSubattribute(
               DefinedAttributeIdentification.SIGNATURE_IDENTIFICATION.getURI()) == null) {
            attribute.removeSubattribute(DefinedAttributeIdentification.SIGNATURE.getURI());

            // A Writer-Attribute without a corresponding signature attribute is an instruction to sign if possible. An incoming
            // object should have no instructions since the sender is not trusted. The instruction is deleted.
            if (attribute.getSingleSubattribute(DefinedAttributeIdentification.WRITER.getURI()) == null) {
               attribute.removeSubattribute(DefinedAttributeIdentification.WRITER.getURI());
            }
         }
         IntegrityResult integrityResult = this.integrity.isSignatureValid(attribute);
         if (this.suppressCorruptedIOs) {
            switch (integrityResult) {
            case INTEGRITY_CHECK_FAIL:
               throw new NetInfCheckedSecurityException("Integrity check failed.");
            case INTEGRITY_NOT_TESTABLE:
               throw new NetInfCheckedSecurityException("Integrity not testable.");
            case INTEGRITY_CHECK_SUCCEEDED:
               break;
            default:
               throw new NetInfCheckedSecurityException("Integrity check result unknown.");
            }
         } else {
            // A Writer-Attribute without a corresponding signature attribute is an instruction to sign if possible. An incoming
            // object should have no instructions since the sender is not trusted. The instruction is deleted.
            if (attribute.getSingleSubattribute(DefinedAttributeIdentification.WRITER.getURI()) == null) {
               attribute.removeSubattribute(DefinedAttributeIdentification.SIGNATURE.getURI());
            }
         }

         // TODO wait for convenient methods to create IOs
         IdentityVerificationResult identityResult = this.identityVerification.isWriterVerified(attribute);
         if (this.suppressCorruptedIOs) {
            switch (identityResult) {
            case IDENTITY_NOT_VERIFIABLE:
               throw new NetInfCheckedSecurityException("Identity not verifiable.");
            case IDENTITY_VERIFICATION_FAILED:
               throw new NetInfCheckedSecurityException("Identity verification failed.");
            case IDENTITY_VERIFICATION_SUCCEEDED:
               break;
            default:
               throw new NetInfCheckedSecurityException("Identity verification result unknown.");
            }
         }
      }

      List<Attribute> subattributes = attribute.getSubattributes();
      for (Attribute subattribute : subattributes) {
         Attribute newSubattribute = verifyAttributesRecursively(subattribute);
         // if a new Attribute was created the old one gets replaced.
         if (newSubattribute != subattribute) {
            attribute.removeSubattribute(subattribute);
            if (newSubattribute != null) {
               attribute.addSubattribute(newSubattribute);
            }
         }
      }
      return attribute;
   }

   private void verifyIdenties(InformationObject informationObject) throws NetInfCheckedException, NoSuchAlgorithmException {
      IdentityVerificationResult result = this.identityVerification.isOwnerVerified(informationObject);
      if (this.suppressCorruptedIOs) {
         switch (result) {
         case IDENTITY_NOT_VERIFIABLE:
            throw new NetInfCheckedSecurityException("Identity not verifiable.");
         case IDENTITY_VERIFICATION_FAILED:
            throw new NetInfCheckedSecurityException("Identity verification failed.");
         case IDENTITY_VERIFICATION_SUCCEEDED:
            break;
         default:
            throw new NetInfCheckedSecurityException("Identity verification result unknown.");
         }
      }

      result = this.identityVerification.isWriterVerified(informationObject);
      if (this.suppressCorruptedIOs) {
         switch (result) {
         case IDENTITY_NOT_VERIFIABLE:
            throw new NetInfCheckedSecurityException("Identity not verifiable.");
         case IDENTITY_VERIFICATION_FAILED:
            throw new NetInfCheckedSecurityException("Identity verification failed.");
         case IDENTITY_VERIFICATION_SUCCEEDED:
            break;
         default:
            throw new NetInfCheckedSecurityException("Identity verification result unknown.");
         }
      }

      result = this.identityVerification.isIOVerifiedByOwner(informationObject);
      if (this.suppressCorruptedIOs) {
         switch (result) {
         case IDENTITY_NOT_VERIFIABLE:
            throw new NetInfCheckedSecurityException("Identity not verifiable.");
         case IDENTITY_VERIFICATION_FAILED:
            throw new NetInfCheckedSecurityException("Identity verification failed.");
         case IDENTITY_VERIFICATION_SUCCEEDED:
            break;
         default:
            throw new NetInfCheckedSecurityException("Identity verification result unknown.");
         }
      }
   }

   private InformationObject handleSignCommandsRecursively(InformationObject informationObject, String userName, String privKey)
   throws NetInfCheckedException {
      List<Attribute> attributes = informationObject.getAttributes();
      boolean writer = false;
      boolean signature = false;
      for (Attribute attribute : attributes) {
         if (attribute.getIdentification().equals(DefinedAttributeIdentification.WRITER.getURI())) {
            writer = true;
         } else if (attribute.getIdentification().equals(DefinedAttributeIdentification.SIGNATURE.getURI())) {
            signature = true;
         } else {
            Attribute newSubattribute = handleSignCommandRecursiveley(attribute, userName, privKey);
            // if a new Attribute was created the old one gets replaced.
            if (newSubattribute != attribute) {
               informationObject.removeAttribute(attribute);
               if (newSubattribute != null) {
                  informationObject.addAttribute(newSubattribute);
               }
            }
         }
      }
      // A Writer-Attribute without a corresponding signature attribute is an instruction to sign if possible.
      if (writer && !signature) {
         switch (this.integrity.sign(informationObject)) {
         case SIGNING_FAILED:
            throw new NetInfCheckedSecurityException("Sign failed.");
         case SIGNING_SUCCEEDED:
            break;
         default:
            throw new NetInfCheckedSecurityException("Sign result unknown.");
         }
      }
      return informationObject;
   }

   private Attribute handleSignCommandRecursiveley(Attribute attribute, String userName, String privateKey)
   throws NetInfCheckedException {
      List<Attribute> subattributes = attribute.getSubattributes();
      boolean writer = false;
      boolean signature = false;
      for (Attribute subattribute : subattributes) {
         if (subattribute.getIdentification().equals(DefinedAttributeIdentification.WRITER.getURI())) {
            writer = true;
         } else if (subattribute.getIdentification().equals(DefinedAttributeIdentification.SIGNATURE.getURI())) {
            signature = true;
         } else {
            Attribute newSubattribute = handleSignCommandRecursiveley(subattribute, userName, privateKey);
            // if a new Attribute was created the old one gets replaced.
            if (newSubattribute != subattribute) {
               attribute.removeSubattribute(subattribute);
               if (newSubattribute != null) {
                  attribute.addSubattribute(newSubattribute);
               }
            }
         }
      }
      // A Writer-Attribute without a corresponding signature attribute is an instruction to sign if possible.
      if (writer && !signature) {
         switch (this.integrity.sign(attribute, userName, privateKey)) {
         case SIGNING_FAILED:
            throw new NetInfCheckedSecurityException("Sign failed.");
         case SIGNING_SUCCEEDED:
            break;
         default:
            throw new NetInfCheckedSecurityException("Sign result unknown.");
         }
      }

      return attribute;
   }

   private InformationObject removeSignCommandsRecursively(InformationObject informationObject) throws NetInfCheckedException {
      List<Attribute> attributes = informationObject.getAttributes();
      boolean writer = false;
      boolean signature = false;
      for (Attribute attribute : attributes) {
         if (attribute.getIdentification().equals(DefinedAttributeIdentification.WRITER.getURI())) {
            writer = true;
         } else if (attribute.getIdentification().equals(DefinedAttributeIdentification.SIGNATURE.getURI())) {
            signature = true;
         } else {
            Attribute newSubattribute = removeSignCommandRecursiveley(attribute);
            // if a new Attribute was created the old one gets replaced.
            if (newSubattribute != attribute) {
               informationObject.removeAttribute(attribute);
               if (newSubattribute != null) {
                  informationObject.addAttribute(newSubattribute);
               }
            }
         }
      }
      if (writer && !signature) {
         informationObject.removeAttribute(DefinedAttributeIdentification.WRITER.getURI());
      }
      return informationObject;
   }

   private Attribute removeSignCommandRecursiveley(Attribute attribute) throws NetInfCheckedException {
      List<Attribute> subattributes = attribute.getSubattributes();
      boolean writer = false;
      boolean signature = false;
      for (Attribute subattribute : subattributes) {
         if (subattribute.getIdentification().equals(DefinedAttributeIdentification.WRITER.getURI())) {
            writer = true;
         } else if (subattribute.getIdentification().equals(DefinedAttributeIdentification.SIGNATURE.getURI())) {
            signature = true;
         } else {
            Attribute newSubattribute = removeSignCommandRecursiveley(subattribute);
            // if a new Attribute was created the old one gets replaced.
            if (newSubattribute != subattribute) {
               attribute.removeSubattribute(subattribute);
               if (newSubattribute != null) {
                  attribute.addSubattribute(newSubattribute);
               }
            }
         }
      }

      if (writer && !signature) {
         attribute.removeSubattribute(DefinedAttributeIdentification.WRITER.getURI());
      }
      return attribute;
   }

   private InformationObject handleReaderListsRecursively(InformationObject informationObject, boolean encrypt)
   throws NetInfCheckedSecurityException {
      List<Attribute> attributes = informationObject.getAttributes();
      boolean readerList = false;
      for (Attribute attribute : attributes) {
         if (attribute.getIdentification().equals(DefinedAttributeIdentification.AUTHORIZED_READERS.getURI())) {
            readerList = true;
         } else {
            Attribute newSubattribute = handleReaderListsRecursively(attribute, encrypt);
            // if a new Attribute was created the old one gets replaced.
            if (newSubattribute != attribute) {
               informationObject.removeAttribute(attribute);
               if (newSubattribute != null) {
                  informationObject.addAttribute(newSubattribute);
               }
            }
         }
      }
      // A ReaderList-Attribute without a corresponding signature attribute is an instruction to encrypt if possible. This will
      // only be done if the user who pushes the InformationObject is trusted.
      if (encrypt && readerList) {
         return this.cryptography.encrypt(informationObject);
      } else {
         // If the user is not trusted the instruction to encrypt is removed.
         if (readerList) {
            informationObject.removeAttribute(DefinedAttributeIdentification.AUTHORIZED_READERS.getURI());
         }
         return informationObject;
      }
   }

   private Attribute handleReaderListsRecursively(Attribute attribute, boolean encrypt) throws NetInfCheckedSecurityException {
      List<Attribute> subattributes = attribute.getSubattributes();
      boolean readerList = false;
      for (Attribute subattribute : subattributes) {
         if (subattribute.getIdentification().equals(DefinedAttributeIdentification.AUTHORIZED_READERS.getURI())) {
            readerList = true;
         } else {
            Attribute newSubattribute = handleReaderListsRecursively(subattribute, encrypt);
            // if a new Attribute was created the old one gets replaced.
            if (newSubattribute != subattribute) {
               attribute.removeSubattribute(subattribute);
               if (newSubattribute != null) {
                  attribute.addSubattribute(newSubattribute);
               }
            }
         }
      }
      // A ReaderList-Attribute without a corresponding signature attribute is an instruction to encrypt if possible. This will
      // only be done if the user who pushes the InformationObject is trusted.
      if (encrypt && readerList) {
         return this.cryptography.encrypt(attribute);
      } else {
         // If the user is not trusted the instruction to encrypt is removed.
         if (readerList) {
            attribute.removeSubattribute(DefinedAttributeIdentification.AUTHORIZED_READERS.getURI());
         }
         return attribute;
      }
   }
}
