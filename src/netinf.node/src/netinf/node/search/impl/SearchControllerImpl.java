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
package netinf.node.search.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.Vector;
import java.util.Map.Entry;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.identity.SearchServiceIdentityObject;
import netinf.common.exceptions.NetInfSearchException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.messages.NetInfMessage;
import netinf.common.messages.SCGetByQueryTemplateRequest;
import netinf.common.messages.SCGetBySPARQLRequest;
import netinf.common.messages.SCGetTimeoutAndNewSearchIDRequest;
import netinf.common.messages.SCGetTimeoutAndNewSearchIDResponse;
import netinf.common.messages.SCSearchResponse;
import netinf.node.search.SearchController;
import netinf.node.search.SearchService;
import netinf.node.search.impl.events.RequestDoneEvent;
import netinf.node.search.impl.events.RequestTimeoutEvent;
import netinf.node.search.impl.events.SearchEvent;
import netinf.node.search.impl.events.SearchServiceErrorEvent;
import netinf.node.search.impl.events.SearchServiceResultEvent;
import netinf.node.search.impl.tasks.SendTimeoutEventTask;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * This search controller is a sample implementation for the {@link SearchController} interface.
 * <p>
 * The managed {@link SearchService}s work asynchronously.
 * 
 * @author PG Augnet 2, University of Paderborn
 * @see SearchController
 */
public class SearchControllerImpl implements SearchController {
   private static final Logger LOG = Logger.getLogger(SearchControllerImpl.class);

   private int timeout;

   private final Map<SearchServiceIdentityObject, SearchService> registeredSearchServices;
   private final Map<Integer, SearchRequest> requests;
   private final Map<Integer, Integer> openSearchIDs;

   private final Timer timeoutTimer;

   private int nextSID;

   private ArrayList<Class<? extends NetInfMessage>> supportedOperations;

   @Override
   public List<Class<? extends NetInfMessage>> getSupportedOperations() {
      LOG.trace(null);

      if (supportedOperations == null) {
         supportedOperations = new ArrayList<Class<? extends NetInfMessage>>();
         supportedOperations.add(SCGetByQueryTemplateRequest.class);
         supportedOperations.add(SCGetBySPARQLRequest.class);
         supportedOperations.add(SCGetTimeoutAndNewSearchIDRequest.class);
      }

      return supportedOperations;
   }

   @Override
   public NetInfMessage processNetInfMessage(final NetInfMessage netInfMessage) {
      LOG.trace(null);

      if (netInfMessage instanceof SCGetByQueryTemplateRequest) {
         final SCGetByQueryTemplateRequest getByQueryTemplateRequest = (SCGetByQueryTemplateRequest) netInfMessage;
         return processSCGetByQueryTemplateRequest(getByQueryTemplateRequest);
      }
      if (netInfMessage instanceof SCGetBySPARQLRequest) {
         final SCGetBySPARQLRequest getBySPARQLRequest = (SCGetBySPARQLRequest) netInfMessage;
         return processSCGetBySPARQLRequest(getBySPARQLRequest);
      }
      if (netInfMessage instanceof SCGetTimeoutAndNewSearchIDRequest) {
         final SCGetTimeoutAndNewSearchIDRequest getNewSearchIDRequest = (SCGetTimeoutAndNewSearchIDRequest) netInfMessage;
         return processSCGetTimeoutAndNewSearchIDRequest(getNewSearchIDRequest);
      }
      return null;
   }

   private NetInfMessage processSCGetByQueryTemplateRequest(final SCGetByQueryTemplateRequest getByQueryTemplateRequest) {
      LOG.trace(null);
      final SCSearchResponse returnMessage = new SCSearchResponse();

      try {
         Set<Identifier> identifiers = getByQueryTemplate(getByQueryTemplateRequest.getType(),
               getByQueryTemplateRequest.getParameters(), getByQueryTemplateRequest.getSearchID());
         for (Identifier identifier : identifiers) {
            returnMessage.addResultIdentifier(identifier);
         }
      } catch (NetInfUncheckedException e) {
         LOG.error("Could not process search request with searchID " + getByQueryTemplateRequest.getSearchID() + ": "
               + e.getMessage());
         returnMessage.setErrorMessage("Could not process search request with searchID '"
               + getByQueryTemplateRequest.getSearchID() + "': " + e.getMessage());
      }

      return returnMessage;
   }

