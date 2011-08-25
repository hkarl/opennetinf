package netinf.common.search;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author PG NetInf 3
 */
public class DefinedQueryTemplatesTest {

   @Test
   public void testSearch() {
      DefinedQueryTemplates def = DefinedQueryTemplates.getDefinedQueryTemplateName("positionBasedShopInRadiusHasProduct");
      assertEquals("positionBasedShopInRadiusHasProduct",
            DefinedQueryTemplates.getDefinedQueryTemplateName("positionBasedShopInRadiusHasProduct").getQueryTemplateName());

      assertEquals("Latitude", def.getParameters()[0][0]);

      /*
       * String parameters[][] = new String[][] { { "Latitude", "java.lang.Double" }, { "Longitude", "java.lang.Double" }, {
       * "Radius", "java.lang.Integer" }, { "ProductsSemicolonSeparated", "java.lang.String" } }; /*for(int i=0 ;
       * i<def.getParameters().length;i++){ assertEquals("Latitude",def.getParameters()[0][0]);
       * //assertEquals(parameters[i][1].toString(),def.getParameters()[i][1].toString()); }
       * assertEquals(null,DefinedQueryTemplates.getDefinedQueryTemplateName("positionBasedShopInRadiusHasProduct"));
       * assertEquals("positionBasedShopInRadiusHasProduct",def.getQueryTemplateName());
       */
   }
}
