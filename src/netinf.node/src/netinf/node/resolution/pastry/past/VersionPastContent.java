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
package netinf.node.resolution.pastry.past;

import java.util.LinkedList;
import java.util.List;

import netinf.common.datamodel.Identifier;
import rice.p2p.commonapi.Id;
import rice.p2p.past.ContentHashPastContentHandle;
import rice.p2p.past.Past;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastContentHandle;
import rice.p2p.past.PastException;

/**
 * This class is used to store and retrieve versioned identifiers of an IO.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class VersionPastContent implements PastContent {

	private static final long serialVersionUID = 2224864664163318409L;

	private List<Identifier> identifiers;
	private Id id;

	public VersionPastContent(Identifier identifier, Id id) {
		this.identifiers = new LinkedList<Identifier>();
		this.identifiers.add(identifier);
		this.id = id;
	}


	@Override
	public PastContent checkInsert(Id id, PastContent existingContent) throws PastException {
		if (existingContent == null) {
			return this;
		}
		if (existingContent instanceof VersionPastContent) {
			VersionPastContent versionPastContent = (VersionPastContent) existingContent;
			versionPastContent.appendIdentifier(identifiers.get(0));
		}

		return existingContent;
	}

	@Override
	public PastContentHandle getHandle(Past local) {
		return new ContentHashPastContentHandle(local.getLocalNodeHandle(), getId());
	}

	@Override
	public Id getId() {
		return id;
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	public void appendIdentifier(Identifier identifier) {
		identifiers.add(identifier);
	}

	public List<Identifier> getIdentifiers() {
		return identifiers;
	}

}