   private NetInfMessage processSCGetBySPARQLRequest(final SCGetBySPARQLRequest getBySPARQLRequest) {
      LOG.trace(null);
      final SCSearchResponse returnMessage = new SCSearchResponse();

      try {
         Set<Identifier> identifiers = getBySPARQL(getBySPARQLRequest.getRequest(), getBySPARQLRequest.getSearchID());
         for (Identifier identifier : identifiers) {
            returnMessage.addResultIdentifier(identifier);
         }
      } catch (NetInfUncheckedException e) {
         LOG.error("Could not process search request with searchID " + getBySPARQLRequest.getSearchID() + ": " + e.getMessage());
         returnMessage.setErrorMessage("Could not process search request with searchID '" + getBySPARQLRequest.getSearchID()
               + "': " + e.getMessage());
      }

      return returnMessage;
   }

   private NetInfMessage processSCGetTimeoutAndNewSearchIDRequest(
         final SCGetTimeoutAndNewSearchIDRequest getTimeoutAndNewSearchIDRequest) {
      LOG.trace(null);
      final int[] result = getTimeoutAndNewSearchID(getTimeoutAndNewSearchIDRequest.getDesiredTimeout());
      return new SCGetTimeoutAndNewSearchIDResponse(result[0], result[1]);
   }

   @Inject
   public SearchControllerImpl() {
      LOG.trace(null);
      // This is the default timeout
      timeout = 10000;

      this.registeredSearchServices = new HashMap<SearchServiceIdentityObject, SearchService>();
      this.requests = new HashMap<Integer, SearchRequest>();
      this.openSearchIDs = new HashMap<Integer, Integer>();
      this.timeoutTimer = new Timer();
      this.nextSID = 0;
   }

   @Inject(optional = true)
   public void setSearchTimeout(@Named("search_timeout") final int timeout) {
      this.timeout = timeout;
   }

   @Override
   public Set<Identifier> getByQueryTemplate(final String type, final List<String> parameters, final int searchID) {
      LOG.trace(null);
      Vector<Object> input = new Vector<Object>();
      input.add(0); // as indicator for getByQueryTemplate
      input.add(searchID);
      input.add(type);
      input.add(parameters);
      return processSearchRequest(input);
   }

   @Override
   public Set<Identifier> getBySPARQL(final String request, final int searchID) {
      LOG.trace(null);
      Vector<Object> input = new Vector<Object>();
      input.add(1); // as indicator for getBySparql
      input.add(searchID);
      input.add(request);
      return processSearchRequest(input);
   }

