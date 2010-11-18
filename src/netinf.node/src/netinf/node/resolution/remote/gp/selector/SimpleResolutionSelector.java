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
package netinf.node.resolution.remote.gp.selector;

import java.util.List;

import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.node.gp.datamodel.Capability;
import netinf.node.gp.datamodel.Property;
import netinf.node.gp.datamodel.Resolution;

import org.apache.log4j.Logger;

/**
 * The {@link SimpleResolutionSelector} simply select always the first possible resolution.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class SimpleResolutionSelector implements ResolutionSelector {

   private static final Logger LOG = Logger.getLogger(SimpleResolutionSelector.class);

   private final Property resolutionGlobal = new Property(PROPERTY_FUNCTION_NAME, PROPERTY_FUNCTION_VALUE_RESOLUTION_GLOBAL);
   private final Property resolution = new Property(PROPERTY_FUNCTION_NAME, PROPERTY_FUNCTION_VALUE_RESOLUTION);

   @Override
   public Resolution getResolutionForDelete(List<Resolution> resolutions) {
      LOG.trace(null);
      if (resolutions != null && resolutions.size() > 0) {
         return resolutions.get(0);
      } else {
         throw new NetInfUncheckedException("Could not determine appropriate resolution");
      }
   }

   /**
    * Tries to use resolution_global, if not found "resolution" is used. Finally an arbitrary resolution is returned.
    * 
    * @see netinf.node.resolution.remote.gp.selector.ResolutionSelector#getResolutionForGet(java.util.List)
    */
   @Override
   public Resolution getResolutionForGet(List<Resolution> resolutions) {
      LOG.trace(null);

      Resolution result = getResolution(resolutions, this.resolutionGlobal);
      if (result != null) {
         LOG.log(DemoLevel.DEMO, "(NODE ) Using global resolution");
         return result;
      } else {
         LOG.log(DemoLevel.DEMO, "(NODE ) No global resolution available");
         LOG.debug("resolution_global not available");
      }

      result = getResolution(resolutions, this.resolution);
      if (result != null) {
         LOG.log(DemoLevel.DEMO, "(NODE ) Using local resolution");
         return result;
      } else {
         LOG.log(DemoLevel.DEMO, "(NODE ) No local resolution available");
         LOG.debug("resolution not available");
      }

      if (resolutions != null && resolutions.size() > 0) {
         return resolutions.get(0);
      } else {
         throw new NetInfUncheckedException("Could not determine appropriate resolution");
      }
   }

   @Override
   public Resolution getResolutionForPut(List<Resolution> resolutions) {
      LOG.trace(null);
      if (resolutions != null && resolutions.size() > 0) {
         return resolutions.get(0);
      } else {
         throw new NetInfUncheckedException("Could not determine appropriate resolution");
      }
   }

   private Resolution getResolution(List<Resolution> resolutions, Property searchedProperty) {
      // LOG.log(DemoLevel.DEMO, "(REMOVE ) Searching in " + resolutions + " for " + searchedProperty);

      if (resolutions != null) {
         for (Resolution resolution : resolutions) {
            List<Capability> capabilities = resolution.getCapabilities();
            for (Capability capability : capabilities) {
               List<Property> properties = capability.getProperties();

               for (Property property : properties) {
                  if (property.getName().equals(searchedProperty.getName())
                        && property.getValue().contains(searchedProperty.getValue())) {
                     return resolution;
                  }
               }
            }
         }
      }
      return null;
   }
}
