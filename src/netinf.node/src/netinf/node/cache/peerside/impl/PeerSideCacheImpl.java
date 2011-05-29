package netinf.node.cache.peerside.impl;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.log.demo.DemoLevel;
import netinf.common.security.Hashing;
import netinf.common.utils.Utils;
import netinf.node.cache.peerside.PeerSideCache;
import netinf.node.cache.peerside.PeerSideCacheServer;
import netinf.node.transfer.http.TransferJobHttp;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Implementation of peer-side caching.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public class PeerSideCacheImpl implements PeerSideCache {

	private static final Logger LOG = Logger.getLogger(PeerSideCacheImpl.class);

	private PeerSideCacheServer server;

	/**
	 * Constructor
	 */

	public PeerSideCacheImpl(PeerSideCacheServer server) {

		this.server = server;

	}

	/**
	 * Set the server.
	 * 
	 * @param server
	 */

	public void setServer(PeerSideCacheServer server) {

		this.server = server;

	}

	@Override
	public boolean contains(DataObject dataObject) {

		LOG.trace(null);

		String hash = getHash(dataObject);
		LOG.debug("Hash of dataobject is " + hash);

		if (hash == null)

			return false;

		else
			return server.contains(hash);
	}

	@Override
	public boolean cache(DataObject dataObject) {

		if (!contains(dataObject)) {
			String hash = getHash(dataObject);

			if (hash == null) {
				LOG.info("DataObject has no Hash and will not be cached");
				return false;
			}

			List<Attribute> locators = dataObject
					.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE
							.toString());

			for (Attribute attr : locators) {
				DataInputStream fis = null;
				String url = attr.getValue(String.class);
				try {
					if (url.startsWith("http://")) {
						String destination = getTmpFolder() + File.separator
								+ hash + ".tmp";
						;
						// TODO Implement Transfer service and use it here....
						TransferJobHttp job = new TransferJobHttp(hash, url,
								destination);
						job.startTransferJob();
						fis = new DataInputStream(new FileInputStream(
								destination));
						int skipSize = fis.readInt();
						for (int i = 0; i < skipSize; i++) {
							fis.read();
						}
						byte[] hashBytes = Hashing.hashSHA1(fis);
						IOUtils.closeQuietly(fis);
						if (hash.equalsIgnoreCase(Utils
								.hexStringFromBytes(hashBytes))) {
							LOG.info("Hash of downloaded file is valid: " + url);
							LOG.log(DemoLevel.DEMO,
									"(NODE ) Hash of downloaded file is valid. Will be cached.");

							server.cache(hashBytes, hash);
							addLocator(dataObject);
							deleteTmpFile(destination);

							return true;
						} else {
							LOG.log(DemoLevel.DEMO,
									"(NODE ) Hash of downloaded file is invalid. Trying next locator");
							LOG.warn("Hash of downloaded file is not valid: "
									+ url);
							LOG.warn("Trying next locator");
						}
					}
				} catch (FileNotFoundException ex) {
					LOG.warn("Error downloading:" + url);
				} catch (IOException e) {
					LOG.warn("Error hashing:" + url);
				} catch (Exception e) {
					LOG.warn("Error hashing, but file was OK: " + url);

				} finally {
					IOUtils.closeQuietly(fis);

				}
			}
			LOG.warn("Could not find reliable source to cache: " + dataObject);
			return false;
		} else {

			LOG.log(DemoLevel.DEMO,
					"(NODE ) DataObject has already been cached. Adding locator.");

			addLocator(dataObject);

			return true;
		}

	}

	/**
	 * Gets the hash-value of a DataObject
	 * 
	 * @param d
	 *            the DataObject
	 * @return hash-value of the DO
	 */

	private String getHash(DataObject d) {

		LOG.trace(null);

		List<Attribute> attributes = d
				.getAttribute(DefinedAttributeIdentification.HASH_OF_DATA
						.getURI());
		if (attributes.isEmpty()) {

			LOG.trace("No hash of data found");
			return null;

		} else {

			return attributes.get(0).getValue(String.class);

		}

	}

	/**
	 * 
	 * @param dataObject
	 */

	private void addLocator(DataObject dataObject) {
		String hash = getHash(dataObject);
		Attribute attribute = dataObject.getDatamodelFactory()
				.createAttribute();
		attribute.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE
				.toString());
		attribute.setIdentification(DefinedAttributeIdentification.HTTP_URL
				.getURI());
		attribute.setValue(server.getURL(hash));
		Attribute cacheMarker = dataObject.getDatamodelFactory()
				.createAttribute();
		cacheMarker
				.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE
						.getAttributePurpose());
		cacheMarker.setIdentification(DefinedAttributeIdentification.CACHE
				.getURI());
		cacheMarker.setValue("true");
		attribute.addSubattribute(cacheMarker);
		// Do not add the same locator twice
		if (!dataObject.getAttributes().contains(attribute)) {
			dataObject.addAttribute(attribute);
		}
	}

	/**
	 * Get the path of temporary folder
	 * 
	 * @return pathToTemp path of temporary folder
	 */

	private String getTmpFolder() {

		String pathToTmp = System.getProperty("java.io.tmpdir")
				+ File.separator + "peerSideCache";
		File folder = new File(pathToTmp);
		if (folder.exists() && folder.isDirectory()) {
			return pathToTmp;
		} else {
			folder.mkdir();
			return pathToTmp;
		}
	}

	/**
	 * Delete the temporary file.
	 * 
	 * @param path
	 */
	private void deleteTmpFile(String path) {

		File file = new File(path);
		file.delete();
	}
}
