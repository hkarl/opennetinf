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
package netinf.node.resolution.locator.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.node.resolution.ResolutionInterceptor;
import netinf.node.resolution.locator.LocatorCostProvider;

import com.google.inject.Inject;

/**
 * The Class LocatorSelectorImpl.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class LocatorSelectorImpl implements ResolutionInterceptor {

   private LocatorCostProvider costProvider;

   private final HashMap<Attribute, Double> costs = new HashMap<Attribute, Double>();

   @Inject
   public void setCostProvider(LocatorCostProvider costProvider) {
      this.costProvider = costProvider;
   }

   @Override
   public InformationObject interceptGet(InformationObject io) {
      List<Attribute> locators = io.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.getAttributePurpose());

      Collections.sort(locators, new Comparator<Attribute>() {

         @Override
         public int compare(Attribute o1, Attribute o2) {
            return getCost(o1).compareTo(getCost(o2));
         }
      });
      for (Attribute locator : locators) {
         io.removeAttribute(locator);
      }
      for (int i = 0; i < locators.size(); i++) {
         io.addAttribute(locators.get(i));

      }
      return io;
   }

   private Double getCost(Attribute locator) {
      if (!costs.containsKey(locator)) {
         costs.put(locator, costProvider.getCost(locator));
      }
      return costs.get(locator);
   }

}
