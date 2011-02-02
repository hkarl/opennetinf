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
package netinf.tools.iomanagement;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

/**
 * Provides static methods to get strings and icons used in the tool
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class Constants {
   /** trace-log for exiting a method */
   public static final String LOG_EXIT = "exit";
   /** trace-log for entering a method */
   public static final String LOG_ENTER = "enter";

   /** Log4j Logger component */
   private static final Logger log = Logger.getLogger(Constants.class);

   /** cache for icons */
   private static Map<String, ImageIcon> iconStore = new HashMap<String, ImageIcon>();

   /**
    * Properties that save the strings
    */
   private static Properties properties = null;

   /**
    * returns an icon for an id (possibly from cache) the icon is inserted into the cache if it is requested for the first time.
    * 
    * @param iconID
    *           id of the icon
    * @return requested icon
    */
   public static ImageIcon getIcon(String iconID) {
      log.trace(Constants.LOG_ENTER);

      String iconName = getString("ICON_" + iconID);
      if (!iconStore.containsKey(iconName)) {
         URL imageUrl = Constants.class.getResource("/" + iconName);
         ImageIcon icon = new ImageIcon(imageUrl);
         iconStore.put(iconName, icon);
         log.trace(Constants.LOG_EXIT);
         return icon;
      }
      log.trace(Constants.LOG_EXIT);
      return iconStore.get(iconName);
   }

   /**
    * returns the properties that are used for string constants
    * 
    * @return properties
    */
   public static Properties getProperties() {
      return properties;
   }

   /**
    * Retrieve a string from the string configuration
    * 
    * @param propertyName
    *           name of requested string
    * @return requested string, never null
    */
   public static String getString(String propertyName) {
      log.trace(Constants.LOG_ENTER);

      if (getProperties() == null) {
         log.debug("Loading Strings properties");
         setProperties(new Properties());
         try {
            getProperties().load(Constants.class.getResourceAsStream("/strings.properties"));
         } catch (IOException e) {
            log.error("Could not load Strings properties");
         }
      }
      String propertyValue = getProperties().getProperty(propertyName);
      if (propertyValue == null) {
         log.error("Could not load String for " + propertyName);
         log.trace(Constants.LOG_EXIT);
         return "String error, see logs for details";
      }

      log.trace(Constants.LOG_EXIT);
      return propertyValue;
   }

   /**
    * sets the properties that are used for string constants
    * 
    * @param properties
    *           new properties
    */
   public static void setProperties(Properties properties) {
      Constants.properties = properties;
   }

}
