/*
 * Copyright (C) 2009,2010 University of Paderborn, Computer Networks Group
 * Copyright (C) 2009,2010 Christian Dannewitz <christian.dannewitz@upb.de>
 * Copyright (C) 2009,2010 Thorsten Biermann <thorsten.biermann@upb.de>
 * 
 * Copyright (C) 2009,2010 Eduard Bauer <edebauer@mail.upb.de>
 * Copyright (C) 2009,2010 Matthias Becker <matzeb@mail.upb.de>
 * Copyright (C) 2009,2010 Frederic Beister <azamir@zitmail.uni-paderborn.de>
 * Copyright (C) 2009,2010 Nadine Dertmann <ndertmann@gmx.de>
 * Copyright (C) 2009,2010 Michael Kionka <mkionka@mail.upb.de>
 * Copyright (C) 2009,2010 Mario Mohr <mmohr@mail.upb.de>
 * Copyright (C) 2009,2010 Felix Steffen <felix.steffen@gmx.de>
 * Copyright (C) 2009,2010 Sebastian Stey <sebstey@mail.upb.de>
 * Copyright (C) 2009,2010 Steffen Weber <stwe@mail.upb.de>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.common.datamodel.impl.identity;

import java.security.PublicKey;

import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;
import netinf.common.datamodel.impl.InformationObjectImpl;
import netinf.common.utils.Utils;

import com.google.inject.Inject;

/**
 * Simple Java-based (especially serialization) representation of {@link IdentityObject}s
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IdentityObjectImpl extends InformationObjectImpl implements IdentityObject {

   private static final long serialVersionUID = -1219593339149490983L;

   @Inject
   public IdentityObjectImpl(DatamodelFactoryImpl datamodelFactory) {
      super(datamodelFactory);
   }

   @Override
   public Attribute getPublicKeys() {
      return getSingleAttribute(DefinedAttributeIdentification.PUBLIC_KEY.getURI());
   }

   @Override
   public void setPublicKeys(Attribute attribute) {
      resetAttribute(DefinedAttributeIdentification.PUBLIC_KEY.getURI(), attribute);
   }

   @Override
   public PublicKey getPublicMasterKey() {
      return Utils.stringToPublicKey(getSingleAttribute(DefinedAttributeIdentification.PUBLIC_KEY.getURI()).getValue(
            String.class));
   }

   @Override
   public void setPublicMasterKey(PublicKey publicKey) {
      resetAttribute(DefinedAttributeIdentification.PUBLIC_KEY.getURI(), Utils.objectToString(publicKey),
            DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
   }

   @Override
   public String describe() {
      StringBuffer buf = new StringBuffer("a (general) Identity Object that ");
      buf.append(getIdentifier().describe());
      return buf.toString();
   }
}