   /**
    * internal method for the processing of search requests
    * 
    * @param input
    *           the request type and all needed parameters
    * @return the set of Identifiers which matched the search
    */
   private Set<Identifier> processSearchRequest(final Vector<Object> input) {
      LOG.trace(null);
      final int searchID = (Integer) input.get(1);
      int usedTimeout;

      // Do this only if there was an ID issued
      if (this.openSearchIDs.containsKey(Integer.valueOf(searchID))) {
         usedTimeout = this.openSearchIDs.get(Integer.valueOf(searchID));
         this.openSearchIDs.remove(Integer.valueOf(searchID));
         final Map<SearchServiceIdentityObject, SearchService> activeServicesForThisRun = getActiveServices();
         // search is possible iff active services exist
         if (!activeServicesForThisRun.isEmpty()) {

            LOG.log(DemoLevel.DEMO, "(NODE ) Performing search...");

            final SendTimeoutEventTask timeoutTask = new SendTimeoutEventTask(this, searchID);

            final SearchRequest newRequest = new SearchRequest(searchID, activeServicesForThisRun.keySet(), timeoutTask, this);
            this.requests.put(Integer.valueOf(searchID), newRequest);

            // start a timer
            LOG.debug("Set timeout timer to " + usedTimeout + " ms");
            this.timeoutTimer.schedule(timeoutTask, usedTimeout);

            int i = 1;
            for (final Entry<SearchServiceIdentityObject, SearchService> serviceEntry : activeServicesForThisRun.entrySet()) {
               // start each search service in an own thread
               LOG.debug("Start search service " + serviceEntry.getKey().getName() + " for search " + searchID
                     + " in a new thread");
               new Thread(new Runnable() {
                  @Override
                  public void run() {
                     switch (((Integer) input.get(0)).intValue()) {
                     case 0:
                        serviceEntry.getValue().getByQueryTemplate((String) input.get(2), (List<String>) input.get(3), searchID,
                              serviceEntry.getKey(), SearchControllerImpl.this);
                        break;
                     case 1:
                        serviceEntry.getValue().getBySPARQL((String) input.get(2), searchID, serviceEntry.getKey(),
                              SearchControllerImpl.this);
                        break;
                     default:
                        LOG.warn("Wrong method indicator");
                     }

                  }
               }, "Search_sID=" + searchID + "_Service=" + i).start();
               i++;
            }

            // wait till request is performed or timeout is reached
            Set<Identifier> result = null;
            synchronized (newRequest) {
               if (!newRequest.isFinished()) {
                  try {
                     LOG.debug("Will wait until i get informed about completion of search request " + searchID);
                     newRequest.wait();
                     LOG.debug("Got informed that search request " + searchID
                           + " is finished. Now will return result to my caller");
                  } catch (InterruptedException e) {
                     LOG.warn("Interrupt while waiting for the completion of a search request");
                  }
               }

               // search finished => process results
               result = newRequest.getResultSet();
               if (result.isEmpty() && newRequest.getErrorMessage().length() != 0) {
                  this.requests.remove(Integer.valueOf(searchID));
                  throw new NetInfSearchException(newRequest.getErrorMessage());
               }
               this.requests.remove(Integer.valueOf(searchID));
            }

            LOG.log(DemoLevel.DEMO, "(NODE ) ... found " + (result.size() == 1 ? "1 result" : result.size() + " results"));

            return result;
         } else {
            LOG.error("No search possible: No active search services");
            throw new NetInfSearchException("no active search services");
         }

      } else {
         throw new NetInfSearchException("No search with ID " + searchID + " active");
      }
   }

