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
package netinf.tools.shopping;

import org.apache.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Set Up Shopping Tool
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class StarterShopping {
   private static final Logger LOG = Logger.getLogger(StarterShopping.class);

   public static void main(String[] args) {
      LOG.trace("enter");

      LOG.debug("Shopping Tool starting up");
      Module module = findCorrectModule(args);
      Injector injector = Guice.createInjector(module);
      MainFrame mainFrame = injector.getInstance(MainFrame.class);
      mainFrame.setVisible(true);

      LOG.trace("exit");
   }

   private static Module findCorrectModule(String[] args) {
      Module result = null;

      String moduleName = null;

      try {
         moduleName = args[0];

         Class<?> moduleClass = Class.forName(moduleName);
         Module module = (Module) moduleClass.newInstance();

         System.out.println("Using module '" + moduleName + "' for configuration");
         result = module;
      } catch (ClassNotFoundException e) {
         System.out.println("Not recognized module '" + moduleName + "'");
      } catch (InstantiationException e) {
         System.out.println("Not recognized module '" + moduleName + "'");
      } catch (IllegalAccessException e) {
         System.out.println("Not recognized module '" + moduleName + "'");
      } catch (ArrayIndexOutOfBoundsException aiooe) {
         System.out.println("No parameter given for module.");
      } finally {
         if (result == null) {
            System.out.println("Using default ShoppingModule");
            result = new ShoppingModule();
         }
      }

      return result;
   }
}
