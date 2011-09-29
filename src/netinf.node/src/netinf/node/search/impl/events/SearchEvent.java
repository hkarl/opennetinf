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
package netinf.node.search.impl.events;

import netinf.node.search.SearchController;

/**
 * This class represents a general search event. Search events are always sent to a {@link SearchController} by calling its
 * {@link SearchController#handleSearchEvent(SearchEvent)} method.
 * <p>
 * Subclasses should be used if they apply
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class SearchEvent {

   private final String description;
   private final int searchID;

   /**
    * creates a new generic search event
    * 
    * @param description
    *           description, null or empty values will be replaced by 'no description'
    * @param searchID
    *           tells the receiver which search process is meant
    */
   public SearchEvent(final String description, final int searchID) {
      this.description = description == null || "".equals(description) ? "no description" : description;
      this.searchID = searchID;
   }

   /**
    * @return description string as saved in this SearchEvent
    */
   public String getDescription() {
      return this.description;
   }

   /**
    * @return search ID as saved in this SearchEvent
    */
   public int getSearchID() {
      return this.searchID;
   }

}
