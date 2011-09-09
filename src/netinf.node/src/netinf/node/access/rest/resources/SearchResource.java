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
package netinf.node.access.rest.resources;

import java.util.List;

import netinf.common.datamodel.Identifier;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfSearchException;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

/**
 * Resource to search for an identifier.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class SearchResource extends NetInfResource {

   private static final Logger LOG = Logger.getLogger(SearchResource.class);

   /**
    * Handler for GET-requests.
    * 
    * @return StringRepresentation of a search form possibly with results
    */
   @Get
   public Representation search() {
      String query = getQuery().getFirstValue("query", true, "");

      StringBuilder html = new StringBuilder();
      html.append("<html>");
      html.append("<head>");
      html.append("<title>Search</title>");
      html.append("</head>");
      html.append("<body>");

      html.append("<h1>Search</h1>");
      html.append("<form action=\"search\" method=\"get\">");
      html.append("<input type=\"text\" size=\"30\" name=\"query\" value=\"" + query + "\" />");
      html.append("<input type=\"submit\" value=\"Search\" />");
      html.append("</form>");
      html.append("<p><small>(Search is based on regular expressions)</small></p>");

      if (!query.equals("")) {
         List<Identifier> result = performQuery(query);
         if (result != null && !result.isEmpty()) {
            html.append("<h2>Results</h2>");
            html.append("<ul>");
            for (Identifier ident : result) {
               html.append("<li>");
               String identStr = ident.toString();
               if (identStr.length() > 60) {
                  html.append(identStr.substring(0, 30) + "..." + identStr.substring(identStr.length() - 30));
               } else {
                  html.append(identStr);
               }
               html.append("<br />");
               html.append("<a href=\"/io/" + identStr + "\">Details</a>");
               html.append(" ");
               html.append("<a href=\"/" + identStr + "\">Download</a>");
               html.append("</li>");
            }
            html.append("</ul>");
         } else {
            html.append("<h2>No results</h2>");
         }
      }
      html.append("</body>");
      html.append("</html>");
      return new StringRepresentation(html.toString(), MediaType.TEXT_HTML);
   }

   /**
    * Simple search using a predefined SPARQL query.
    * 
    * @param query
    *           Regular expression as String
    * @return List of Identifiers matching given query
    */
   private List<Identifier> performQuery(String query) {
      List<Identifier> result = null;
      StringBuilder sparql = new StringBuilder();
      sparql.append("?id ?p ?b.");
      sparql.append("?b netinf:attributeValue ?v.");
      sparql.append("FILTER regex(?v, \"" + query + "\", \"i\").");
      try {
         result = getNodeConnection().performSearch(sparql.toString(), 1000);
      } catch (NetInfSearchException nse) {
         LOG.warn("Error during search: " + nse.getMessage());
      } catch (NetInfCheckedException nce) {
         LOG.warn("Error: " + nce.getMessage());
      }
      return result;
   }

}
