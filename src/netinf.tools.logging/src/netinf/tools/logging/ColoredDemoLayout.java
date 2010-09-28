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
/**
 * 
 */
package netinf.tools.logging;

import java.util.HashMap;
import java.util.Properties;

import netinf.common.utils.Utils;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * The Class ColoredDemoLayout.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class ColoredDemoLayout extends PatternLayout {

   private static final String CSI = Character.toString((char) 27) + "[";
   private static final String DEFAULT = CSI + "0m";

   private final HashMap<String, String> colorDefs;

   private final boolean color;

   /**
    * 
    */
   public ColoredDemoLayout() {
      this.colorDefs = new HashMap<String, String>();
      Properties prop = Utils.loadProperties("../configs_official/log4j/logging.properties");
      this.color = !prop.containsKey("color") || Boolean.valueOf((String) prop.get("color"));
      if (this.color) {
         for (Object keyObject : prop.keySet()) {

            String key = null;
            if (keyObject instanceof String && !"color".equals(keyObject)) {
               key = (String) keyObject;
            }

            Object valueObject = prop.get(keyObject);
            String value = null;
            if (valueObject instanceof String) {
               value = (String) valueObject;
            }

            if (key != null && value != null) {
               this.colorDefs.put(key, value);
            }
         }
      }
   }


   private void addColor(String source, String color) {
      this.colorDefs.put(source, ColoredDemoLayout.CSI + color);
   }

   @Override
   public String format(LoggingEvent event) {
      StringBuffer buf = new StringBuffer();
      String line = super.format(event);
      String app = (String) event.getProperties().get("application");
      if (this.color && this.colorDefs.containsKey(app)) {
         buf.append(ColoredDemoLayout.CSI);
         buf.append(this.colorDefs.get(app));
         buf.append("m");
         buf.append(line);
         buf.append(ColoredDemoLayout.DEFAULT);
      } else {
         buf.append(line);
      }
      return buf.toString();
   }

}