   @Override
   public void handleSearchEvent(final SearchEvent event) {
      LOG.trace(null);
      LOG.debug("Event type: " + event.getClass().toString());
      if (event instanceof RequestDoneEvent || event instanceof RequestTimeoutEvent) {
         // Something happened that tells us to deliver some results
         final SearchRequest correspondingRequest = this.requests.get(Integer.valueOf(event.getSearchID()));
         if (correspondingRequest != null) {
            try {
               if (event instanceof RequestDoneEvent) {
                  correspondingRequest.getTimeoutTask().cancel();
               }

               // signalize that search request is finished
               synchronized (correspondingRequest) {
                  LOG.debug("Signalizing that search request " + event.getSearchID() + " is finished");
                  correspondingRequest.notifyAll();
               }
            } catch (NullPointerException e) {
               if (event instanceof RequestDoneEvent) {
                  LOG.debug("RequestDoneEvent too late. "
                        + "SearchRequest was already processed and deleted because of RequestTimeoutEvent");
               } else {
                  LOG.debug("RequestTimeoutEvent too late. "
                        + "SearchRequest was already processed and deleted because of RequestDoneEvent");
               }
            }
         }
      }
      if (event instanceof SearchServiceResultEvent) {
         // Some SearchService has results for us
         final SearchServiceResultEvent castedEvent = (SearchServiceResultEvent) event;
         final SearchRequest correspondingRequest = this.requests.get(Integer.valueOf(event.getSearchID()));
         try {
            synchronized (correspondingRequest) {
               if (correspondingRequest == null) {
                  // Request object already gone
                  LOG.debug("Service too late: searchID:" + castedEvent.getSearchID() + ", service:"
                        + castedEvent.getSearchServiceIdO().getName());
               } else {
                  final SearchServiceIdentityObject correspondingService = castedEvent.getSearchServiceIdO();
                  final Set<Identifier> correspondingResultSet = castedEvent.getResultSet();
                  correspondingRequest.addResults(correspondingService, correspondingResultSet);
                  LOG.debug("searchID:" + castedEvent.getSearchID() + ", service:" + castedEvent.getSearchServiceIdO().getName()
                        + ": added own result to overall result");
               }
            }
         } catch (NullPointerException e) {
            LOG.debug("Service too late: searchID:" + castedEvent.getSearchID() + ", service:"
                  + castedEvent.getSearchServiceIdO().getName());
         }
      }
      if (event instanceof SearchServiceErrorEvent) {
         // a search service reports an error: it will not provide any result => it is finished
         final SearchServiceErrorEvent castedEvent = (SearchServiceErrorEvent) event;
         final SearchRequest correspondingRequest = this.requests.get(Integer.valueOf(castedEvent.getSearchID()));
         try {
            synchronized (correspondingRequest) {
               if (correspondingRequest == null) {
                  // Request object already gone
                  LOG.debug("Service, reporting error, is too late anyway: searchID:" + castedEvent.getSearchID() + ", service:"
                        + castedEvent.getSearchServiceIdO().getName());
               } else {
                  // set service as finished
                  final SearchServiceIdentityObject correspondingService = castedEvent.getSearchServiceIdO();
                  correspondingRequest.addResults(correspondingService, new HashSet<Identifier>());
                  // add error message
                  if (correspondingRequest.getErrorMessage().length() == 0) {
                     correspondingRequest.addToErrorMessage(correspondingService.getName() + ": " + castedEvent.getDescription());
                  } else {
                     correspondingRequest.addToErrorMessage("; " + correspondingService.getName() + ": "
                           + castedEvent.getDescription());
                  }
               }
            }
         } catch (NullPointerException e) {
            LOG.debug("Service, reporting error, is too late anyway: searchID:" + castedEvent.getSearchID() + ", service:"
                  + castedEvent.getSearchServiceIdO().getName());
         }
      }
   }

   /**
    * check which services are ready
    * 
    * @return a mapping from {@link SearchServiceIdentityObject} to {@link SearchService}
    */
   private Map<SearchServiceIdentityObject, SearchService> getActiveServices() {
      LOG.trace(null);

      final HashMap<SearchServiceIdentityObject, SearchService> activeServices = new HashMap<SearchServiceIdentityObject, SearchService>();

      // check for active SearchServices and add them
      for (SearchService searchService : registeredSearchServices.values()) {
         if (searchService.isReady()) {
            if (activeServices.containsKey(searchService.getIdentityObject())) {
               throw new NetInfSearchException("Multiple definitions of SearchService "
                     + searchService.getIdentityObject().getName());
            } else {
               activeServices.put(searchService.getIdentityObject(), searchService);
            }
         }
      }
      return activeServices;
   }

   @Override
   public int[] getTimeoutAndNewSearchID(final int desiredTimeout) {
      LOG.trace(null);
      // TODO find a way to allow only receiver of the ID to start a search
      // TODO prevent DOS-attack by restricting callers

      int[] returnValues = new int[2];

      // use the smaller timeout value
      int usedTimeout;
      if (desiredTimeout < this.timeout) {
         usedTimeout = desiredTimeout;
      } else {
         usedTimeout = this.timeout;
      }
      returnValues[0] = usedTimeout;
      LOG.debug("Determine timeout: controllerTimeout = " + this.timeout + ", desiredTimeout= " + desiredTimeout
            + " => timeout will be " + usedTimeout + " ms");

      synchronized (this) {
         final int searchID = this.nextSID;
         this.nextSID++;
         this.openSearchIDs.put(Integer.valueOf(searchID), usedTimeout);
         returnValues[1] = searchID;
      }
      LOG.debug("Assigned search ID: " + returnValues[1]);

      return returnValues;
   }

   @Override
   public void addSearchService(final SearchService searchService) {
      LOG.trace(null);
      registeredSearchServices.put(searchService.getIdentityObject(), searchService);
   }

   @Override
   public void removeSearchService(final SearchService searchService) {
      LOG.trace(null);
      registeredSearchServices.remove(searchService.getIdentityObject());
   }

}
