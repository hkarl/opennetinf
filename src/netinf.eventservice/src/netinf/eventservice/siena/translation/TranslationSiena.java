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
package netinf.eventservice.siena.translation;

import netinf.common.datamodel.InformationObject;
import netinf.common.messages.ESFEventMessage;
import siena.Notification;

/**
 * Every {@link ESFEventMessage} contains two kinds of {@link InformationObject} - the old one and the new one. The translation in
 * the case of Siena works as follows. Since Siena supports only name-value pairs, we use every predicate (a attribute-uri) as a
 * name, and every object as the value of the name-value pair. In the case of of objects being resources without a value, we skip
 * the according translation of the attribute.
 * 
 * Recognize that this translation is not lossless. We lose the information about the position of a predicate in the tree (we are
 * not taking the subject into account while translating). Additionally, we cannot handle {@link InformationObject} which do use
 * the same attribute-uri more than once. In this case the first object for the attribute-uri is used, and consecutive are
 * ignored. Although this are pretty large restrictions, we cannot do that in another way.
 * 
 * Finally, in order to retrieve the initial {@link InformationObject}s, we add two fields to each {@link Notification} which
 * contain the serialized form of the old and the new {@link InformationObject}.
 * 
 * @author PG Augnet 2, University of Paderborn
 * 
 */
public final class TranslationSiena {

   public static final String PREFIX_OLD = "old";
   public static final String PREFIX_NEW = "new";
   public static final String PREFIX_DIFF = "diff";
   public static final String PREFIX_DIFF_DETAILS = "diff_details";
   public static final String PREFIX_OLD_WILDCARD = "old_recursive";
   public static final String PREFIX_NEW_WILDCARD = "new_recursive";

   /**
    * This name is used to identify the serialization format of the according information object.
    */
   public static final String FORMAT = "format";

   /**
    * Is used to indicate all the translations, which describe some sub path of from an information object to a particular
    * attribute. This sign is NOT used in all the translated path that start directly at the information object.
    * 
    * E.g. oldIO->uri1->attribute1->uri2->propert2 would be translated to (for attribute2) old|*|uri2 and old|uri1|uri2, and to
    * (for attribute1) old|uri1, indicating that uri1 and the path from uri1|uri2 are direct descendants of the IO.
    * 
    */
   public static final String WILDCARD = "*";

   public static final String SEPARATOR = "|";

   public static final String STATUS_CREATED = "CREATED";
   public static final String STATUS_DELETED = "DELETED";
   public static final String STATUS_CHANGED = "CHANGED";
   public static final String STATUS_UNCHANGED = "UNCHANGED";

   public static final String STATUS_CHANGED_LT = "<";
   public static final String STATUS_CHANGED_GT = ">";

}
