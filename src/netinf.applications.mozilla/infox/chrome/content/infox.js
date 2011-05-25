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

/* Central Logging Class. For now with just two levels: info and error.
 */
var LOG = {
	info: function(strMessage) {
		var consoleService	= 	Components.classes["@mozilla.org/consoleservice;1"]
								.getService(Components.interfaces.nsIConsoleService);
		consoleService.logStringMessage("InFox: " + strMessage);
	},
	
	error: function(strMessage) {
		Components.utils.reportError("InFox: " + strMessage);
		alert(strMessage);
	}
};

/* The main InFox (NetInf Firefox Extension) Class. Contains all NetInf related functionality.
 */
var InFox = {
		
	/* Is called on Firefox startup. Registers several observers.
	 */
	startup: function() {		
		LOG.info("initializing");
		
		gBrowser.addEventListener("DOMContentLoaded", this.registerClickObserver, true);
		gBrowser.addEventListener("DOMTitleChanged", this.registerClickObserver, true);
		gBrowser.addEventListener("contextmenu", this.checkContextMenu, true);
		
		this.init();
	},
	
	/* Initializes the InFox object (initializes all preferences).
	 */
	init: function() {
		this.prefManager	= Components.classes["@mozilla.org/preferences-service;1"]
								.getService(Components.interfaces.nsIPrefService)
         						.getBranch("extensions.infox.");
		
		this.prefManager.QueryInterface(Components.interfaces.nsIPrefBranch2);
		this.prefManager.addObserver("", this, false);
		
		this.protocol_name 	= "ni:";
		
		this.SERVER			= this.prefManager.getCharPref("server");
		this.PORT			= this.prefManager.getCharPref("port");
		
		this.USERNAME		= this.prefManager.getCharPref("username");
		this.PRIVATEKEY		= this.prefManager.getCharPref("password");
		
		this.auto_open 		= this.prefManager.getBoolPref("autoopen");
		this.colorize_links	= this.prefManager.getBoolPref("colorizelinks");
		
		this.unrescolor		= this.prefManager.getCharPref("unrescolor");	
		this.rescolor		= this.prefManager.getCharPref("rescolor");
		
		this.GPJobId		= "";
		
		this.RESTBEHAVIOR	= this.prefManager.getBoolPref("restbehavior");
		
		this.updateUI();
	},
	
	/* Preference Observer
	 * 
	 * @param aSubject is the nsIPrefBranch we're observing
	 * @param aTopic is the triggered event
	 * @param aData is the name of the pref that's been changed (relative to aSubject)
	 */
	observe: function(aSubject, aTopic, aData)
	{
	  if(aTopic != "nsPref:changed") return;
	  //LOG.info("preferences changed. Reinitializing.");
	  this.init();
	},
	
	/* Mouse Click Observer. Checks whether a NetInf link is clicked or not.
	 */
	registerClickObserver: function(event) {
		var doc = event.originalTarget;
		doc.addEventListener("click", InFox.getURI, true);
		InFox.init();
	},
	
	/* ContextMenu Observer. Checks whether a NetInf link's context menu is shown or not.
	 */
	checkContextMenu: function(event) {
		// check if the link contains a NetInf URI (ni:)
		var pattern = /^ni:(.*)/;
		var patternfound = false;
		
		var isNetInfAnchor = event.target instanceof HTMLAnchorElement && (pattern.test(event.target.getAttributeNode("href").nodeValue) || pattern.test(event.target.getAttributeNode("ni").nodeValue))
	
		document.getElementById("infox-anchor-contextmenu").hidden = !isNetInfAnchor;
		
	},
	
	/* Updates the user interface. Does stuff like styling links and counting unresolved NetInf-links.
	 */
	updateUI: function () {
		
		var allLinks	= content.document.getElementsByTagName("a"),
			style		= content.document.getElementById("netinfox-style"),
			head 		= content.document.getElementsByTagName("head")[0];
		
		foundLinks = 0;
		
		for (var i=0, il=allLinks.length; i<il; i++) {

			// Get all NetInf links
			var netinfLink		= allLinks[i];
			var pattern			= /^ni:(.*)/;
			var patternfound	= false;
			
			try {
				patternfound = patternfound || pattern.test(netinfLink.getAttributeNode("href").nodeValue);
				patternfound = patternfound || pattern.test(netinfLink.getAttributeNode("ni").nodeValue);
			} catch (e) {
				patternfound = false;
			}
			
			if (!style) {
				style	= content.document.createElement("link");
				
				style.id	= "netinfox-style";
				style.type	= "text/css";
				style.rel	= "stylesheet";
				style.href	= "chrome://infox/skin/skin.css";
				
				head.appendChild(style);
			}
			
			if (patternfound) {
				// Style as NetInf link
				if (this.colorize_links)
				{
					//LOG.info("Styling link");
					netinfLink.className = "netinf-selected";
					netinfLink.style.background = this.unrescolor + " url(\"chrome://infox/skin/oni16.png\") no-repeat";
				}
				
				if(this.RESTBEHAVIOR || true){
					netinfLink.href = "#";
					netinfLink.setAttribute('onclick', 'alert("blabla")'); 
				}
								
				foundLinks++;
			}
		}
		
		// Statusbar output
		if (foundLinks == 0) {
			document.getElementById('infox-statusbar-text').label = "No unresolved NetInf links";
		}
		else {
			document.getElementById('infox-statusbar-text').label = foundLinks + " unresolved NetInf links";
		}
		
		try {
			// GP Toolbar
			document.getElementById('infox-gp-target').disabled = true;
			document.getElementById('infox-gp-switchdevice-button').disabled = true;
		} catch (e) {
			//LOG.info("Please add the GP-bar under 'View -> Toolbars -> Customize...' to enable GP functionality.");
		}
		
	},
	
	/* Executed on click, checks whether target of the click is a NetInf link, or not.
	 * On left-click NetInf URIs are transformed to HTTP URIs.
	 * 
	 * @param event Event (MouseClick)
	 */
	getURI: function(event) {
		//just do that when the left Mouse button is clicked either on a link or on the context menu
		if ((event.which == 1) || (event.onLink)) {
		
			if (event.target instanceof HTMLAnchorElement) {
			
				// check if the link contains a NetInf URI (ni:)
				var pattern = /^ni:(.*)/;
				var patternfound = false;
				var strIdentifier;
				
				// check id- and href attribute. Resolved links just have the id left to distinguish them from usual links
				if (event.target.getAttributeNode("id") != null) {
					patternfound = pattern.test(event.target.getAttributeNode("id").nodeValue);
					pattern.exec(event.target.getAttributeNode("id").nodeValue);
					strIdentifier = RegExp.$1;
				}
				else if (event.target.getAttributeNode("ni") != null) {
					patternfound = pattern.test(event.target.getAttributeNode("ni").nodeValue);
					pattern.exec(event.target.getAttributeNode("ni").nodeValue);
					strIdentifier = RegExp.$1;
				}
				else {
					patternfound = pattern.test(event.target.getAttributeNode("href").nodeValue);
					pattern.exec(event.target.getAttributeNode("href").nodeValue);
					strIdentifier = RegExp.$1;
				}
				if(patternfound) {				
				// ... and fire! Get that Locator from our InformationObject
					var showinfo = false;
					if (event.onLink)
						showinfo = true;
					InFox.sendRequest(strIdentifier, event.target, showinfo);	
				}
			} else {
				// do nothing
			}
			
			
		} else { // neither context menu nor left click
			return;
		}
	},
	
	/* Sends a XMLHttpRequest to a NetInf node requesting the IO with Identifier strIdentifier.
	 * 
	 * @param 	strIdentifier String IO Identifier
	 * @param	htmlAnchor HTMLAnchorElement Link that was clicked 
	 */
	sendRequest: function(strIdentifier, htmlAnchor, showinfo) {
		
		var http	= new XMLHttpRequest();
		var url 	= 'http://' + this.SERVER + ':' + this.PORT;
	    var params 	=	"<?xml version='1.0' encoding='UTF-8'?>\n" +
						"<RSGetRequest>\n" +
						"\t<SerializeFormat>RDF</SerializeFormat>\n" +
						"\t<Identifier>ni:" + strIdentifier + "</Identifier>\n" +
						"\t<UserName>" + this.USERNAME + "</UserName>\n" +
						"\t<PrivateKey>" + this.PRIVATEKEY + "</PrivateKey>\n" +
						"\t<FetchAllVersions>true</FetchAllVersions>\n" +
						// TODO: Do we need to download the BinaryObject? This should be true in case we want to load it anyway
						"\t<DownloadBinaryObject>true</DownloadBinaryObject>\n" +
						"</RSGetRequest>";
	    
		LOG.info("Requesting " + strIdentifier + " from <" + url + ">"); //+ ":\n\n" + params);
		
	    http.open("POST", url, true);
		
		//Send the proper header information along with the request
	    http.setRequestHeader("Content-type", "text/xml");
	    http.setRequestHeader("Content-length", params.length);
	    http.setRequestHeader("Connection", "close");
	
	    http.send(params);
	    http.onreadystatechange = function() {//Call this function when the state changes.
			var msg = "";
			switch (http.readyState) {
				case 1:
					msg = "Request Object created, but not initalized";
					break;
				case 2:
					msg = "Request is initialized and uploading";
					break;
				case 3:
					msg = "Request sent and partial data is loaded";
					break;
				case 4:
					if (http.status == 200) {
						msg = "Request is successfully completed";
					} else {
						msg = "Request unsuccessfull. HTTP status: " + http.status
						LOG.info(msg);
						//return;
					}
					break;
				default:
					msg = "undefined";
			}
	    	LOG.info(msg);
			
	    	if(http.readyState == 4 && http.status == 200) {
	    		//LOG.info(http.responseText); // ugly output
	    		if (showinfo == false) {
	    			InFox.handleIO(http.responseXML, htmlAnchor)
	    		} else {
	    			InFox.showIO(http.responseXML);
	    		}
	    	} else if (http.readyState == 4 && http.status != 200) {
			content.location.href = "http://www.netinf.org/infoxerror/";		
	    	}
	    };

	    var requestTimer = setTimeout(function() {
       	    http.abort();
            LOG.info("Request Timeout");
     	    }, 5000);
		
	    //http.send(params);
	
	},

	/* Called when requested IO arrives. Identifies the IO's type and delegates handling of the IO.
	 * 
	 * @param informationObject XMLDocument
	 * @param htmlAnchor HTMLAnchorElement
	 */
	handleIO: function(informationObject, htmlAnchor) {
		
		if (informationObject.getElementsByTagName("ErrorMessage").length > 0) {
			LOG.error(informationObject.getElementsByTagName("ErrorMessage")[0].firstChild.nodeValue);
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
		var type = rdfIO.evaluate('//netinf:ioType', rdfIO, nsResolver2, XPathResult.ANY_TYPE, null )
		.iterateNext().textContent;
		
		//LOG.info("Received InformationObject (Type: " + type + "):\n\n" + netinfMsg);
		
		switch (type) {
			case "netinf.common.datamodel.rdf.InformationObjectRdf":
				this.showIO(informationObject);
				break;
			case "netinf.common.datamodel.rdf.DataObjectRdf":
				this.updateURI(rdfIO,"netinf:http_url" , htmlAnchor, "");
				break;
			case "netinf.common.datamodel.rdf.identity.IdentityObjectRdf":
				this.updateURI(rdfIO,"netinf:e_mail_address" , htmlAnchor, "mailto:");
				break;
			default:
				LOG.info("Unkown Information Object Type \'" + type + "\'");
		}
	
	},
	
	/* Called when a DataObject was requested. Updates the URI of an HTMLAnchorElement
	 * with the new URI from the IO.
	 * 
	 * @param rdfIO XMLDocument The InformationObject in XML formar
	 * @param netinfAttribute String The attribute which should be used to update the URI
	 * @param htmlAnchor HTMLAnchorElement The clicked HTML anchor which will be updated
	 * @param hrefprefix String Prefix, like mailto:, which will be inserted when the anchor will be updated
	 */
	updateURI: function(rdfIO, netinfAttribute, htmlAnchor, hrefprefix) {
		// Open the link, if auto_open is enabled and we got a locator
		if (htmlAnchor instanceof HTMLAnchorElement) {
			
			if (rdfIO.getElementsByTagName(netinfAttribute).length == 0) {
				LOG.error("InformationObject contains no Attribute:" + netinfAttribute);
			}
			
			if (netinfAttribute == "netinf:http_url") {
				// Let's select the best locator available
				var newhref = this.selectLocator(rdfIO);
			} else {
				var nsResolver = rdfIO.createNSResolver( rdfIO.ownerDocument == null ? rdfIO.documentElement : rdfIO.ownerDocument.documentElement);
				var newhref = rdfIO.evaluate( '//' + netinfAttribute + '/netinf:attributeValue', rdfIO, nsResolver, XPathResult.ANY_TYPE, null )
								.iterateNext().textContent;
			}
			
			// Chop off "String:" in the attributeValue
			pattern = /String:(.*)/;
			pattern.exec(newhref);
			newhref = hrefprefix + RegExp.$1;
			
			LOG.info("New URI is <" + newhref + ">");
			
			// Okay, let's see if we got an gp: link here
			pattern = /gp:(.*)/;
			isgplink = pattern.test(newhref);
			if (isgplink) {
				// This is a GP link, so do a TCStartRequest
				pattern.exec(newhref);
				strSource = RegExp.$1;
				
				this.tcStartRequest(strSource);
			} else {		
				// No GP-link, so just update the anchor
				if (this.auto_open == true) {			
					//direct redirect to new URI
					LOG.info("redirect to new URI from InformationObject");
					content.location.href = newhref;
				}
				else {
					//just update the link's URI (and style it if enabled)
					htmlAnchor.getAttributeNode("href").nodeValue = newhref;
					if (this.colorize_links) {
						htmlAnchor.className = "netinf-selected";
						htmlAnchor.style.background = this.rescolor + " url(\"chrome://infox/skin/oni16.png\") no-repeat";
					}
					this.updateUI();
					LOG.info("just update the URI from InformationObject");
				}
			}
		}
		else {
			LOG.error("Target is not a HTML anchor element");
		}
	},
	
	/* Selects a locator from a XML branch containing locators from an IO
	 * 
	 * @param xmlLocatorBranch XMLDocument This should contain (at least) all locators from the original IO
	 */
	selectLocator: function(xmlLocatorBranch) {
	
		var xmlDoc = xmlLocatorBranch;
		
		var arrLocators = [];
		var ploc;
		var loc;

		var nsResolver = xmlDoc.createNSResolver( xmlDoc.ownerDocument == null ? xmlDoc.documentElement : xmlDoc.ownerDocument.documentElement);
		var itPrivilegedLocators = xmlDoc.evaluate('//netinf:http_url/netinf:cache/../netinf:attributeValue', xmlDoc, nsResolver, XPathResult.ANY_TYPE, null );
		var itLocators = xmlDoc.evaluate('//netinf:http_url[not(./netinf:cache)]/netinf:attributeValue', xmlDoc, nsResolver, XPathResult.ANY_TYPE, null );
		
		// Firstly push the privileged/cached locators
		while (ploc = itPrivilegedLocators.iterateNext()) {
	    	arrLocators.push(ploc)
		}		
		
		// Secondly push the unprivileged locators;
		while (loc = itLocators.iterateNext()) {
	    	arrLocators.push(loc)
		}
		
		for (var i in arrLocators) {
			LOG.info("Locator attribute found <" + arrLocators[i].textContent + ">");
		}
		
		// FIXME: Okay, this is a very simple selection. We always choose the first (privileged) locator in the list.
		var selectedLocator = arrLocators[0].textContent;

		return selectedLocator;		
	},
	
	/* Validates the IO. Checks if the author can be trusted and if all attributes are valid.
	 * To be implemented!
	 * 
	 * @param rdfIO the IO to verify
	 */
	validateIO: function(rdfIO) {
		// TODO: To be implemented
	},

	/* Shows the IO Dialog, containing all first level attributes
	 *
	 * @param informationObject the RDF/XML formatted InformationObject
	 */
	showIO: function(informationObject) {
		
		if (informationObject.getElementsByTagName("ErrorMessage").length > 0) {
			LOG.error(informationObject.getElementsByTagName("ErrorMessage")[0].firstChild.nodeValue);
			return;
		}
		
		// The actually transfered IO looks like a RDF packaged into an XML. So we unpack it. I am not sure if this is necessary...		
		var xmlDoc = informationObject;
		var nsResolver = xmlDoc.createNSResolver( xmlDoc.ownerDocument == null ? xmlDoc.documentElement : xmlDoc.ownerDocument.documentElement);
		var netinfMsg = xmlDoc.evaluate('//InformationObject', xmlDoc, nsResolver, XPathResult.ANY_TYPE, null )
								.iterateNext().textContent;
		
		//LOG.info(netinfMsg); //ugly output
		
		var parser = new DOMParser();
		var rdfIO = parser.parseFromString(netinfMsg, "text/xml");
	
		LOG.info("Show the InformationObject info dialog");
		var params = {inn:{io:rdfIO, enabled:true}, out:null};       
		
		window.openDialog("chrome://infox/content/informationObject.xul", "", "chrome, centerscreen", params).focus();
	  
		if (params.out) {
			// User clicked ok. Process changed arguments; e.g. write them to disk or whatever
			return;
		}
		else {
			// User clicked cancel. Typically, nothing is done here.
		}
	},
	
	/* Starts a TransferRequest (using Generic Path Integration)
	 * 
	 * @param strSource the source address
	 */
	tcStartRequest: function(strSource) {
		
		var http	= new XMLHttpRequest();
		var url 	= 'http://' + this.SERVER + ':' + this.PORT;
	    var params 	=	"<?xml version='1.0' encoding='UTF-8'?>\n" +
						"<TCStartTransferRequest>\n" +
						"\t<SerializeFormat>RDF</SerializeFormat>\n" +
						"\t<Source>" + strSource + "</Source>\n" +
						"</TCStartTransferRequest>";
	    
		LOG.info("Sending TCStartRequest to NetInf Node <" + url + ">"); //+ ":\n\n" + params);
		
	    http.open("POST", url, true);
		
		//Send the proper header information along with the request
	    http.setRequestHeader("Content-type", "text/xml");
	    http.setRequestHeader("Content-length", params.length);
	    http.setRequestHeader("Connection", "close");
	
	    http.send(params);
	    http.onreadystatechange = function() {//Call this function when the state changes.
			var msg = "";
			switch (http.readyState) {
				case 1:
					msg = "Request Object created, but not initalized";
					break;
				case 2:
					msg = "Request is initialized and uploading";
					break;
				case 3:
					msg = "Request sent and partial data is loaded";
					break;
				case 4:
					if (http.status == 200) {
						msg = "Request is successfully completed";
					} else {
						msg = "Request unsuccessfull. HTTP status: " + http.status
						LOG.info(msg);
						return;
					}
					break;
				default:
					msg = "undefined";
			}
	    	LOG.info(msg);
			
	    	if(http.readyState == 4 && http.status == 200) {
	    		//LOG.info(http.responseText); // ugly output
	    		var rdfIO = http.responseXML;
				var nsResolver = rdfIO.createNSResolver( rdfIO.ownerDocument == null ? rdfIO.documentElement : rdfIO.ownerDocument.documentElement);
				InFox.GPJobId = rdfIO.evaluate( '//JobId', rdfIO, nsResolver, XPathResult.ANY_TYPE, null )
								.iterateNext().textContent;
				LOG.info("GP Job ID is: " + InFox.GPJobId);
				
				// Update GP Toolbar
				document.getElementById('infox-gp-target').disabled = false;
				document.getElementById('infox-gp-switchdevice-button').disabled = false;
	    	} else if (http.readyState == 4 && http.status != 200) {
	    		LOG.error(msg);
	    	}
	    };

            var requestTimer = setTimeout(function() {
            http.abort();
            LOG.info("Request Timeout");
            }, 5000);
		
	    //http.send(params);
	},
	
	/* Requests a Change Transfer (using Generic Path Integration) for the current Transfer Job.
	 */
	tcChangeRequest: function() {
		
		var strDestination = document.getElementById('infox-gp-target').value;
		
		var http	= new XMLHttpRequest();
		var url 	= 'http://' + this.SERVER + ':' + this.PORT;
	    var params 	=	"<?xml version='1.0' encoding='UTF-8'?>\n" +
						"<TCChangeTransferRequest>\n" +
						"\t<SerializeFormat>RDF</SerializeFormat>\n" +
						"\t<Proceed>true</Proceed>\n" +
						"\t<NewDestination>" + strDestination + "</NewDestination>\n" +
						"\t<JobId>" + this.GPJobId + "</JobId>\n" +
						"</TCChangeTransferRequest>";
	    
		LOG.info("Sending TCChangeRequest for job <" + this.GPJobId + ">" + "\n" + "with new destination " + strDestination + " to NetInf Node <" + url + ">" + ":\n\n" + params);
		
	    http.open("POST", url, true);
		
		//Send the proper header information along with the request
	    http.setRequestHeader("Content-type", "text/xml");
	    http.setRequestHeader("Content-length", params.length);
	    http.setRequestHeader("Connection", "close");
	
	    http.send(params);
	    http.onreadystatechange = function() {//Call this function when the state changes.
			var msg = "";
			switch (http.readyState) {
				case 1:
					msg = "Request Object created, but not initalized";
					break;
				case 2:
					msg = "Request is initialized and uploading";
					break;
				case 3:
					msg = "Request sent and partial data is loaded";
					break;
				case 4:
					if (http.status == 200) {
						msg = "Request is successfully completed";
					} else {
						msg = "Request unsuccessfull. HTTP status: " + http.status
						LOG.info(msg);
						return;
					}
					break;
				default:
					msg = "undefined";
			}
	    	LOG.info(msg);
			
	    	if(http.readyState == 4 && http.status == 200) {
	    		LOG.info(http.responseText); // ugly output
				// to something usefull
	    	} else if (http.readyState == 4 && http.status != 200) {
	    		LOG.error(msg);
	    	}
	    };

            var requestTimer = setTimeout(function() {
            http.abort();
            LOG.info("Request Timeout");
            }, 5000);
		
	    //http.send(params);
	},

};

// When Firefox is loaded, we initialize the InFox instance
window.addEventListener("load", function(e) { InFox.startup(); }, false);
//window.addEventListener("unload", function(e) { InFox.shutdown(); }, false);
