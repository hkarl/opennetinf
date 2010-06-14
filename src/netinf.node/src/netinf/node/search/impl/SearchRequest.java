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
package netinf.node.search.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.identity.SearchServiceIdentityObject;
import netinf.node.search.SearchController;
import netinf.node.search.SearchService;
import netinf.node.search.impl.events.RequestDoneEvent;
import netinf.node.search.impl.tasks.SendTimeoutEventTask;

/**
 * This class is used to represent a search process/request.
 * <p>
 * Among other things, it is used to collect the results of the different SearchServices for the represented search request.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class SearchRequest {

   private final Map<SearchServiceIdentityObject, Boolean> resultStatus;
   private final Set<Identifier> resultSet;
   private final int searchID;
   private final SearchController callback;
   private final SendTimeoutEventTask timeoutTask;
   private String errorMessage;

   /**
    * @param searchID
    *           ID of the search process
    * @param services
    *           the involved services
    * @param timeoutTask
    *           the SendTimeoutEventTask which belongs to this request
    * @param callback
    *           the SearchController to call on completion
    */
   public SearchRequest(final int searchID, final Set<SearchServiceIdentityObject> services,
         final SendTimeoutEventTask timeoutTask, final SearchController callback) {
      this.searchID = searchID;
      this.timeoutTask = timeoutTask;
      this.callback = callback;
      this.resultStatus = new HashMap<SearchServiceIdentityObject, Boolean>();
      for (final SearchServiceIdentityObject currentService : services) {
         this.resultStatus.put(currentService, Boolean.FALSE);
      }
      this.resultSet = new HashSet<Identifier>();
      this.errorMessage = "";
   }

   /**
    * adds the results of a specific {@link SearchService} for this search request
    * 
    * @param service
    *           results are added for this service, which is represented by its SearchServiceIdentityObject
    * @param results
    *           these results are added
    */
   public void addResults(final SearchServiceIdentityObject service, final Set<Identifier> results) {
      this.resultStatus.put(service, Boolean.TRUE);
      this.resultSet.addAll(results);
      if (isFinished()) {
         final RequestDoneEvent event = new RequestDoneEvent("REQUEST DONE", this.searchID);
         this.callback.handleSearchEvent(event);
      }
   }

   /**
    * @return the results found for this search request
    */
   public Set<Identifier> getResultSet() {
      return this.resultSet;
   }

   /**
    * @return true iff all involved SearchServices reported their results or an error
    */
   boolean isFinished() {
      boolean allFinished = true;
      for (final Boolean b : this.resultStatus.values()) {
         allFinished &= b.booleanValue();
      }
      return allFinished;
   }

   /**
    * @return SendTimeoutEventTask of this search request
    */
   public SendTimeoutEventTask getTimeoutTask() {
      return this.timeoutTask;
   }

   /**
    * @param error
    *           the error message to add
    */
   public void addToErrorMessage(final String error) {
      errorMessage += error;
   }

   /**
    * @return the errors reported by the involved SearchServices. if length == 0, no error was recorded
    */
   public String getErrorMessage() {
      return errorMessage;
   }

}
