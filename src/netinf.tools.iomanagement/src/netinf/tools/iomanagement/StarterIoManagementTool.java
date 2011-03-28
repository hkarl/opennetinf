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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import netinf.common.datamodel.InformationObject;
import netinf.tools.iomanagement.gui.frames.MainFrame;

import org.apache.log4j.xml.DOMConfigurator;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;

// Command Line Options
//
// [(-n|--node-host) <host>]
//    The hostname or IP address the node is running on (default: localhost)
//
// [(-p|--port) <port>]
//    The port number the node is running on (default: 5000)
//
// [(-f|--serialization-format) <format>]
//    The serialization format to use (JAVA/RDF) (default: RDF)
//
// [(-d|--add-datatypes) dtype1:dtype2:...:dtypeN ]
//    allows to add datatypes for IOs - fully qualified! (default: )
//
// [(-l|--logging) logging1:logging2:...:loggingN ]
//    sets the logging configuration (default: log4j/standardTool.xml)
//
// [-h|--help]
//    Displays help
/**
 * Starter class for the NetInf IO Management tool for command line options: {@link StarterIoManagementTool}
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class StarterIoManagementTool {

   /**
    * @param args
    *           command line arguments (see {@link StarterIoManagementTool} for details)
    */
   public static void main(String[] args) {

      JSAPResult parsedCL = parseCommandLine(args);
      if (parsedCL == null) {
         System.exit(1);
      } else {
         Injector injector = Guice.createInjector(new IoManagementModule(parsedCL));
         MainFrame mainFrame = injector.getInstance(MainFrame.class);
         mainFrame.setVisible(true);
      }

   }

   /**
    * @param args
    *           command line arguments
    * @return parsing result
    */
   private static JSAPResult parseCommandLine(String[] args) {
      JSAP parser = new JSAP();

      FlaggedOption hostOption = new FlaggedOption("host");
      hostOption.setStringParser(JSAP.INETADDRESS_PARSER);
      hostOption.setShortFlag('n');
      hostOption.setLongFlag("node-host");
      hostOption.setDefault(Constants.getString("DEFAULT_HOST"));
      hostOption.setHelp("The hostname or IP address the node is running on");

      FlaggedOption portOption = new FlaggedOption("port"); // NOPMD by pgaugnet2 on 16.03.10 14:13
      portOption.setStringParser(JSAP.INTEGER_PARSER);
      portOption.setShortFlag('p');
      portOption.setLongFlag("port");
      portOption.setDefault(Constants.getString("DEFAULT_PORT"));
      portOption.setHelp("The port number the node is running on");

      FlaggedOption serializationFormatOption = new FlaggedOption("format"); // NOPMD by pgaugnet2 on 16.03.10 14:13
      serializationFormatOption.setStringParser(JSAP.STRING_PARSER);
      serializationFormatOption.setShortFlag('f');
      serializationFormatOption.setLongFlag("serialization-format");
      serializationFormatOption.setDefault(Constants.getString("DEFAULT_FORMAT"));
      serializationFormatOption.setHelp("The serialization format to use (JAVA/RDF)");

      FlaggedOption datatypesOption = new FlaggedOption("dtype");
      datatypesOption.setList(JSAP.LIST);
      datatypesOption.setShortFlag('d');
      datatypesOption.setLongFlag("add-datatypes");
      datatypesOption.setDefault(new String[0]);
      datatypesOption.setHelp("allows to add datatypes for IOs - fully qualified!");

      FlaggedOption loggingOption = new FlaggedOption("logging");
      loggingOption.setList(JSAP.LIST);
      loggingOption.setShortFlag('l');
      loggingOption.setLongFlag("logging");
      loggingOption.setDefault("log4j/standardTool.xml");
      loggingOption.setHelp("sets the logging configuration");

      Switch helpSwitch = new Switch("help");
      helpSwitch.setShortFlag('h');
      helpSwitch.setLongFlag("help");
      helpSwitch.setHelp("Displays this help");

      try {
         parser.registerParameter(hostOption);
         parser.registerParameter(portOption);
         parser.registerParameter(serializationFormatOption);
         parser.registerParameter(datatypesOption);
         parser.registerParameter(loggingOption);
         parser.registerParameter(helpSwitch);
      } catch (JSAPException e) {
         System.err.println("JSAP command line error: " + e.getMessage());
      }

      JSAPResult result = parser.parse(args);

      String logConf = "../configs/" + result.getString("logging");
      System.out.println("Using logging configuration " + logConf);
      DOMConfigurator.configure(logConf);

      ArrayList<String> semanticErrors = new ArrayList<String>();
      // check port number
      if (result.contains("port") && (result.getInt("port") < 0 || result.getInt("port") > 65535)) {
         semanticErrors.add("Invalid port number: " + result.getInt("port"));
      }

      // check additional datatypes
      if (result.contains("dtype")) {
         for (String dtype : result.getStringArray("dtype")) {
            try {
               Class<?> tempClass = Class.forName(dtype);
               if (!Arrays.asList(tempClass.getInterfaces()).contains(InformationObject.class)) {
                  semanticErrors.add(dtype + " found but not a valid IO datatype");
               }
            } catch (ClassNotFoundException e) {
               semanticErrors.add("Couldn't find datatype " + dtype);
            }
         }
      }

      // check format
      if (result.contains("format")
            && !(result.getString("format").toUpperCase().equals("RDF") || result.getString("format").toUpperCase()
                  .equals("JAVA"))) {
         semanticErrors.add(result.getString("format") + " is not a valid serialization format");
      }

      // RETURN IF OK
      if (result.success() && !result.getBoolean("help") && semanticErrors.isEmpty()) {
         return result;
      }

      // error occured OR user requested help
      System.err.println();
      for (Iterator<?> errs = result.getErrorMessageIterator(); errs.hasNext();) {
         System.err.println("Error: " + errs.next());
      }
      for (Iterator<String> errs = semanticErrors.iterator(); errs.hasNext();) {
         System.err.println("Error: " + errs.next());
      }

      System.err.println();
      System.err.println("Usage: java " + StarterIoManagementTool.class.getName());
      System.err.println("                " + parser.getUsage());
      System.err.println();
      System.err.println(parser.getHelp());
      return null;

   }
}
