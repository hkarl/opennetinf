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
package netinf.node.transfer.http;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import netinf.common.log.demo.DemoLevel;
import netinf.node.transfer.ExecutableTransferJob;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * The Class TransferJobHttp.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class TransferJobHttp extends ExecutableTransferJob {

   private static final Logger LOG = Logger.getLogger(TransferJobHttp.class);

   public TransferJobHttp(String jobId, String source, String destination) {
      super(jobId, source, destination);

   }

   @Override
   public boolean isCompleted() {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public void startTransferJob() {
      LOG.info("Start downloading data for caching from: " + getSource());
      LOG.log(DemoLevel.DEMO, "(NODE ) Start downloading data for caching from: " + getSource());
      DataOutputStream file = null;
      InputStream is = null;
      try {
         URL url = new URL(this.getSource());
         URLConnection urlConnection = url.openConnection();
         byte[] contentTypeBytes = urlConnection.getContentType().getBytes();
         is = urlConnection.getInputStream();
         file = new DataOutputStream(new FileOutputStream(this.getDestination()));
         file.writeInt(contentTypeBytes.length);
         file.write(contentTypeBytes);
         byte[] buffer = new byte[4096];
         int readBytes = -1;
         while ((readBytes = is.read(buffer)) != -1) {
            file.write(buffer, 0, readBytes);
         }
         LOG.info("Finished download from: " + getSource());
         LOG.log(DemoLevel.DEMO, "(NODE ) Finished download from: " + getSource());
      } catch (MalformedURLException e) {
         LOG.warn("Could not download data from: " + getSource());
      } catch (IOException e) {
         LOG.warn("Could not download data from: " + getSource());
      } finally {
         IOUtils.closeQuietly(is);
         IOUtils.closeQuietly(file);
      }

   }

}
