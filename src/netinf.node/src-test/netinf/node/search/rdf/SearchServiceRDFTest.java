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
package netinf.node.search.rdf;

import java.util.Properties;
import java.util.Set;

import junit.framework.Assert;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.utils.Utils;
import netinf.node.rdf.ClearSDBDatabase;
import netinf.node.resolution.InformationObjectHelper;
import netinf.node.resolution.rdf.RDFResolutionService;
import netinf.node.search.SearchController;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Unit tests for the {@link SearchServiceRDF}
 * 
 * @author PG Augnet 2, University of Paderborn
 * @see SearchServiceRDFTestModule
 */
public class SearchServiceRDFTest {

   private static final String CONFIGS_TESTING_PROPERTIES = "../configs/testing.properties";
   private SearchController searchController;
   private SearchServiceRDF searchService;
   private RDFResolutionService rdfResolutionService;
   private static InformationObjectHelper ioHelper;

   @Before
   public void setUp() {
      PropertyConfigurator.configure(Utils.loadProperties(CONFIGS_TESTING_PROPERTIES));
      ClearSDBDatabase.main(null);

      final Properties properties = Utils.loadProperties(CONFIGS_TESTING_PROPERTIES);
      final Injector injector = Guice.createInjector(new SearchServiceRDFTestModule(properties));
      ioHelper = injector.getInstance(InformationObjectHelper.class);
      searchController = injector.getInstance(SearchController.class);
      searchService = injector.getInstance(SearchServiceRDF.class);
      rdfResolutionService = injector.getInstance(RDFResolutionService.class);
      searchController.addSearchService(searchService);
   }

   @After
   public void tearDown() {
      searchService.tearDown();
      rdfResolutionService.tearDown();
   }

   @Test
   public void identifiersExist() {

      // prepare database
      final InformationObject io = ioHelper.getDummyIO();
      rdfResolutionService.put(io);

      // check if service is ready
      Assert.assertEquals(true, searchService.isReady());

      final int[] preparationValues = searchController.getTimeoutAndNewSearchID(5000);
      final Set<Identifier> searchResult = searchController.getBySPARQL("", preparationValues[1]);

      // check result
      Assert.assertEquals(1, searchResult.size());
      Assert.assertTrue(searchResult.contains(io.getIdentifier()));

   }

   @Test
   public void noHit() {
      // check if service is ready
      Assert.assertEquals(true, searchService.isReady());

      final int[] preparationValues = searchController.getTimeoutAndNewSearchID(5000);
      final Set<Identifier> searchResult = searchController.getBySPARQL("", preparationValues[1]);

      // check result
      Assert.assertEquals(0, searchResult.size());

   }

   @Test
   public void multipleRequests() {

      // prepare database
      final InformationObject io = ioHelper.getDummyIO();
      rdfResolutionService.put(io);

      // check if service is ready
      Assert.assertEquals(true, searchService.isReady());

      int[] preparationValues = searchController.getTimeoutAndNewSearchID(5000);
      Set<Identifier> searchResult = searchController.getBySPARQL("", preparationValues[1]);

      // check result
      Assert.assertEquals(1, searchResult.size());
      Assert.assertTrue(searchResult.contains(io.getIdentifier()));

      preparationValues = searchController.getTimeoutAndNewSearchID(5000);
      searchResult = searchController.getBySPARQL("", preparationValues[1]);

      // check result
      Assert.assertEquals(1, searchResult.size());
      Assert.assertTrue(searchResult.contains(io.getIdentifier()));

   }

}
