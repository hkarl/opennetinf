
import static org.junit.Assert.assertEquals;
import netinf.common.search.DefinedQueryTemplates;

import org.junit.Test;

public class DefinedQueryTemplatesTest {
	
	@Test
	public void testSearch(){
		DefinedQueryTemplates def = DefinedQueryTemplates.getDefinedQueryTemplateName("Test");
		assertEquals(def,def);
		
		String parameters[][] = new String[][] {
		         { "Latitude", "java.lang.Double" }, { "Longitude", "java.lang.Double" }, { "Radius", "java.lang.Integer" },
		         { "ProductsSemicolonSeparated", "java.lang.String" } };
	
		for(int i=0 ; i<def.getParameters().length;i++){
			assertEquals(parameters[i][0].toString(),def.getParameters()[i][0].toString());
			assertEquals(parameters[i][1].toString(),def.getParameters()[i][1].toString());
		}
		
		assertEquals(null,DefinedQueryTemplates.getDefinedQueryTemplateName("positionBasedShopInRadiusHasProduct"));
		assertEquals("positionBasedShopInRadiusHasProduct",def.getQueryTemplateName());
		
	}
}