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
package netinf.node.resolution.remote.gp.selector;

import java.util.List;

import netinf.node.gp.datamodel.Resolution;
import netinf.node.resolution.ResolutionService;

/**
 * A {@link ResolutionSelector} determines which {@link Resolution} to choose from several possible resolutions for different
 * operations (get, put, delete). After a suitable {@link Resolution} was chosen, it should be used to perform the according
 * operation.
 * <p>
 * This interface additionally contains typical names and values concerning the resolution.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface ResolutionSelector {

   // Some predefined properties and their values.
   String PROPERTY_FUNCTION_NAME = "function";
   String PROPERTY_FUNCTION_VALUE_RESOLUTION = "resolution";
   String PROPERTY_FUNCTION_VALUE_RESOLUTION_GLOBAL = "resolution_global";
   String PROPERTY_FUNCTION_VALUE_RESOLUTION_LOCAL = "resolution_local";

   String PROPERTY_BANDWIDTH_NAME = "bandwidth";
   String PROPERTY_BANDWIDTH_VALUE_WEAK = "weak";
   String PROPERTY_BANDWIDTH_VALUE_MEDIUM = "medium";
   String PROPERTY_BANDWIDTH_VALUE_HIGH = "high";

   String PROPERTY_POSITION_NAME = "position";
   String PROPERTY_POSITION_VALUE_MOBILE = "mobile";
   String PROPERTY_POSITION_VALUE_FIXED = "fixed";

   /**
    * Selects an appropriate {@link Resolution} of the given list of {@link Resolution} for performing the following two kinds of
    * requests {@link ResolutionService#get(netinf.common.datamodel.Identifier)} and
    * {@link ResolutionService#getAllVersions(netinf.common.datamodel.Identifier)}.
    * 
    * @param resolutions
    * @return
    */
   Resolution getResolutionForGet(List<Resolution> resolutions);

   /**
    * Selects an appropriate {@link Resolution} of the given list of {@link Resolution} for performing the following request
    * requests {@link ResolutionService#put(netinf.common.datamodel.InformationObject)}
    * 
    * @param resolutions
    * @return
    */
   Resolution getResolutionForPut(List<Resolution> resolutions);

   /**
    * Selects an appropriate {@link Resolution} of the given list of {@link Resolution} for performing the following request
    * requests {@link ResolutionService#delete(netinf.common.datamodel.Identifier)}
    * 
    * @param resolutions
    * @return
    */
   Resolution getResolutionForDelete(List<Resolution> resolutions);
}
