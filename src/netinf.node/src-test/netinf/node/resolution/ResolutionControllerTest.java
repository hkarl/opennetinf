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
package netinf.node.resolution;

import static org.junit.Assert.fail;
import netinf.common.security.SecurityManager;
import netinf.node.resolution.impl.ResolutionControllerImpl;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * The Class ResolutionControllerTest.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
@Ignore
public class ResolutionControllerTest {

   private static ResolutionController resolutionController;
   private static ResolutionService localRsMock;
   private static ResolutionService remoteRsMock;

   @BeforeClass
   public static void setUp() throws Exception {
      ResolutionServiceSelector selector = EasyMock.createMock(ResolutionServiceSelector.class);
      SecurityManager securityManager = EasyMock.createMock(SecurityManager.class);

      resolutionController = new ResolutionControllerImpl(selector, securityManager);
      remoteRsMock = EasyMock.createMock(ResolutionService.class);
      localRsMock = EasyMock.createMock(ResolutionService.class);
   }

   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void testResolutionControllerImpl() {
      fail("Not yet implemented");
   }

   @Test
   public void testDelete() {
      fail("Not yet implemented");
   }

   @Test
   public void testGet() {
      fail("Not yet implemented");
   }

   @Test
   public void testGetAllVersions() {
      fail("Not yet implemented");
   }

   @Test
   public void testGetResolutionServices() {
      fail("Not yet implemented");
   }

   @Test
   public void testGetSupportedOperations() {
      fail("Not yet implemented");
   }

   @Test
   public void testPut() {

   }

   @Test
   public void testAddResolutionService() {
      fail("Not yet implemented");
   }

   @Test
   public void testRemoveResolutionService() {
      fail("Not yet implemented");
   }

   @Test
   public void testProcessNetInfMessage() {
      fail("Not yet implemented");
   }
}
