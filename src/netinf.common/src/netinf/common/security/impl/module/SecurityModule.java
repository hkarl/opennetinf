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
package netinf.common.security.impl.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import netinf.common.communication.RemoteNodeConnection;
import netinf.common.security.CryptoAlgorithm;
import netinf.common.security.Cryptography;
import netinf.common.security.IdentityVerification;
import netinf.common.security.Integrity;
import netinf.common.security.SecurityManager;
import netinf.common.security.SignatureAlgorithm;
import netinf.common.security.identity.IdentityManager;
import netinf.common.security.identity.impl.IdentityManagerImpl;
import netinf.common.security.impl.CryptoAlgorithmImpl;
import netinf.common.security.impl.CryptographyImpl;
import netinf.common.security.impl.IdentityVerificationImpl;
import netinf.common.security.impl.IntegrityImpl;
import netinf.common.security.impl.SecurityManagerImpl;
import netinf.common.security.impl.SignatureAlgorithmImpl;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Singleton;

/**
 * Module binding Security related classes
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class SecurityModule extends AbstractModule {

   /**
    * Security logic may need to retrieve IOs to encrypt/decrypt, check integrity, verify identities, etc. Inside a node, this
    * should not be done using a ConvenienceCommunicator, but by directly resolving the IO inside the node. The following lines of
    * code allow to use a different {@link RemoteNodeConnection} for security than for other parts of the system
    * 
    * @author PG Augnet 2, University of Paderborn
    */
   @Retention(RetentionPolicy.RUNTIME)
   @Target( { ElementType.FIELD, ElementType.PARAMETER })
   @BindingAnnotation
   public @interface Security {
   }

   @Override
   protected void configure() {

      bind(IdentityVerification.class).to(IdentityVerificationImpl.class);
      bind(SignatureAlgorithm.class).to(SignatureAlgorithmImpl.class);

      bind(Integrity.class).to(IntegrityImpl.class);

      bind(CryptoAlgorithm.class).to(CryptoAlgorithmImpl.class);
      bind(Cryptography.class).to(CryptographyImpl.class);

      bind(IdentityManager.class).to(IdentityManagerImpl.class).in(Singleton.class);
      bind(SecurityManager.class).to(SecurityManagerImpl.class);
   }

}
