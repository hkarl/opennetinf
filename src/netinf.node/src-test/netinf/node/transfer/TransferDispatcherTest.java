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
package netinf.node.transfer;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;
import netinf.node.transferdispatcher.TransferDispatcher;
import netinf.node.transferdispatcher.streamprovider.NetInfNoStreamProviderFoundException;

import org.junit.AfterClass;
import org.junit.Test;

/**
 * Test routines for the {@link TransferDispatcher}
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class TransferDispatcherTest {

   @Test
   public void testGetStreamAndSave() {
      try {
         String url = "http://www.uni-paderborn.de/uploads/pics/Resounding_Tinkle.JPG";
         String destination = "Resounding_Tinkle.JPG";
         TransferDispatcher dispatcher = TransferDispatcher.getInstance();
         dispatcher.getStreamAndSave(url, destination, false);
         File f = new File(destination);
         Assert.assertTrue(f.exists());
      } catch (NetInfNoStreamProviderFoundException e) {
         Assert.fail();
      } catch (IOException e) {
         Assert.fail();
      }
   }

   @AfterClass
   public static void cleanUp() {
      File f = new File("Resounding_Tinkle.JPG");
      if (f.exists()) {
         f.delete();
      }
   }

}
