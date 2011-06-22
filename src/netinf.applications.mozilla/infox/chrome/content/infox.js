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
		var consoleService = Components.classes["@mozilla.org/consoleservice;1"].getService(Components.interfaces.nsIConsoleService);
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
		this.prefManager = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService).getBranch("extensions.infox.");
		
		this.prefManager.QueryInterface(Components.interfaces.nsIPrefBranch2);
		this.prefManager.addObserver("", this, false);
		
		this.protocol_name 	= "ni:";
		
		this.SERVER			= this.prefManager.getCharPref("server");
		this.PORT			= this.prefManager.getCharPref("port");
		
		this.USERNAME		= this.prefManager.getCharPref("username");
		this.PRIVATEKEY		= this.prefManager.getCharPref("password");
		
		this.auto_open 		= this.prefManager.getBoolPref("autoopen");
		this.colorize_links	= this.prefManager.getBoolPref("colorizelinks");
		
		this.restbehavior	= this.prefManager.getBoolPref("restbehavior");
		this.RESTPORT		= this.prefManager.getCharPref("restport");
		
		this.unrescolor		= this.prefManager.getCharPref("unrescolor");	
		this.rescolor		= this.prefManager.getCharPref("rescolor");
		
		this.GPJobId		= "";
		
		// load jQuery library
		var jsLoader = Components.classes["@mozilla.org/moz/jssubscript-loader;1"].getService(Components.interfaces.mozIJSSubScriptLoader);
		jsLoader.loadSubScript("chrome://infox/content/jquery/jquery-1.3.2.min.js");
		jQuery.noConflict();
		docContext = window.content.document;
		
		// other settings
		TIMEOUT_TIME = 5000; // 5 seconds
		ERROR_PAGE = "http://www.netinf.org/infoxerror/";
		
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
	  if (aTopic != "nsPref:changed") {
		  return;
	  }
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
		
		var isNetInfAnchor = event.target instanceof HTMLAnchorElement && (pattern.test(event.target.getAttributeNode("href").nodeValue) || pattern.test(event.target.getAttributeNode("ni").nodeValue));
	
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
				// main style sheed
				style	= content.document.createElement("link");
				style.id	= "netinfox-style";
				style.type	= "text/css";
				style.rel	= "stylesheet";
				style.href	= "chrome://infox/skin/skin.css";
				head.appendChild(style);
				
				// add style for unresolved links
				var unresColorStyle = content.document.createElement('style');
				unresColorStyle.type = 'text/css';
				unresColorStyle.innerHTML = '.unresolved { background-color: ' + this.unrescolor + '; }';
				head.appendChild(unresColorStyle);
		
				// add style for resolved links
				var resColorStyle = content.document.createElement('style');
				resColorStyle.type = 'text/css';
				resColorStyle.innerHTML = '.resolved { background-color: ' + this.rescolor + '; }';
				head.appendChild(resColorStyle);
			}
			
			if (patternfound) {
				// Style as NetInf link
				if (this.colorize_links) {
					netinfLink.className = "netinf-selected unresolved";
					//netinfLink.style.backgroundColor = this.unrescolor;
				}			
				foundLinks++;
			}
		}
		
		// Statusbar output
		if (foundLinks === 0) {
			document.getElementById('infox-statusbar-text').label = "No unresolved NetInf links";
		} else {
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
				if (event.target.getAttributeNode("id") !== null) {
					patternfound = pattern.test(event.target.getAttributeNode("id").nodeValue);
					pattern.exec(event.target.getAttributeNode("id").nodeValue);
					strIdentifier = RegExp.$1;
				} else if (event.target.getAttributeNode("ni") !== null) {
					patternfound = pattern.test(event.target.getAttributeNode("ni").nodeValue);
					pattern.exec(event.target.getAttributeNode("ni").nodeValue);
					strIdentifier = RegExp.$1;
				} else {
					patternfound = pattern.test(event.target.getAttributeNode("href").nodeValue);
					pattern.exec(event.target.getAttributeNode("href").nodeValue);
					strIdentifier = RegExp.$1;
				}
				if(patternfound) {				
					// ... and fire! Get that Locator from our InformationObject
					var showinfo = false;
					if (event.onLink) {
						showinfo = true;
					}

					// request found URI
					if (InFox.restbehavior) { // request via REST
						LOG.info("Sending request via RESTful interface");
						InFox.sendRESTRequest(strIdentifier, event.target, showinfo);
					} else { // request via NetInfMessage (standard)
						LOG.info("Sending request via standard HTTP interface (NetInfMessage");
						InFox.sendNetInfMessageRequest(strIdentifier, event.target, showinfo);
					}
				}
			} // else do nothing
			
		} else { // neither context menu nor left click
			return;
		}
	},
	
	/* Sends a HTTP request via the REST interface and handles the returned IO
	 * 
	 * @param strIdentifier String IO Identifier
	 * @param htmlAnchor HTMLAnchorElement Link that was clicked
	 * @param showinfo Showing the IO in a dialog or not
	 */
	sendRESTRequest: function(strIdentifier, htmlAnchor, showinfo) {
		var client	= new XMLHttpRequest();
		var url = 'http:/' + this.SERVER + ':' + this.RESTPORT + '/io/ni:' + strIdentifier;
		
		client.open("GET", url, true);
		client.setRequestHeader("Content-type", "text/plain");
		client.send(null);
		client.onreadystatechange = function() {
			if(client.readyState == 4 && client.status == 200) {
				LOG.info('(REST ) IO received');
				var ioXML = client.responseXML;
				
				// if rightclick -> open InfoDialog
				if (showinfo === true) {
					InFox.showIO(ioXML, true);
					return;
				} // else continue
				
				// check IO type and handle it
				var ioType = jQuery(ioXML).find('netinf\\:ioType').text();
				LOG.info('(REST ) type is ' + ioType);
				switch (ioType) {
					case 'netinf.common.datamodel.rdf.InformationObjectRdf':
						LOG.info('(REST ) handling like an IO');
						InFox.handleRESTIO(ioXML, strIdentifier, htmlAnchor);
						break;
					case 'netinf.common.datamodel.rdf.DataObjectRdf':
						LOG.info('(REST ) handling like a DO');
						InFox.handleRESTDO(strIdentifier, htmlAnchor);
						break;
					case 'netinf.common.datamodel.rdf.identity.IdentityObjectRdf':
						LOG.info('(REST ) handling like an iDO');
						InFox.handleRESTiDO(ioXML, strIdentifier, htmlAnchor);
						break;
					default:
						LOG.info("(REST ) Unkown Information Object Type \'" + ioType + "\'");
						break;
				}
				
			} else if (client.readyState == 4 && client.status != 200) {
				LOG.info("(REST ) No IO received...");
				client.abort();
				InFox.goToErrorPage();		
	    	}
		};
		
		// set timeout
		var httpTimeout = setTimeout(function(){
			LOG.info("(REST ) request timed out after " + TIMEOUT_TIME + "seconds...");
			client.abort();
		}, TIMEOUT_TIME);

	},
	
	/*
	 * Forwards to the specified error page
	 */
	goToErrorPage: function() {
		LOG.info("Forwarding to http://www.netinf.org/infoxerror/");
		content.location.href = ERROR_PAGE;
	},
	
	/*
	 * Handles the via REST requested IO
	 */
	handleRESTIO: function(ioXML, strIdentifier, htmlAnchor) {
		// get name of the IO
		var $ioName = jQuery(ioXML).find('netinf\\:name');
		if($ioName.length === 0) {
			// no name found, take name of link
			ioName = htmlAnchor.innerHTML;
		} else {
			ioName = this.removeStringPrefix($ioName.text());
		}
		// get description
		var $ioDesc = jQuery(ioXML).find('netinf\\:description');
		if($ioDesc.length === 0) {
			// no description found
			ioDesc = '';
		} else {
			ioDesc = this.removeStringPrefix($ioDesc.text());
		}
		// build popup window
		var popupHtml = '<div id="RestPopup">';  
		popupHtml += '<h3>' + ioName + '</h3>';
		popupHtml += '<a id="RestPopupClose">x</a>'; 
		if (ioDesc !== '') {
			popupHtml += '<div id="RestDescription">' + ioDesc + '</div>';
		} 
		popupHtml += '<div id="RestPopupContent"></div>';
		popupHtml += '</div>';
		
		// remove if already existing anywhere (e.g. clicking again) ^^
		jQuery('#RestPopup', docContext).remove();
		
		// append popup
        jQuery('body', docContext).append(popupHtml);
        
        // element position
        var $aElement = jQuery('a[href=ni:' + strIdentifier + ']', docContext);
        var position = $aElement.position();
        var aElemWidth = $aElement.width();
        
        // apply position
        var windowWidth = document.documentElement.clientWidth;  
        var windowHeight = document.documentElement.clientHeight;
        var popupHeight = jQuery('#RestPopup', docContext).height(); 
        var popupWidth = jQuery('#RestPopup', docContext).width();
        
        var leftPos = 0;
        if (position.left < windowWidth/2) { // link is on left side
        	leftPos = position.left + aElemWidth;
        } else { // link is on right side
        	leftPos = position.left - popupWidth + aElemWidth;
        }
        
        jQuery('#RestPopup', docContext).css({  
        	"position": "absolute",  
        	"top": position.top - popupHeight,  
        	"left": leftPos  
        	}); 
        
        // register close event
        jQuery('#RestPopupClose', docContext).click(function(){
        	jQuery('#RestPopup', docContext).remove();
        	});  
		
		// add referenced DOs, if existing
		jQuery(ioXML).find('netinf\\:referenced_do').each(function(){
			LOG.info("(REST ) adding referenced DOs");
			var $uriOfDO = jQuery(this).find('netinf\\:attributeValue');
			var uriOfDO = InFox.removeStringPrefix($uriOfDO.text());
			InFox.addDOreference(uriOfDO);
		});
	},		
	
	/*
	 * Adds referenced DOs to the popup of the IO
	 */
	addDOreference: function(uriOfDO) {
		LOG.info("(REST ) requesting " + uriOfDO);
		var client	= new XMLHttpRequest();
		var url = 'http:/' + this.SERVER + ':' + this.RESTPORT + '/io/' + uriOfDO;
		
		client.open("GET", url, true);
		client.setRequestHeader("Content-type", "text/plain");
		client.send(null);
		client.onreadystatechange = function() {
			if (client.readyState == 4 && client.status == 200) {
				LOG.info('(REST ) referenced DO received');
				var doXML = client.responseXML;
				// get content type
				var contentType = InFox.getAttributeValue(doXML, 'netinf\\:content_type');
				if (contentType === null) {
					contentType = '';
				}
				
				// build entry
				var spanClass = 'default';
				if (contentType.indexOf("video") != -1) {
					spanClass = 'video';
				} else if (contentType.indexOf("image") != -1) {
					spanClass = 'image';
				} else if (contentType.indexOf("pdf") != -1) {
					spanClass = 'pdf';
				}
				var linkToDO = 'http:/' + InFox.SERVER + ':' + InFox.RESTPORT + '/' + uriOfDO;
				var doEntry = '<p><span class="' + spanClass + '"><a href="' + linkToDO + '">Download as ' + contentType + '</a></span></p>';
				
				// append at popup
				jQuery('#RestPopupContent', docContext).append(doEntry);
			} else if (client.readyState == 4 && client.status != 200) {
				LOG.info("(REST ) No DO received...");
				client.abort();
	    	}
		};
		
		// set timeout
		var httpTimeout = setTimeout(function(){
			LOG.info("(REST ) request timed out after " + TIMEOUT_TIME + "seconds...");
			client.abort();
		}, TIMEOUT_TIME);
	},
	
	/*
	 * Returns the value of a given attribute in a XML document 
	 */
	getAttributeValue: function(xml, attribute) {
		var $value = jQuery(xml).find(attribute);
		if($value.length === 0) {
			// no value found, return null
			return null;
		} else {
			return this.removeStringPrefix($value.text());
		}
	},
	
	/*
	 * Handles the via REST requested DO
	 */
	handleRESTDO: function(strIdentifier, htmlAnchor) {
		LOG.info('(REST ) building RESTified url - RESTful interface will choose appropriate locator');
		var redirect = 'http:/' + this.SERVER + ':' + this.RESTPORT + '/ni:' + strIdentifier;
		this.handleRedirect(redirect, htmlAnchor);
	},
	
	/*
	 * Handles the via REST requested iDO
	 */
	handleRESTiDO: function(idoXML, strIdentifier, htmlAnchor) {
		LOG.info('(REST ) building url with the email of the iDO');
		var email = jQuery(idoXML).find('netinf\\:e_mail_address');
		var redirect = '';
		if(email.length !== 0) { // if email entry exists
			redirect = 'mailto:' + this.removeStringPrefix(email);
		} else {
			LOG.info('(REST ) email does not exist, redirecting to iDO in XML-format');
			redirect = 'http:/' + this.SERVER + ':' + this.RESTPORT + '/io/ni:' + strIdentifier;
		}
		this.handleRedirect(redirect, htmlAnchor);
	},
	
	/*
	 * Handles the redirect the specified href, or refreshes the UI by that
	 */
	handleRedirect: function(href, htmlAnchor) {
		if (this.auto_open) { // redirect to new URI
			LOG.info("(REST ) redirecting to " + href);
			content.location.href = href;
		} else {
			LOG.info("(REST ) updating UI with new link: " + href);
			// just update the link's URI (and style it if enabled)
			htmlAnchor.getAttributeNode("href").nodeValue = href;
			if (this.colorize_links) {
				htmlAnchor.className = "netinf-selected resolved";
				//htmlAnchor.style.backgroundColor = this.rescolor;
			}
			
			this.updateUI();
		}
	},
	
	/*
	 * Removes the prefix 'String:' of a given value
	 */
	removeStringPrefix: function(text) {
		// Chop off "String:" in the attributeValue
		var pat = /String:(.*)/;
		pat.exec(text);
		text = RegExp.$1;
		return text;
	},
	
	/* Sends a XMLHttpRequest to a NetInf node requesting the IO with Identifier strIdentifier.
	 * 
	 * @param 	strIdentifier String IO Identifier
	 * @param	htmlAnchor HTMLAnchorElement Link that was clicked 
	 * @param showinfo Showing the IO in a dialog or not
	 */
	sendNetInfMessageRequest: function(strIdentifier, htmlAnchor, showinfo) {
		
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
						msg = "Request unsuccessfull. HTTP status: " + http.status;
						LOG.info(msg);
						//return;
					}
					break;
				default:
					msg = "undefined";
					break;
			}
	    	LOG.info(msg);
			
	    	if(http.readyState == 4 && http.status == 200) {
	    		if (showinfo === false) {
	    			InFox.handleIO(http.responseXML, htmlAnchor);
	    		} else {
	    			InFox.showIO(http.responseXML, false);
	    		}
	    	} else if (http.readyState == 4 && http.status != 200) {
	    		InFox.goToErrorPage();		
	    	}
	    };

		// set timeout
		var httpTimeout = setTimeout(function(){
			LOG.info("request timed out after " + TIMEOUT_TIME + "seconds...");
			client.abort();
		}, TIMEOUT_TIME);
		
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
		var netinfMsg = xmlDoc.evaluate('//InformationObject', xmlDoc, nsResolver, XPathResult.ANY_TYPE, null ).iterateNext().textContent;
		
		var parser = new DOMParser();
		var rdfIO = parser.parseFromString(netinfMsg, "text/xml");
		
		var nsResolver2 = rdfIO.createNSResolver( rdfIO.ownerDocument == null ? rdfIO.documentElement : rdfIO.ownerDocument.documentElement);
		var type = rdfIO.evaluate('//netinf:ioType', rdfIO, nsResolver2, XPathResult.ANY_TYPE, null ).iterateNext().textContent;
		
		//LOG.info("Received InformationObject (Type: " + type + "):\n\n" + netinfMsg);
		
		switch (type) {
			case "netinf.common.datamodel.rdf.InformationObjectRdf":
				this.showNetInfWrappedIO(informationObject);
				break;
			case "netinf.common.datamodel.rdf.DataObjectRdf":
				this.updateURI(rdfIO,"netinf:http_url" , htmlAnchor, "");
				break;
			case "netinf.common.datamodel.rdf.identity.IdentityObjectRdf":
				this.updateURI(rdfIO,"netinf:e_mail_address" , htmlAnchor, "mailto:");
				break;
			default:
				LOG.info("Unkown Information Object Type \'" + type + "\'");
				break;
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
			
			if (rdfIO.getElementsByTagName(netinfAttribute).length === 0) {
				LOG.error("InformationObject contains no Attribute:" + netinfAttribute);
			}
			
			if (netinfAttribute == "netinf:http_url") {
				// Let's select the best locator available
				var newhref = this.selectLocator(rdfIO);
			} else {
				var nsResolver = rdfIO.createNSResolver( rdfIO.ownerDocument == null ? rdfIO.documentElement : rdfIO.ownerDocument.documentElement);
				newhref = rdfIO.evaluate( '//' + netinfAttribute + '/netinf:attributeValue', rdfIO, nsResolver, XPathResult.ANY_TYPE, null ).iterateNext().textContent;
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
				if (this.auto_open === true) {			
					//direct redirect to new URI
					LOG.info("redirect to new URI from InformationObject");
					content.location.href = newhref;
				}
				else {
					//just update the link's URI (and style it if enabled)
					htmlAnchor.getAttributeNode("href").nodeValue = newhref;
					if (this.colorize_links) {
						htmlAnchor.className = "netinf-selected resolved";
						// htmlAnchor.style.backgroundColor = this.rescolor;
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
	    	arrLocators.push(ploc);
		}		
		
		// Secondly push the unprivileged locators;
		while (loc = itLocators.iterateNext()) {
	    	arrLocators.push(loc);
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
	showIO: function(informationObject, isWrappedXML) {
		LOG.info("Show the InformationObject info dialog");
		
		if (informationObject.getElementsByTagName("ErrorMessage").length > 0) {
			LOG.error(informationObject.getElementsByTagName("ErrorMessage")[0].firstChild.nodeValue);
			return;
		}

		// The actually transfered IO looks like a RDF packaged into an XML. So we unpack it. I am not sure if this is necessary...		
		var xmlDoc = informationObject;
		
		if (isWrappedXML === false) {
			var nsResolver = xmlDoc.createNSResolver( xmlDoc.ownerDocument == null ? xmlDoc.documentElement : xmlDoc.ownerDocument.documentElement);
			var netinfMsg = xmlDoc.evaluate('//InformationObject', xmlDoc, nsResolver, XPathResult.ANY_TYPE, null).iterateNext().textContent;
		
			var parser = new DOMParser();
			xmlDoc = parser.parseFromString(netinfMsg, "text/xml");
		}
		
		var params = {inn:{io:xmlDoc, enabled:true, isPureXML:isWrappedXML}, out:null};       
		window.openDialog("chrome://infox/content/informationObject.xul", "", "chrome, centerscreen", params).focus();
	  
		if (params.out) {
			// User clicked ok. Process changed arguments; e.g. write them to disk or whatever
			return;
		} // else User clicked cancel. Typically, nothing is done here.

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
						msg = "Request unsuccessfull. HTTP status: " + http.status;
						LOG.info(msg);
						return;
					}
					break;
				default:
					msg = "undefined";
					break;
			}
	    	LOG.info(msg);
			
	    	if(http.readyState == 4 && http.status == 200) {
	    		//LOG.info(http.responseText); // ugly output
	    		var rdfIO = http.responseXML;
				var nsResolver = rdfIO.createNSResolver( rdfIO.ownerDocument == null ? rdfIO.documentElement : rdfIO.ownerDocument.documentElement);
				InFox.GPJobId = rdfIO.evaluate( '//JobId', rdfIO, nsResolver, XPathResult.ANY_TYPE, null ).iterateNext().textContent;
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
						msg = "Request unsuccessfull. HTTP status: " + http.status;
						LOG.info(msg);
						return;
					}
					break;
				default:
					msg = "undefined";
					break;
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
	},
	
	/**
	 * resets the preferences to default values
	 */
	resetPreferences: function() {
		LOG.info("Reset preferences");
		var prefs = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService).getBranch("extensions.infox.");
		
		// very stupid way, but its not possible on an other way in FF3
		prefs.setCharPref('server', 'nn1.testbed.netinf.org');
		prefs.setCharPref('port', '8080');
		prefs.setCharPref('username', 'generic');
		prefs.setCharPref('password', 'generic');
		prefs.setBoolPref('autoopen', true);
		prefs.setBoolPref('colorizelinks', true);
		prefs.setCharPref('unrescolor', '#f5f5f5');
		prefs.setCharPref('rescolor', '#E6EFC2');
		prefs.setBoolPref('restbehavior', true);
		prefs.setCharPref('restport', '8081');
	}

};

// When Firefox is loaded, we initialize the InFox instance
window.addEventListener("load", function(e) { InFox.startup(); }, false);
//window.addEventListener("unload", function(e) { InFox.shutdown(); }, false);

