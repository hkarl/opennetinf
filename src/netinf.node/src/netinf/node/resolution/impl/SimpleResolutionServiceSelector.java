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
package netinf.node.resolution.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.node.resolution.ResolutionServiceSelector;

import org.apache.log4j.Logger;

/**
 * The Class SimpleResolutionServiceSelector.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class SimpleResolutionServiceSelector implements ResolutionServiceSelector {

   private static final Logger LOG = Logger.getLogger(SimpleResolutionServiceSelector.class);

   private final List<ResolutionServiceIdentityObject> resolutionServices = new ArrayList<ResolutionServiceIdentityObject>();

   @Override
   public void addResolutionService(ResolutionServiceIdentityObject resolutionServiceInformation) {
      resolutionServices.add(resolutionServiceInformation);
      sortResolutionServices();
   }

   private void sortResolutionServices() {
      Collections.sort(resolutionServices, new Comparator<ResolutionServiceIdentityObject>() {

         @Override
         public int compare(ResolutionServiceIdentityObject o1, ResolutionServiceIdentityObject o2) {
            if (o1 != null && o2 != null) {
               return o2.getDefaultPriority().compareTo(o1.getDefaultPriority());
            } else {
               LOG.trace("Compared resolutionServices which ResolutionServiceIdentityObjects were null. "
                     + "This is not allowed to happen");
               return 0;
            }
         }
      });
   }

   @Override
   public List<ResolutionServiceIdentityObject> getRSForDelete() {
      return new ArrayList<ResolutionServiceIdentityObject>(resolutionServices);
   }

   @Override
   public List<ResolutionServiceIdentityObject> getRSForGet() {
      return new ArrayList<ResolutionServiceIdentityObject>(resolutionServices);
   }

   @Override
   public List<ResolutionServiceIdentityObject> getRSForPut() {
      return new ArrayList<ResolutionServiceIdentityObject>(resolutionServices);
   }

   @Override
   public void removeResolutionService(ResolutionServiceIdentityObject resolutionServiceInformation) {
      resolutionServices.remove(resolutionServiceInformation);
   }
}
