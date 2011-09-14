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
package netinf.tools.iomanagement.log;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This is a log4j appender for a jTextArea.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class TextAreaAppender extends WriterAppender {

   /** JTextArea used for logging (usually in main window) */
   private static JTextArea logTextArea = null;

   /**
    * @return the JTextArea used for logging
    */
   public static JTextArea getLogTextArea() {
      return logTextArea;
   }

   /**
    * @param logTextArea
    *           the JTextArea to use for logging
    */
   public static void setLogTextArea(JTextArea logTextArea) {
      TextAreaAppender.logTextArea = logTextArea;
   }

   /**
    * Set the target JTextArea for the logging information to appear.
    * 
    * @param jTextArea
    *           target for logging
    */
   public static void setTextArea(JTextArea jTextArea) {
      TextAreaAppender.setLogTextArea(jTextArea);
   }

   /**
    * Format and then append the loggingEvent to the stored JTextArea.
    */
   @Override
   public void append(LoggingEvent loggingEvent) {
      if (getLogTextArea() != null) {
         final String message = this.layout.format(loggingEvent);

         // Append formatted message to textarea using the Swing Thread.
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               getLogTextArea().append(message);
            }

         });
      }

   }
}
