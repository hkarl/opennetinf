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
package netinf.node.resolution.pastry;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import rice.environment.params.ParameterChangeListener;
import rice.environment.params.Parameters;
import rice.environment.params.simple.ParamsNotPresentException;

/**
 * The Class NetInfPastryParameters.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class NetInfPastryParameters implements Parameters {
   private Properties properties;

   private Properties defaults;

   private Set<ParameterChangeListener> changeListeners;

   public static final String DEFAULT_PARAMS_IN_CLASSPATH = "freepastry.params";

   public static final String ARRAY_SPACER = ",";

   /**
    * @param orderedDefaults
    * @param mutableConfigFileName
    *           if this is null, no params are saved, if this file doesn't exist, you will get a warning printed to stdErr, then
    *           the file will be created if you ever store
    * @throws IOException
    */
   public NetInfPastryParameters(Properties properties) {
      this.properties = new Properties(properties);
      this.defaults = new Properties();
      this.changeListeners = new HashSet<ParameterChangeListener>();
      try {
         ClassLoader loader = this.getClass().getClassLoader();
         // some VMs report the bootstrap classloader via null-return
         if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
         }
         this.defaults.load(loader.getResource(DEFAULT_PARAMS_IN_CLASSPATH).openStream());
      } catch (Exception ioe) {
         String errorString = "Warning, couldn't load param file:" + (DEFAULT_PARAMS_IN_CLASSPATH);
         throw new ParamsNotPresentException(errorString, ioe);
      }

   }

   public Enumeration enumerateDefaults() {
      return defaults.keys();
   }

   public Enumeration enumerateNonDefaults() {
      return properties.keys();
   }

   protected InetSocketAddress parseInetSocketAddress(String name) throws UnknownHostException {
      String host = name.substring(0, name.indexOf(':'));
      String port = name.substring(name.indexOf(':') + 1);

      try {
         return new InetSocketAddress(InetAddress.getByName(host), Integer.parseInt(port));
      } catch (UnknownHostException uhe) {
         System.err.println("ERROR: Unable to find IP for ISA " + name + " - returning null.");
         return null;
      }
   }

   protected String getProperty(String name) {
      String result = properties.getProperty(name);

      if (result == null) {
         result = defaults.getProperty(name);
      }

      if (result == null) {
         System.err.println("WARNING: The parameter '" + name + "' was not found - this is likely going to cause an error.");
         // You " +
         // "can fix this by adding this parameter (and appropriate value) to the
         // proxy.params file in your ePOST " +
         // "directory.");
      } else {
         // remove any surrounding whitespace
         result = result.trim();
      }

      return result;
   }

   /**
    * Note, this method does not implicitly call store()
    * 
    * @see #store()
    * @param name
    * @param value
    */
   protected void setProperty(String name, String value) {
      if ((defaults.getProperty(name) != null) && (defaults.getProperty(name).equals(value))) {
         // setting property back to default, remove override property if any
         if (properties.getProperty(name) != null) {
            properties.remove(name);
            fireChangeEvent(name, value);
         }
      } else {
         if ((properties.getProperty(name) == null) || (!properties.getProperty(name).equals(value))) {
            properties.setProperty(name, value);
            fireChangeEvent(name, value);
         }
      }
   }

   public void remove(String name) {
      properties.remove(name);
      fireChangeEvent(name, null);
   }

   public boolean contains(String name) {
      if (defaults.containsKey(name)) {
         return true;
      }
      return properties.containsKey(name);
   }

   public int getInt(String name) {
      try {
         return Integer.parseInt(getProperty(name));
      } catch (NumberFormatException nfe) {
         throw new NumberFormatException(nfe.getMessage() + " for parameter " + name);
      }
   }

   public double getDouble(String name) {
      try {
         return Double.parseDouble(getProperty(name));
      } catch (NumberFormatException nfe) {
         throw new NumberFormatException(nfe.getMessage() + " for parameter " + name);
      }
   }

   public float getFloat(String name) {
      try {
         return Float.parseFloat(getProperty(name));
      } catch (NumberFormatException nfe) {
         throw new NumberFormatException(nfe.getMessage() + " for parameter " + name);
      }
   }

   public long getLong(String name) {
      try {
         return Long.parseLong(getProperty(name));
      } catch (NumberFormatException nfe) {
         throw new NumberFormatException(nfe.getMessage() + " for parameter " + name);
      }
   }

   public boolean getBoolean(String name) {
      return Boolean.parseBoolean(getProperty(name));
   }

   public InetAddress getInetAddress(String name) throws UnknownHostException {
      return InetAddress.getByName(getString(name));
   }

   public InetSocketAddress getInetSocketAddress(String name) throws UnknownHostException {
      return parseInetSocketAddress(getString(name));
   }

   public InetSocketAddress[] getInetSocketAddressArray(String name) throws UnknownHostException {
      if (getString(name).length() == 0) {
         return new InetSocketAddress[0];
      }

      String[] addresses = getString(name).split(ARRAY_SPACER);
      List<InetSocketAddress> result = new LinkedList<InetSocketAddress>();

      for (int i = 0; i < addresses.length; i++) {
         InetSocketAddress address = parseInetSocketAddress(addresses[i]);

         if (address != null) {
            result.add(address);
         }
      }

      return result.toArray(new InetSocketAddress[result.size()]);
   }

   public String getString(String name) {
      return getProperty(name);
   }

   public String[] getStringArray(String name) {
      String list = getProperty(name);

      if (list != null) {
         return ("".equals(list) ? new String[0] : list.split(ARRAY_SPACER));
      } else {
         return null;
      }
   }

   public void setInt(String name, int value) {
      setProperty(name, Integer.toString(value));
   }

   public void setDouble(String name, double value) {
      setProperty(name, Double.toString(value));
   }

   public void setFloat(String name, float value) {
      setProperty(name, Float.toString(value));
   }

   public void setLong(String name, long value) {
      setProperty(name, Long.toString(value));
   }

   public void setBoolean(String name, boolean value) {
      setProperty(name, Boolean.toString(value));
   }

   public void setInetAddress(String name, InetAddress value) {
      setProperty(name, value.getHostAddress());
   }

   public void setInetSocketAddress(String name, InetSocketAddress value) {
      setProperty(name, value.getAddress().getHostAddress() + ":" + value.getPort());
   }

   public void setInetSocketAddressArray(String name, InetSocketAddress[] value) {
      StringBuffer buffer = new StringBuffer();

      for (int i = 0; i < value.length; i++) {
         buffer.append(value[i].getAddress().getHostAddress()).append(':').append(value[i].getPort());
         if (i < value.length - 1) {
            buffer.append(ARRAY_SPACER);
         }
      }

      setProperty(name, buffer.toString());
   }

   public void setString(String name, String value) {
      setProperty(name, value);
   }

   public void setStringArray(String name, String[] value) {
      StringBuffer buffer = new StringBuffer();

      for (int i = 0; i < value.length; i++) {
         buffer.append(value[i]);
         if (i < value.length - 1) {
            buffer.append(ARRAY_SPACER);
         }
      }

      setProperty(name, buffer.toString());
   }

   public void store() throws IOException {
      // We don't store our Properties here. Will be managed centrally for the whole netinf node.
   }

   public void addChangeListener(ParameterChangeListener p) {
      changeListeners.add(p);
   }

   public void removeChangeListener(ParameterChangeListener p) {
      changeListeners.remove(p);
   }

   private void fireChangeEvent(String name, String val) {
      Iterator<ParameterChangeListener> i = changeListeners.iterator();
      while (i.hasNext()) {
         ParameterChangeListener p = i.next();
         p.parameterChange(name, val);
      }
   }

}
