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
/* Main Class containing all NetInf related methods
 */
var InBird = {
	
	/* Sends messages to the Thunderbird error console
	 * 
	 * @param strMessage The message to log
	 */
	log: function(strMessage) {
		var consoleService	= 	Components.classes["@mozilla.org/consoleservice;1"]
								.getService(Components.interfaces.nsIConsoleService);
		consoleService.logStringMessage("InBird: " + strMessage);

	},
	
	/* Replaces the NetInf receiver address with the mail address specified in the IdO
	 * 
	 * @param evt The compose-send-message event
	 */
	replaceAddress: function(evt) { 
	
		this.prefManager	= Components.classes["@mozilla.org/preferences-service;1"]
								.getService(Components.interfaces.nsIPrefService)
         						.getBranch("extensions.inbird.");
		
		this.prefManager.QueryInterface(Components.interfaces.nsIPrefBranch2);
		
		this.SERVER			= this.prefManager.getCharPref("server");
		this.PORT			= this.prefManager.getCharPref("port");
	
		var msgcomposeWindow = document.getElementById( "msgcomposeWindow" );  
		var msg_type = msgcomposeWindow.getAttribute( "msgtype" ); 
		
		// do not continue unless this is an actual send event  
		if( !(msg_type == nsIMsgCompDeliverMode.Now || msg_type == nsIMsgCompDeliverMode.Later) )  
			return;  
		
		// Modify the message subject if necessary 
		//gMsgCompose.compFields.subject += " - sent via InBird";  
		//document.getElementById("msgSubject").value = gMsgCompose.compFields.subject;  
		
		// Replace a NetInf formatted mail address ( attribute@IdO-Identifier ) with the mail address stored within the given attribute
		netinfaddress = gMsgCompose.compFields.to;
		
		var pattern = /([a-zA-Z0-9._-].)+<(.*)@(.*)\.netinf/;
		var patternfound = pattern.test(netinfaddress);
			
		var patternfound = pattern.test(netinfaddress);
		
		if (patternfound) {
			InBird.log("Detected NetInf IdentityObject (in e-mail format): " + netinfaddress);
			
			pattern.exec(netinfaddress);
			var attribute = RegExp.$2;
			var strIdentifier = RegExp.$3;

			// Get the IdO in RDF/XML format
			var xmlIO = InBird.getIO(strIdentifier);
			// Get the attribute from the IdO
			gMsgCompose.compFields.to = InBird.getAttribute(xmlIO, attribute, strIdentifier);
			 
			//gMsgCompose.compFields.priority = "3";  
			if( gMsgCompose.compFields.otherRandomHeaders != "" )  
				gMsgCompose.compFields.otherRandomHeaders += "\n\n";  
				gMsgCompose.compFields.otherRandomHeaders += "X-NetInf-Resolved: true\n";  
		    
			// Modify the message body if necessary
			try {  
				var editor = GetCurrentEditor();  
				var editor_type = GetCurrentEditorType();  
					editor.beginTransaction();  
					editor.beginningOfDocument(); // seek to beginning  
					
				if( editor_type == "textmail" || editor_type == "text" ) {  
					//editor.insertLineBreak();
					//editor.insertText( "---" );  
					//editor.insertLineBreak();
					//editor.insertText( "Sent via InBird" );   
				} else {  
					//editor.insertHTML( "<p>Sent via InBird</p>" );  
				}  
				
			    editor.endTransaction();  
			} catch(ex) {  
				Components.utils.reportError(ex);  
			    return false;  
			}  
		} else {
			InBird.log("No Identity Object detected.");  
			return false;
		}
	},
	
	/* Get an IO with Identifier strIdentifier via RSGetRequest
	 * 
	 * @param strIdentifier IO Identifier
	 */
	getIO: function(strIdentifier) {
		
		this.prefManager	= Components.classes["@mozilla.org/preferences-service;1"]
 								.getService(Components.interfaces.nsIPrefService)
          						.getBranch("extensions.inbird.");
 		
 		this.prefManager.QueryInterface(Components.interfaces.nsIPrefBranch2);
 		
 		this.SERVER			= this.prefManager.getCharPref("server");
 		this.PORT			= this.prefManager.getCharPref("port");		
		this.USERNAME		= this.prefManager.getCharPref("username");
		this.PRIVATEKEY		= this.prefManager.getCharPref("password");
		
		var http	= new XMLHttpRequest();
		var url 	= 'http://' + this.SERVER + ':' + this.PORT;
	    var params 	=	"<?xml version='1.0' encoding='UTF-8'?>\n" +
						"<RSGetRequest>\n" +
						"\t<SerializeFormat>RDF</SerializeFormat>\n" +
						"\t<Identifier>" + strIdentifier + "</Identifier>\n" +
						"\t<UserName>" + this.USERNAME + "</UserName>\n" +
						"\t<PrivateKey>" + this.PRIVATEKEY + "</PrivateKey>\n" +
						"\t<FetchAllVersions>true</FetchAllVersions>\n" +
					// TODO: Do we need to download the BinaryObject? This should be true in case we want to load it anyway
					//	"\t<DownloadBinaryObject>true</DownloadBinaryObject>\n" +
						"</RSGetRequest>";
	    
		InBird.log("Requesting " + strIdentifier + " from <" + url + ">" + ":\n\n" + params);
		
	    http.open("POST", url, false);
		
		//Send the proper header information along with the request
	    http.setRequestHeader("Content-type", "text/xml");
	    http.setRequestHeader("Content-length", params.length);
	    http.setRequestHeader("Connection", "close");
	
	    http.send(params);
	    //InBird.log(http.responseText); // ugly output
	    return http.responseXML;
	},
	
	/* Get the attribute value from the IO in XML/RDF format
	 * 
	 * @param informationObject The InformationObject in XML/RDF format
	 * @param attribute The attribute name
	 */
	getAttribute: function(informationObject, attribute, strIdentifier) {
		
		this.log("Looking for attribute <" + attribute + ">");
		
		//var parser = new DOMParser();
		//var informationObject = parser.parseFromString(textIO, "text/xml");
		
		if (informationObject.getElementsByTagName("ErrorMessage").length > 0) {
			Components.utils.reportError(informationObject.getElementsByTagName("ErrorMessage")[0].firstChild.nodeValue);
			return;
		}
		
		// The actually transfered IO looks like a RDF packaged into an XML. So we unpack it. I am not sure if this is necessary...		
		var xmlDoc = informationObject;
		var nsResolver = xmlDoc.createNSResolver( xmlDoc.ownerDocument == null ? xmlDoc.documentElement : xmlDoc.ownerDocument.documentElement);
		
		
		var netinfMsg = xmlDoc.evaluate('//InformationObject', xmlDoc, nsResolver, XPathResult.ANY_TYPE, null )
									.iterateNext().textContent;
		var parser = new DOMParser();
		var rdfIO = parser.parseFromString(netinfMsg, "text/xml");
		
		var nsResolver2 = rdfIO.createNSResolver( rdfIO.ownerDocument == null ? rdfIO.documentElement : rdfIO.ownerDocument.documentElement);
		
		// Try to get attributeValue
		var attributeValue = "";
		try {
			var clist = rdfIO.getElementsByTagName('netinf:'+attribute)[0].childNodes;
			for(node in clist) {
				if(clist[node].tagName == 'netinf:attributeValue') {
					attributeValue = clist[node].firstChild.nodeValue;
					break;
				}
			}
		} catch(e) {
			Components.utils.reportError("Found no " + attribute + " attribute!");
		}
		
		// If attributeValue is empty, try to get encrypted_content
		var encryptedContent = "";
		if (attributeValue == "") {
			try {
				encryptedContent = rdfIO.evaluate('//netinf:encrypted_content', rdfIO, nsResolver2, XPathResult.ANY_TYPE, null ).iterateNext().textContent;
			} catch(e) {
				Components.utils.reportError("Found no encrypted content!");
			}
		}		
		
		// evaluate attributeValue
		if(attributeValue != "") {
			// Chop off "String:" in the attributeValue
			pattern = /String:(.*)/;
			pattern.exec(attributeValue);
			attributeValue = RegExp.$1;
			this.log("Found " + attribute + " attribute - using " + attributeValue + " for " + strIdentifier);	
			alert("Found " + attribute + " attribute - using " + attributeValue + " for " + strIdentifier);
			return attributeValue;
		}

		// evaluate encryptedContent
		if(encryptedContent != "") {
			Components.utils.reportError("Found no " + attribute + " attribute but IO has encrypted_content - perhaps you don't have permission to contact " + strIdentifier + " via e-mail?");	
			alert("Found no " + attribute + " attribute but IO has encrypted_content - perhaps you don't have permission to contact " + strIdentifier + " via e-mail?");
			return "";
		}

		// nothing found		
		Components.utils.reportError("Found no " + attribute + " attribute or encrypted_content in IO - it is very likely that " + strIdentifier + " doesn't have an e-mail address");		
		alert("Found no " + attribute + " attribute or encrypted_content in IO - it is very likely that " + strIdentifier + " doesn't have an e-mail address");	
		return "";
		
	}
}
   
// could use document.getElementById("msgcomposeWindow") instead of window  
window.addEventListener( "compose-send-message", InBird.replaceAddress, true );
