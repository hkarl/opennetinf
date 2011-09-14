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
package netinf.common.security.identity;

import java.security.PrivateKey;

import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.exceptions.NetInfCheckedException;

/**
 * Identity Manager manages locally known Identites (i.e., Identity Objects and related Private Keys for Signing, Encrypting,
 * etc.)
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface IdentityManager {

   /**
    * Checks whether the Private Key that belongs to a certain (Sub-) Identity is available.
    * 
    * @param pathToPublicKey
    *           Reference to (Sub-)Identity to get the Private Key of.
    * @return true if the PrivateKey is available.
    */
   boolean hasPrivateKey(String pathToPublicKey);

   /**
    * Checks whether the Private Key that belongs to a certain (Sub-) Identity is available.
    * 
    * @param pathToPublicKey
    *           Reference to (Sub-)Identity to get the Private Key of.
    * @param userName
    *           The username used for a client to store his Private Keys.
    * @param privateKey
    *           The private key used for a client to store his Private Keys.
    * @return true if the PrivateKey is available.
    */
   boolean hasPrivateKey(String pathToPublicKey, String userName, String privateKey);

   /**
    * Get the Private Key that belongs to a certain (Sub-) Identity.
    * 
    * @param pathToPublicKey
    *           Reference to (Sub-)Identity to get the Private Key of.
    * @return Private Key according to referenced (Sub-)Identity.
    */
   PrivateKey getPrivateKey(String pathToPublicKey) throws NetInfCheckedException;

   /**
    * Get the Private Key that belongs to a certain (Sub-) Identity.
    * 
    * @param pathToPublicKey
    *           Reference to (Sub-)Identity to get the Private Key of.
    * @param userName
    *           The username used for a client to store his Private Keys.
    * @param privateKey
    *           The private key used for a client to store his Private Keys.
    * @param pathToPublicKey
    *           Reference to (Sub-)Identity to get the Private Key of.
    * @return Private Key according to referenced (Sub-)Identity.
    */
   PrivateKey getPrivateKey(String pathToPublicKey, String userName, String privateKey) throws NetInfCheckedException;

   /**
    * Get a local (Sub-) Identity that can be used for Signing, Encrypting,... IOs and Properties
    * 
    * @return Path to a Identity of the local node (i.e., the Private Key is known). E.g.,
    *         IDENTIFIER/DefinedPropertyIdentification.PUBLIC_KEY.getURI()
    */
   String getLocalIdentity() throws NetInfCheckedException;

   /**
    * Creates a new Identity Object, containing a Master Key Pair Don't forget to sign and push the IdentityObject to Resolution
    * Service, if you want others to be able to access it.
    * 
    * @return IdentityObject with the new Identity.
    */
   IdentityObject createNewMasterIdentity() throws NetInfCheckedException;

   /**
    * Creates a new SubKey in an IdentityObject
    * 
    * @param identityObject
    *           IdentityObject to create the new SubIdentity in
    * @param path
    *           Path to create the SubKey in (including the Identifier of the IO)
    * @return Reference to the new SubIdentity, e.g. IDENTIFIER/DefinedPropertyIdentification.SUB_IDENTITY.getURI()
    */
   String createNewSubKey(IdentityObject identityObject, String path);

   /**
    * Explicitly ask IdentityManager to load Identities from preset path using preset password
    * 
    * @throws NetInfCheckedException
    *            In case Identities could not be loaded from file
    */
   void loadIdentities() throws NetInfCheckedException;

   /**
    * Explicitly ask IdentityManager to save Identities to preset path using preset password
    * 
    * @throws NetInfCheckedException
    *            In case Identities could not be saved to file
    */
   void storeIdentities() throws NetInfCheckedException;

   /**
    * Set Password to use for encrypting and decrypting the local Identities file
    * 
    * @param password
    *           Password to encrypt and decrypt local Identities file
    */
   void setPassword(String password);

   /**
    * Set file path of local Identities file to store to and load from
    * 
    * @param path
    *           file path of local Identities file
    */
   void setFilePath(String path);
}
