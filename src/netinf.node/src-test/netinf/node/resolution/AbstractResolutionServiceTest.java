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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.List;

import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfResolutionException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.node.resolution.eventprocessing.EventPublisher;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

/**
 * This class contains tests applicable to all resolutionServices.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public abstract class AbstractResolutionServiceTest {

   protected static InformationObjectHelper ioHelper;
   protected ResolutionService resolutionService;

   @Test
   public void testPutGet() throws NetInfCheckedException {
      resolutionService.put(ioHelper.getDummyIO());
      InformationObject io = resolutionService.get(ioHelper.getDummyIO().getIdentifier());
      Assert.assertTrue(ioHelper.getDummyIO().equals(io));
   }

   @Test
   public void testGetIdentity() {
      Assert.assertNotNull(resolutionService.getIdentity());
   }

   @Test
   public void testPutWithFailingEventPublisher() {
      InformationObject io = ioHelper.createUniqueIO();
      InformationObject io2 = ioHelper.createUniqueIO();
      EventPublisher ep = createMock(EventPublisher.class);
      ep.publishPut(null, io);
      EasyMock.expectLastCall().andThrow(new NetInfUncheckedException("Connection Error"));
      replay(ep);
      EventPublisher ep2 = createMock(EventPublisher.class);
      ep2.publishPut(null, io);
      ep2.publishPut(null, io2);
      replay(ep2);
      resolutionService.addEventService(ep);
      resolutionService.addEventService(ep2);
      resolutionService.put(io);
      resolutionService.put(io2);
      verify(ep);
   }

   @Test
   public void testDelete() throws NetInfCheckedException {
      resolutionService.put(ioHelper.getDummyIO());
      EventPublisher ep = createMock(EventPublisher.class);
      ep.publishDelete(ioHelper.getDummyIO());
      replay(ep);
      resolutionService.addEventService(ep);
      resolutionService.delete(ioHelper.getDummyIO().getIdentifier());
      Assert.assertNull(resolutionService.get(ioHelper.getDummyIO().getIdentifier()));
      EasyMock.verify(ep);
   }

   @Test(expected = NetInfResolutionException.class)
   public void testDeleteVersioned() {
      InformationObject obj = ioHelper.createUniqueVersionedIO();
      try {
         resolutionService.put(obj);
      } catch (NetInfResolutionException e) {
         throw new RuntimeException(e);
      }

      EventPublisher ep = EasyMock.createStrictMock(EventPublisher.class);
      ep.publishDelete(obj);
      replay(ep);
      resolutionService.delete(obj.getIdentifier());
      EasyMock.verify(ep);
   }

   @Test
   public void testDeleteNonExisting() {
      EventPublisher ep = createMock(EventPublisher.class);
      replay(ep);
      resolutionService.delete(ioHelper.getDummyIO().getIdentifier());
      EasyMock.verify(ep);
   }

   @Test(expected = NetInfResolutionException.class)
   public void testPutVersionedWithoutVersion() {
      InformationObject obj = ioHelper.createUniqueVersionedIO();
      obj.getIdentifier().removeIdentifierLabel(DefinedLabelName.VERSION_NUMBER.getLabelName());
      resolutionService.put(obj);
   }

   @Test
   public void testGetAllVersions() {
      resolutionService.put(ioHelper.createUniqueIO());
      InformationObject obj1 = ioHelper.createDummyIOWithUniqueVersion();
      List<Identifier> list = resolutionService.getAllVersions(obj1.getIdentifier());
      Assert.assertNull(list);
      resolutionService.put(obj1);
      InformationObject obj2 = ioHelper.createDummyIOWithUniqueVersion();
      resolutionService.put(obj2);
      list = resolutionService.getAllVersions(obj1.getIdentifier());
      Assert.assertEquals(2, list.size());
      Assert.assertEquals(obj1, resolutionService.get(list.get(0)));
      Assert.assertEquals(obj2, resolutionService.get(list.get(1)));
   }

   @Test(expected = NetInfResolutionException.class)
   public void testGetAllVersionsOfUnversionedIO() {
      resolutionService.getAllVersions(ioHelper.createUniqueIO().getIdentifier());
   }

   @Test
   public void testGetVersionedWithoutVersion() {
      InformationObject obj1 = ioHelper.createDummyIOWithUniqueVersion();
      InformationObject obj2 = ioHelper.createDummyIOWithUniqueVersion();
      resolutionService.put(obj1);
      resolutionService.put(obj2);
      obj1.getIdentifier().removeIdentifierLabel(DefinedLabelName.VERSION_NUMBER.getLabelName());
      // Assert.assertNotSame(obj1, actual)
      Assert.assertEquals(obj2, resolutionService.get(obj1.getIdentifier()));

   }

   @Test
   public void testPutWithEventPublisher() {
      InformationObject io = ioHelper.getDummyIO();
      EventPublisher ep = createMock(EventPublisher.class);
      ep.publishPut(null, io);
      replay(ep);
      resolutionService.addEventService(ep);
      resolutionService.put(ioHelper.getDummyIO());
      verify(ep);
   }

   @Test
   public void testGetNotThere() throws NetInfCheckedException {
      InformationObject io = resolutionService.get(ioHelper.getDummyIO().getIdentifier());
      Assert.assertNull(io);
   }

}
