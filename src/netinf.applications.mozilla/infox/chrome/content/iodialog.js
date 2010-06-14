/* Logging for the Information Object Dialog
 */
function log(strMessage) {
		var consoleService	= 	Components.classes["@mozilla.org/consoleservice;1"]
								.getService(Components.interfaces.nsIConsoleService);
		consoleService.logStringMessage("InFox: " + strMessage);

}

/* Called once the Information Object Dialog is loading. Checks if the Node-side security check
 * was successful and lists all first-level attributes of the IO.
 */
function onLoad() {
 
	var rdfIO = window.arguments[0].inn.io;
	var nsResolver = rdfIO.createNSResolver( rdfIO.ownerDocument == null ? rdfIO.documentElement : rdfIO.ownerDocument.documentElement);
	  
	var securitycheckfailed = rdfIO.evaluate('//netinf:securityCheckFailed', rdfIO, nsResolver, XPathResult.ANY_TYPE, null );
	
	var verificationbox = document.getElementById("verificationbox");
	if (securitycheckfailed.length > 0) {
		verificationbox.className = "invalid";
		document.getElementById("verificationstatus-text").value = "Failed";
	} else {
		verificationbox.className = "valid";
	}
	
	
	var attribute = rdfIO.evaluate('//netinf:attributeValue/parent::*', rdfIO, nsResolver, XPathResult.ANY_TYPE, null );
	var attributepurpose = rdfIO.evaluate('//netinf:attributePurpose', rdfIO, nsResolver, XPathResult.ANY_TYPE, null );
	var attributevalue = rdfIO.evaluate('//netinf:attributeValue', rdfIO, nsResolver, XPathResult.ANY_TYPE, null );
		
	var tree = document.getElementById("attribrows");
	
	addAttributeRows(rdfIO, tree);
	
	window.resizeTo(window.screen.availWidth/2, window.screen.availHeight/2);

}

/* Adds all child attributes to a attribute list item.
 * 
 * @param xmlNode xml node of the attribute
 * @param list tree item
 */
function addChildAttribute(xmlNode, tree) {
	//FIXME: This might not work as expected
	for(var i = 0; i < xmlNode.length; i++) {
		//alert(xmlNode[i].nodeName);
		var item = document.createElement("treeitem");
		var row  = document.createElement("treerow");
		
		var security = document.createElement("treecell");
		var name = document.createElement("treecell");
		var purpose = document.createElement("treecell");
		var value = document.createElement("treecell");
		
		tree.appendChild(item);
		item.appendChild(row);
		row.appendChild(security);
		row.appendChild(name);
		row.appendChild(purpose);
		row.appendChild(value);
		
		security.setAttribute("src", "chrome://infox/skin/padlock-closed.png");
		name.setAttribute("label", xmlNode[i].nodeName);
		purpose.setAttribute("label", xmlNode[i].getElementsByTagName("netinf:attributePurpose")[0].textContent);
		value.setAttribute("label", xmlNode[i].getElementsByTagName("netinf:attributeValue")[0].textContent);
	}
}

/* Adds the first-level attributes to the list
 */
function addAttributeRows(rdfIO, tree) {
	
	var nsResolver = rdfIO.createNSResolver( rdfIO.ownerDocument == null ? rdfIO.documentElement : rdfIO.ownerDocument.documentElement);
	
	var attribute = rdfIO.evaluate('//netinf:transportedIO/child::*/child::*[./netinf:attributeValue]', rdfIO, nsResolver, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null );
	var attributepurpose = rdfIO.evaluate('//netinf:attributePurpose', rdfIO, nsResolver, XPathResult.ANY_TYPE, null );
	var attributevalue = rdfIO.evaluate('//netinf:attributeValue', rdfIO, nsResolver, XPathResult.ANY_TYPE, null );

	var a = [];  
	for(var i = 0; i < attribute.snapshotLength; i++) {  
		 a[i] = attribute.snapshotItem(i);  
	}
	
	var tree = document.getElementById("attribrows");

	addChildAttribute(a, tree);
}

/* Called once if and only if the user clicks OK
 */
function onOK() {
   // Return the changed arguments.
   // Notice if user clicks cancel, window.arguments[0].out remains null
   // because this function is never called
   window.arguments[0].out = {
		   name:document.getElementById("name").value,
		   description:document.getElementById("description").value
   };
   return true;
}