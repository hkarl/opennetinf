/*
Copyright (C) 2009,2010 University of Paderborn, Computer Networks Group
Copyright (C) 2009,2010 Christian Dannewitz <christian.dannewitz@upb.de>
Copyright (C) 2009,2010 Thorsten Biermann <thorsten.biermann@upb.de>

Copyright (C) 2009,2010 Eduard Bauer <edebauer@mail.upb.de>
Copyright (C) 2009,2010 Matthias Becker <matzeb@mail.upb.de>
Copyright (C) 2009,2010 Frederic Beister <azamir@zitmail.uni-paderborn.de>
Copyright (C) 2009,2010 Nadine Dertmann <ndertmann@gmx.de>
Copyright (C) 2009,2010 Martin Dräxler <draexler@mail.uni-paderborn.de>
Copyright (C) 2009,2010 Michael Kionka <mkionka@mail.upb.de>
Copyright (C) 2009,2010 Mario Mohr <mmohr@mail.upb.de>
Copyright (C) 2009,2010 Felix Steffen <felix.steffen@gmx.de>
Copyright (C) 2009,2010 Sebastian Stey <sebstey@mail.upb.de>
Copyright (C) 2009,2010 Steffen Weber <stwe@mail.upb.de>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
Neither the name of the University of Paderborn nor the
names of its contributors may be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
/*
 * Logging for the Information Object Dialog
 */
function log(strMessage) {
		var consoleService = Components.classes["@mozilla.org/consoleservice;1"].getService(Components.interfaces.nsIConsoleService);
		consoleService.logStringMessage("InFox: " + strMessage);
}

/*
 * Called once the Information Object Dialog is loading. Checks if the Node-side security check
 * was successful and lists all first-level attributes of the IO.
 */
function onLoad() {
	// read parameters
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

/*
 * Adds all child attributes to a attribute list item.
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

/*
 * Adds the first-level attributes to the list
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

/*
 * Called once if and only if the user clicks OK
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
