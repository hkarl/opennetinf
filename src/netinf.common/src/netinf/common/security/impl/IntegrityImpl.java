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

import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.SIGNATURE;
import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.SIGNATURE_IDENTIFICATION;
import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.WRITER;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.creator.ValidCreator;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.security.DefinedSignatureIdentification;
import netinf.common.security.Integrity;
import netinf.common.security.IntegrityResult;
import netinf.common.security.SignatureAlgorithm;
import netinf.common.security.identity.IdentityManager;
import netinf.common.security.impl.module.SecurityModule.Security;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * The Class IntegrityImpl. {@link IntegrityImpl} implements the {@link Integrity} interface.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IntegrityImpl implements Integrity {

   private static final Logger LOG = Logger.getLogger(IntegrityImpl.class);
   private final DatamodelFactory datamodelFactory;
   private final SignatureAlgorithm signatureAlgorithm;
   /**
    * Connection to a Resolution Service, to get IOs required for Integrity Verification from
    */
   private final NetInfNodeConnection nodeConnection;
   private final IdentityManager identityManager;

   public static final String PATH_SEPERATOR = "?";

   @Inject
   public IntegrityImpl(DatamodelFactory dmfactory, SignatureAlgorithm sigAlg, @Security NetInfNodeConnection nodeConnection,
         IdentityManager identityManager) {
      this.signatureAlgorithm = sigAlg;
      this.datamodelFactory = dmfactory;
      this.nodeConnection = nodeConnection;
      // this.nodeConnection.setSerializeFormat(SerializeFormat.JAVA);
      this.identityManager = identityManager;

      LOG.debug("Integrity Module created.");
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.Integrity#isSignatureValid(netinf.common.datamodel.InformationObject)
    */
   @Override
   public IntegrityResult isSignatureValid(InformationObject io) throws NetInfCheckedException {
      // check whether there is a IO at all
      if (io == null) {
         LOG.error("No Information Object given.");
         throw new NetInfUncheckedException("No Information Object given.");
      }

      // check whether writer is given (i.e., Writer Property is present)
      Attribute writer = io.getSingleAttribute(WRITER.getURI());
      if (writer == null) {
         String error = "Writer is missing.";
         io.addAttribute(signatureVerificationFailedAttribute(error));
         LOG.warn(error);
         return IntegrityResult.INTEGRITY_NOT_TESTABLE;
      }

      Map<String, PublicKey> publicKeys = getWritersPublicKey(writer);

      // check whether IO is signed (i.e., Signature Property is present)
      Attribute signature = io.getSingleAttribute(SIGNATURE.getURI());
      if (signature == null) {
         String error = "No Signature present.";
         io.addAttribute(signatureVerificationFailedAttribute(error));
         LOG.warn(error);
         return IntegrityResult.INTEGRITY_NOT_TESTABLE;
      }
      Attribute signatureIdent = signature.getSingleSubattribute(SIGNATURE_IDENTIFICATION.getURI());
      if (signatureIdent == null) {
         String error = "Signature Identification missing.";
         io.addAttribute(signatureVerificationFailedAttribute(error));
         LOG.warn(error);
         return IntegrityResult.INTEGRITY_NOT_TESTABLE;
      }

      // get Content String, to compare it to signed one
      String contentString = getContentString(io);

      // if contentString and signed one equal, IO has valid Signature
      try {
         PublicKey publicKey = publicKeys.get(writer.getValue(String.class));

         String signatureString = signature.getValue(String.class);

         switch (DefinedSignatureIdentification.getDefinedSignatureIdentificationByIdentificationString(signatureIdent
               .getValue(String.class))) {
               case SHA1_WITH_DSA:
                  if (this.signatureAlgorithm.verifySignature(contentString, signatureString, publicKey, "SHA1withDSA")) {
                     LOG.debug("Integrity Check succeeded.");
                     return IntegrityResult.INTEGRITY_CHECK_SUCCEEDED;
                  }
                  break;
               case SHA1_WITH_RSA:
                  if (this.signatureAlgorithm.verifySignature(contentString, signatureString, publicKey, "SHA1withRSA")) {
                     LOG.debug("Integrity Check succeeded.");
                     return IntegrityResult.INTEGRITY_CHECK_SUCCEEDED;
                  }
                  break;
               default:
                  // Signature Algorithm not known. Add error Attribute
                  break;
         }
      } catch (Exception e) {
         LOG.debug(e.getMessage());
      }

      String error = "Integrity Check failed.";
      io.addAttribute(signatureVerificationFailedAttribute(error));
      LOG.debug(error);
      return IntegrityResult.INTEGRITY_CHECK_FAIL;
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.Integrity#isSignatureValid(netinf.common.datamodel.property.Property)
    */
   @Override
   public IntegrityResult isSignatureValid(Attribute property) throws NetInfCheckedException {
      // check whether there is a property at all
      if (property == null) {
         LOG.warn("No Property given.");
         return IntegrityResult.INTEGRITY_NOT_TESTABLE;
      }

      // check whether writer is given (i.e., Writer Property is present)
      Attribute writer = property.getSingleSubattribute(WRITER.getURI());
      if (writer == null) {
         String error = "Writer is missing.";
         property.addSubattribute(signatureVerificationFailedAttribute(error));
         LOG.warn(error);
         return IntegrityResult.INTEGRITY_NOT_TESTABLE;
      }

      Map<String, PublicKey> publicKeys = getWritersPublicKey(writer);

      // check whether IO is signed (i.e., Signature Property is present)
      Attribute signature = property.getSingleSubattribute(SIGNATURE.getURI());
      if (signature == null) {
         String error = "No Signature present.";
         property.addSubattribute(signatureVerificationFailedAttribute(error));
         LOG.warn(error);
         return IntegrityResult.INTEGRITY_NOT_TESTABLE;
      }
      Attribute signatureIdent = signature.getSingleSubattribute(SIGNATURE_IDENTIFICATION.getURI());
      if (signatureIdent == null) {
         String error = "Signature Identification missing.";
         property.addSubattribute(signatureVerificationFailedAttribute(error));
         LOG.warn(error);
         return IntegrityResult.INTEGRITY_NOT_TESTABLE;
      }

      String contentString = getAttributeContent(property);

      // if contentString and signed one equal, IO has valid Signature
      try {
         PublicKey publicKey = publicKeys.get(writer.getValue(String.class));

         String signatureString = signature.getValue(String.class);

         switch (DefinedSignatureIdentification.getDefinedSignatureIdentificationByIdentificationString(signatureIdent
               .getValue(String.class))) {
               case SHA1_WITH_DSA:
                  if (this.signatureAlgorithm.verifySignature(contentString, signatureString, publicKey, "SHA1withDSA")) {
                     LOG.debug("Integrity Check succeeded.");
                     return IntegrityResult.INTEGRITY_CHECK_SUCCEEDED;
                  }
                  break;
               case SHA1_WITH_RSA:
                  if (this.signatureAlgorithm.verifySignature(contentString, signatureString, publicKey, "SHA1withRSA")) {
                     LOG.debug("Integrity Check succeeded.");
                     return IntegrityResult.INTEGRITY_CHECK_SUCCEEDED;
                  }
                  break;
               default:
                  // Signature Algorithm not known. Add error Attribute
                  break;
         }
      } catch (Exception e) {
         LOG.debug(e.getMessage());
      }

      String error = "Integrity Check failed.";
      property.addSubattribute(signatureVerificationFailedAttribute(error));
      LOG.debug(error);
      return IntegrityResult.INTEGRITY_CHECK_FAIL;
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.Integrity#sign(netinf.common.datamodel.InformationObject)
    */
   @Override
   public IntegrityResult sign(InformationObject io) {
      return sign(io, null, null);
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.Integrity#sign(netinf.common.datamodel.InformationObject)
    */
   @Override
   public IntegrityResult sign(InformationObject io, String userName, String privateKey) {

      // get Writer
      Attribute writerProperty = io.getSingleAttribute(WRITER.getURI());
      if (writerProperty == null) {
         LOG.warn("Can't sign, because there is no writer attribute");
         return IntegrityResult.SIGNING_FAILED;
      }

      // get Writer's path to public key
      String pathToPublicKey = writerProperty.getValue(String.class);

      // get Writer's private key according to public key path
      PrivateKey privKey;
      try {
         privKey = this.identityManager.getPrivateKey(pathToPublicKey, userName, privateKey);
      } catch (NetInfCheckedException e1) {
         LOG.warn("Could not get private key. Did not sign!");
         return IntegrityResult.SIGNING_FAILED;
      }

      String contentString = getContentString(io);

      String signedContentString = null;
      try {
         signedContentString = this.signatureAlgorithm.sign(contentString, privKey, "SHA1withRSA");
      } catch (Exception e) {
         LOG.warn("Signing failed.");
         return IntegrityResult.SIGNING_FAILED;
      }

      // add signed contentString as a property to the IO
      Attribute signatureProperty = io.getSingleAttribute(SIGNATURE.getURI());
      if (signatureProperty == null) {
         signatureProperty = ValidCreator.createValidAttribute(SIGNATURE, null, DefinedAttributePurpose.SYSTEM_ATTRIBUTE);
      }
      signatureProperty.setValue(signedContentString);
      io.addAttribute(signatureProperty);

      // add signature Ident that was used to sign
      Attribute signatureIdentificationProperty = signatureProperty.getSingleSubattribute(SIGNATURE_IDENTIFICATION.getURI());
      if (signatureIdentificationProperty == null) {
         signatureIdentificationProperty = ValidCreator.createValidAttribute(SIGNATURE_IDENTIFICATION, null,
               DefinedAttributePurpose.SYSTEM_ATTRIBUTE);
      }
      signatureIdentificationProperty.setValue(DefinedSignatureIdentification
            .getIdentificationStringBySignatureIdentification(DefinedSignatureIdentification.SHA1_WITH_RSA));
      signatureProperty.addSubattribute(signatureIdentificationProperty);

      LOG.debug("Signing succeeded");
      return IntegrityResult.SIGNING_SUCCEEDED;
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.Integrity#sign(netinf.common.datamodel.property.Property)
    */
   @Override
   public IntegrityResult sign(Attribute attribute) {
      return sign(attribute, null, null);
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.Integrity#sign(netinf.common.datamodel.property.Property)
    */
   @Override
   public IntegrityResult sign(Attribute attribute, String userName, String privateKey) {
      // get Writer
      Attribute writerProperty = attribute.getSingleSubattribute(WRITER.getURI());
      if (writerProperty == null) {
         LOG.warn("Can't sign, because there is no writer attribute");
         return IntegrityResult.SIGNING_FAILED;
      }

      // get Writer's path to public key
      String pathToPublicKey = writerProperty.getValue(String.class);

      // get Writer's private key according to public key path
      PrivateKey privKey;
      try {
         privKey = this.identityManager.getPrivateKey(pathToPublicKey, userName, privateKey);
      } catch (NetInfCheckedException e1) {
         LOG.warn("Could not get private key. Did not sign!");
         return IntegrityResult.SIGNING_FAILED;
      }

      String contentString = getAttributeContent(attribute);

      String signedContentString = null;
      try {
         signedContentString = this.signatureAlgorithm.sign(contentString, privKey, "SHA1withRSA");
      } catch (Exception e) {
         LOG.warn("Signing failed");
         return IntegrityResult.SIGNING_FAILED;
      }

      // add signed contentString as a subproperty
      Attribute signatureProperty = attribute.getSingleSubattribute(SIGNATURE.getURI());
      if (signatureProperty == null) {
         signatureProperty = ValidCreator.createValidAttribute(SIGNATURE, null, DefinedAttributePurpose.SYSTEM_ATTRIBUTE);
      }
      signatureProperty.setValue(signedContentString);
      attribute.addSubattribute(signatureProperty);

      // add signature Ident that was used to sign
      Attribute signatureIdentificationProperty = signatureProperty.getSingleSubattribute(SIGNATURE_IDENTIFICATION.getURI());
      if (signatureIdentificationProperty == null) {
         signatureIdentificationProperty = ValidCreator.createValidAttribute(SIGNATURE_IDENTIFICATION, null,
               DefinedAttributePurpose.SYSTEM_ATTRIBUTE);
      }
      signatureIdentificationProperty.setValue(DefinedSignatureIdentification
            .getIdentificationStringBySignatureIdentification(DefinedSignatureIdentification.SHA1_WITH_RSA));
      signatureProperty.addSubattribute(signatureIdentificationProperty);

      LOG.debug("Signing succeeded.");
      return IntegrityResult.SIGNING_SUCCEEDED;
   }

   /**
    * Get the complete content of an IO by concatenating Identifier, Type of IO and all the data of Attributes
    * 
    * @param io
    *           IO to get the content of
    * @return String containing all the data of an IO
    */
   private String getContentString(InformationObject io) {
      StringBuilder contentString = new StringBuilder();
      // add Identifier and type of IO to contentString
      contentString.append(io.getIdentifier().toString());

      // add type of IO
      Attribute ioType = io.getSingleAttribute(DefinedAttributeIdentification.IO_TYPE.getURI());
      if (ioType != null) {
         contentString.append(ioType.getValue(String.class));
      }

      // get a String of the content to be secured
      contentString.append(getContentString(io.getAttributes()));

      return contentString.toString();
   }

   /**
    * Recursively creates a String of all Properties that shall be signed. Is invoked by
    * {@link IntegrityImpl.getHashedContentString}
    * 
    * @param properties
    *           Properties to walk through recursively
    * @return String of information in properties of this property-subtree
    */
   private String getContentString(List<Attribute> properties) {
      // check whether there are properties at all
      if (properties == null) {
         return "";
      }

      String contentString = new String();

      // iterate all properties to secure them
      for (int i = 0; i < properties.size(); i++) {
         Attribute currentProperty = properties.get(i);

         // check whether this property is secured in overall
         if (hasSubproperty(currentProperty, DefinedAttributeIdentification.SECURED_IN_OVERALL)) {
            // currentProperty has to be secured in overall, so add it to ContentString
            contentString = contentString.concat(getAttributeContent(currentProperty));
         }

         // recursively walk the subproperties
         contentString = contentString.concat(getContentString(currentProperty.getSubattributes()));
      }

      return contentString;
   }

   /**
    * Get the content of an attribute as String
    * 
    * @param attribute
    *           Attribute to get content string of
    * @return String of content
    */
   private String getAttributeContent(Attribute attribute) {
      String contentString = new String();

      // Add Identifier to contentString to bind Attribute-Signature to IO
      contentString = contentString.concat(attribute.getInformationObject().getIdentifier().toString());

      // Walk tree of properties upwards and save path
      String attributePath = new String();
      Attribute attributePointer = attribute;
      while (attributePointer.getParentAttribute() != null) {
         attributePointer = attributePointer.getParentAttribute();
         attributePath = attributePointer.getIdentification() + attributePath;
      }
      contentString = contentString.concat(attributePath);

      // Identification of this Attribute
      contentString = contentString.concat(attribute.getIdentification());
      // Value of this Attribute
      contentString = contentString.concat(attribute.getValueRaw());
      // Purpose of this Property
      // contentString = contentString.concat(property.getPropertyPurpose());

      return contentString;
   }

   /**
    * Checks a given property for existence of a certain subproperty
    * 
    * @param property
    *           Property to check for existence of subproperty
    * @param signatureIdentification
    *           Identification of subproperty to look for
    * @return has the property a subproperty with identification propertyIdentification?
    */
   private boolean hasSubproperty(Attribute property, DefinedAttributeIdentification signatureIdentification) {
      if (property.getSingleSubattribute(signatureIdentification.getURI()) != null) {
         return true;
      }

      return false;
   }

   /**
    * Given a writer Attribute, retrieve the Public Key of the writer.
    * 
    * @param writer
    *           Writer attribute that contains the path to the writers identity
    * @return Mapping of path to Writer Identity and Public Key of the that writer
    * @throws NetInfCheckedException
    */
   private Map<String, PublicKey> getWritersPublicKey(Attribute writer) throws NetInfCheckedException {
      Map<String, PublicKey> publicKeys = new HashMap<String, PublicKey>();

      if (writer.getInformationObject() instanceof IdentityObject) {
         // Attributes in IdentityObject are only allowed to be signed by MasterKey, so append that key to KeyList
         publicKeys.put(writer.getInformationObject().getIdentifier().toString() + PATH_SEPERATOR
               + DefinedAttributeIdentification.PUBLIC_KEY.getURI(), ((IdentityObject) writer.getInformationObject())
               .getPublicMasterKey());
      } else {
         // extract the identifier from IdentityPath
         String writerIdentifierString = writer.getValue(String.class);
         writerIdentifierString = writerIdentifierString.substring(0, writerIdentifierString.indexOf(PATH_SEPERATOR));

         Identifier identifier = this.datamodelFactory.createIdentifierFromString(writerIdentifierString);

         InformationObject informationObject = this.nodeConnection.getIO(identifier);
         if (informationObject instanceof IdentityObject) {
            // TODO this has to be modified when Subidentities are used
            publicKeys.put(writer.getValue(String.class), ((IdentityObject) informationObject).getPublicMasterKey());
         }
      }

      return publicKeys;
   }

   /**
    * Create an attribute indicating that there was an Signature Verification Error
    * 
    * @param error
    *           Error message
    * @return Attribute containing the error message
    */
   private Attribute signatureVerificationFailedAttribute(String error) {
      return ValidCreator.createValidAttribute(DefinedAttributeIdentification.SIGNATURE_VERIFICATION_FAILED, error,
            DefinedAttributePurpose.SYSTEM_ATTRIBUTE);
   }

}
