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
package netinf.node.gp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import netinf.common.exceptions.NetInfUncheckedException;
import netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer;
import netinf.node.gp.messages.GPNetInfMessages.NIaddName;
import netinf.node.gp.messages.GPNetInfMessages.NImoveEP;
import netinf.node.gp.messages.GPNetInfMessages.NIprepareGP;
import netinf.node.gp.messages.GPNetInfMessages.NIresolve;
import netinf.node.gp.messages.GPNetInfMessages.NIresolveCallback;
import netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer.NIMessageType;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessage;

/**
 * A convenience class for testing. Represents the GP side. Can send messages to NetInf and receive message from netInf.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class GPCommunicator {

   private static final Logger LOG = Logger.getLogger(GPCommunicator.class);

   private final DataInputStream in;
   private final DataOutputStream out;
   private NIMessageContainer receivedMessage;

   public GPCommunicator(DataInputStream in, DataOutputStream out) {
      LOG.trace("Created new instance of GPCommunicator");
      this.in = in;
      this.out = out;
   }

   public NIMessageType getMessageType() {
      return receivedMessage.getType();
   }

   public GeneratedMessage getMessage() {
      LOG.trace(null);
      if (receivedMessage.getType() == NIMessageType.ADDNAME) {
         return getAddName();
      } else if (receivedMessage.getType() == NIMessageType.MOVEEP) {
         return getMoveEP();
      } else if (receivedMessage.getType() == NIMessageType.PREPAREGP) {
         return getPrepareGP();
      } else if (receivedMessage.getType() == NIMessageType.RESOLVE) {
         return getResolve();
      } else if (receivedMessage.getType() == NIMessageType.RESOLVECALLBACK) {
         return getResolveCallback();
      } else {
         return null;
      }
   }

   public NIresolveCallback getResolveCallback() {
      LOG.trace(null);
      return receivedMessage.getResolveCallback();
   }

   public NIresolve getResolve() {
      LOG.trace(null);
      return receivedMessage.getResolve();
   }

   public NIprepareGP getPrepareGP() {
      LOG.trace(null);
      return receivedMessage.getPrepareGP();
   }

   public NImoveEP getMoveEP() {
      LOG.trace(null);
      return receivedMessage.getMoveEP();
   }

   public NIaddName getAddName() {
      LOG.trace(null);
      return receivedMessage.getAddName();
   }

   public NIaddName receiveNIaddName() throws IOException {
      LOG.trace(null);
      receive();
      return getAddName();
   }

   public NIresolve receiveNIresolve() throws IOException {
      receive();
      return getResolve();
   }

   public NIprepareGP receiveNIprepareGP() throws IOException {
      receive();
      return getPrepareGP();
   }

   public NImoveEP receiveNImoveEP() throws IOException {
      receive();
      return getMoveEP();
   }

   public void sendMessage(GeneratedMessage message) {
      LOG.trace(null);
      NIMessageContainer messageContainer = null;

      if (message instanceof NIaddName) {
         NIaddName niAddName = (NIaddName) message;
         netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer.Builder builder = NIMessageContainer.newBuilder();
         builder.setAddName(niAddName);
         builder.setType(NIMessageType.ADDNAME);
         messageContainer = builder.build();
      } else if (message instanceof NIresolve) {
         NIresolve niResolve = (NIresolve) message;
         netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer.Builder builder = NIMessageContainer.newBuilder();
         builder.setResolve(niResolve);
         builder.setType(NIMessageType.RESOLVE);
         messageContainer = builder.build();
      } else if (message instanceof NIresolveCallback) {
         NIresolveCallback niResolveCallback = (NIresolveCallback) message;
         netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer.Builder builder = NIMessageContainer.newBuilder();
         builder.setResolveCallback(niResolveCallback);
         builder.setType(NIMessageType.RESOLVECALLBACK);
         messageContainer = builder.build();
      } else if (message instanceof NIprepareGP) {
         NIprepareGP niPrepareVLCGP = (NIprepareGP) message;
         netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer.Builder builder = NIMessageContainer.newBuilder();
         builder.setPrepareGP(niPrepareVLCGP);
         builder.setType(NIMessageType.PREPAREGP);
         messageContainer = builder.build();
      } else if (message instanceof NImoveEP) {
         NImoveEP niMoveEP = (NImoveEP) message;
         netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer.Builder builder = NIMessageContainer.newBuilder();
         builder.setMoveEP(niMoveEP);
         builder.setType(NIMessageType.MOVEEP);
         messageContainer = builder.build();
      } else {
         throw new NetInfUncheckedException("Could not determine kind of message");
      }

      try {
         LOG.debug("Sending message " + messageContainer);

         byte[] toBeSend = messageContainer.toByteArray();
         out.writeInt(toBeSend.length);
         out.write(toBeSend);
      } catch (IOException e) {
         LOG.error("Error while sending message");
      }
   }

   public void receive() throws IOException {
      LOG.trace(null);
      int readInt = in.readInt();

      byte[] bytes = new byte[readInt];
      in.readFully(bytes);

      receivedMessage = NIMessageContainer.parseFrom(bytes);
      LOG.debug("GP counterpart received a message: " + receivedMessage);
   }

   public void sendBytes(byte[] bytes) throws IOException {
      int length = bytes.length;
      out.writeInt(length);
      out.write(bytes);
   }

   public void close() throws IOException {
      out.close();
      in.close();
   }
}
