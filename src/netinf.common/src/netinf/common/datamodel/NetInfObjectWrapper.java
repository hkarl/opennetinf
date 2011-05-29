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
package netinf.common.datamodel;

import netinf.common.datamodel.attribute.Attribute;

/**
 * The top-most class of the datamodel. Every wrapped object can directly be accessed, serialized, and cloned. Accordingly it is
 * necessary to have this abstraction for all classes of the datamodel.
 * <p>
 * The wrapped object is the "real" data holder. By this, it is possible for e.g. to use a database, rdf-representation, or any
 * other backend storage for the data. The advantage is the uniform access to all these storage possibilities.
 * <p>
 * Every class, inhereting from this class can be serialized seperately.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface NetInfObjectWrapper {

   /**
    * @return the wrapped object. In case this object does not wrap another object, the actual object is returned.
    */
   Object getWrappedObject();

   /**
    * This is the byte representation of the information belonging to this {@link NetInfObjectWrapper}. In case of simple java
    * classes, this returns simply the serialized objects. In case of RDF, this are the bytes representing the rdf-string (xml,
    * turtle, or what so ever).
    * 
    * @return byte-array that represents this object completely.
    */
   byte[] serializeToBytes();

   /**
    * If an object is cloned, it is not bound to its parent. E.g. in case of a cloned {@link Attribute} this implies that,
    * whenever only a single {@link Attribute} is cloned, the cloned {@link Attribute} is not bound to any
    * {@link InformationObject} or {@link Attribute}.
    * 
    * @return
    */
   Object clone();

   /**
    * If an object has a parent (in case of {@link InformationObject} or {@link Attribute}, the parent is NOT taken into account
    * during the calculation of the hashCode.
    * 
    * @return
    */
   int hashCode();

   /**
    * If an object has a parent (in case of {@link InformationObject} or {@link Attribute}, the parent is NOT taken into account
    * while equals is computed. Additionally, always the sorted lists of {@link Attribute}s (in case of {@link InformationObject},
    * or {@link Attribute} are compared.
    * 
    * @param obj
    * @return
    */
   boolean equals(Object obj);

   /**
    * @return the datamodelFactory, which created the according objects.
    */
   DatamodelFactory getDatamodelFactory();

}
