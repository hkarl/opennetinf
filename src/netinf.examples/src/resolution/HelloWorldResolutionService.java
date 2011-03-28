/*
 * Copyright (C) 2009,2010 University of Paderborn, Computer Networks Group
 * Copyright (C) 2009,2010 Christian Dannewitz <christian.dannewitz@upb.de>
 * Copyright (C) 2009,2010 Thorsten Biermann <thorsten.biermann@upb.de>
 * 
 * Copyright (C) 2009,2010 Eduard Bauer <edebauer@mail.upb.de>
 * Copyright (C) 2009,2010 Matthias Becker <matzeb@mail.upb.de>
 * Copyright (C) 2009,2010 Frederic Beister <azamir@zitmail.uni-paderborn.de>
 * Copyright (C) 2009,2010 Nadine Dertmann <ndertmann@gmx.de>
 * Copyright (C) 2009,2010 Martin Dräxler <draexler@mail.uni-paderborn.de>
 * Copyright (C) 2009,2010 Sebastian Dröge <slomo@upb.de>
 * Copyright (C) 2009,2010 Michael Kionka <mkionka@mail.upb.de>
 * Copyright (C) 2009,2010 Mario Mohr <mmohr@mail.upb.de>
 * Copyright (C) 2009,2010 Carsten Rösnick <roesnick@mail.upb.de>
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
package resolution;

import java.util.ArrayList;
import java.util.List;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.node.resolution.AbstractResolutionService;
import netinf.node.resolution.ResolutionService;

import com.google.inject.Inject;

/**
 * @author PG Augnet 2, University of Paderborns
 */
public class HelloWorldResolutionService extends AbstractResolutionService implements ResolutionService {

   private final DatamodelFactory datamodelFactory;
   private final InformationObject helloWorldIO;

   @Inject
   public HelloWorldResolutionService(DatamodelFactory datamodelFactory) {
      this.datamodelFactory = datamodelFactory;
      helloWorldIO = datamodelFactory.createInformationObject();
      Identifier identifier = datamodelFactory.createIdentifier();
      IdentifierLabel identifierLabel = datamodelFactory.createIdentifierLabel();
      identifierLabel.setLabelName(DefinedLabelName.UNIQUE_LABEL.getLabelName());
      identifierLabel.setLabelValue("Hello World");
      identifier.addIdentifierLabel(identifierLabel);
      helloWorldIO.setIdentifier(identifier);
   }

   @Override
   protected ResolutionServiceIdentityObject createIdentityObject() {
      ResolutionServiceIdentityObject identity = this.datamodelFactory
            .createDatamodelObject(ResolutionServiceIdentityObject.class);
      identity.setName("HelloWorldResolutionService");
      identity.setDefaultPriority(1000);
      identity.setDescription("This is a Hello World resolution service");
      return identity;
   }

   @Override
   public void delete(Identifier identifier) {
      // There is nothing to delete
   }

   @Override
   public String describe() {
      return "always returning a Hello World IO";
   }

   @Override
   public InformationObject get(Identifier identifier) {
      return helloWorldIO;
   }

   @Override
   public List<Identifier> getAllVersions(Identifier identifier) {
      List<Identifier> list = new ArrayList<Identifier>();
      list.add(helloWorldIO.getIdentifier());
      return list;
   }

   @Override
   public void put(InformationObject informationObject) {
      // This ResolutionService does not support push

   }

}
