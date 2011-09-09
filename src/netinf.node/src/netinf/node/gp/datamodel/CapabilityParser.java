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
package netinf.node.gp.datamodel;

import java.util.Properties;

import netinf.node.resolution.remote.gp.selector.ResolutionSelector;

/**
 * The {@link CapabilityParser} creates instances of the classes {@link Capability} and {@link Property} from strings. These
 * strings are usually defined in e.g {@link Properties}-files. The {@link CapabilityParser} parses the properties in the
 * following way:
 * <p>
 * The capability definition works as follows: 'netinf.gp.capabilities' defines a comma separated list of capabilities e.g
 * netinf.gp.capabilities = first, second, third Each capability might have arbitrary many properties (again comma separated list)
 * e.g netinf.gp.capability.first = one, two, three Each property might have arbitrary many values netinf.gp.capability.first.one
 * = value1, value2
 * <p>
 * possible values are
 * <ol>
 * <li>function: search, resolution_local, resolution_global, resolution</li>
 * <li>bandwidth: weak, medium, high</li>
 * <li>position: mobile, fixed</li>
 * </ol>
 * 
 * @see ResolutionSelector
 * @author PG Augnet 2, University of Paderborn
 */
public class CapabilityParser {

   private static final String SEPARATOR = ".";
   public static final String CAPABILITIES = "netinf.gp.capabilities";

   public Capability[] parseCapabilities(Properties properties) {
      String capabilityString = properties.getProperty(CAPABILITIES);

      if (capabilityString == null) {
         return new Capability[0];
      }

      String[] singleCapabilities = parseString(capabilityString);
      Capability[] result = new Capability[singleCapabilities.length];

      for (int i = 0; i < singleCapabilities.length; i++) {
         String singleCapability = singleCapabilities[i];

         result[i] = parseCapability(properties, singleCapability);
      }

      return result;
   }

   private Capability parseCapability(Properties properties, String name) {
      String capabilityKey = CAPABILITIES + SEPARATOR + name;

      Capability result = new Capability();
      result.setName(name);

      if (properties == null) {
         return result;
      }

      // Check existence
      String propertyString = properties.getProperty(capabilityKey);

      // Parse rest
      String[] singleProperties = parseString(propertyString);

      for (int i = 0; i < singleProperties.length; i++) {
         String singleProperty = singleProperties[i];
         String value = properties.getProperty(capabilityKey + SEPARATOR + singleProperty);

         if (value != null) {
            result.addProperty(new Property(singleProperty, value));
         }
      }

      return result;
   }

   private String[] parseString(String values) {
      String[] split = values.split(",");

      for (int i = 0; i < split.length; i++) {
         String string = split[i];
         split[i] = string.trim();
      }

      return split;
   }

}
