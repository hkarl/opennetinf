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
package netinf.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;

import netinf.common.exceptions.NetInfUncheckedException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Utility methods that are used by more than a single package
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class Utils {
   private static final Logger LOG = Logger.getLogger(Utils.class);

   /**
    * Unserializes a Java Object. If possible, then use the appropriate DatamodelFactory method, like e.g.
    * DatamodelFactory#createInformationObjectFromBytes.
    */
   public static Object unserializeJavaObject(byte[] bytes) {
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

      try {
         ObjectInputStream stream = new ObjectInputStream(byteArrayInputStream);
         return stream.readObject();
      } catch (IOException e) {
         LOG.error(e.getMessage());
         throw new NetInfUncheckedException(e);
      } catch (ClassNotFoundException e) {
         LOG.error(e.getMessage());
         throw new NetInfUncheckedException(e);
      }
   }

   public static String bytesToString(byte[] bytes) {
      StringBuilder string = new StringBuilder();
      for (byte b : bytes) {
         string.append((char) b);
      }
      return string.toString();
      // return Base64.encodeBase64String(bytes);

   }

   public static byte[] stringToBytes(String string) {
      char[] chars = string.toCharArray();
      byte[] bytes = new byte[chars.length];
      for (int i = 0; i < chars.length; i++) {
         bytes[i] = (byte) chars[i];
      }
      return bytes;
      // return Base64.decodeBase64(string);
   }

   public static Properties loadProperties(String pathToProperties) {
      Properties result = new Properties();

      FileInputStream stream = null;

      try {
         stream = new FileInputStream(pathToProperties);
         result.load(stream);

      } catch (FileNotFoundException e) {
         throw new NetInfUncheckedException(e);
      } catch (IOException e) {
         throw new NetInfUncheckedException(e);
      } finally {
         try {
            if (stream != null) {
               stream.close();
            }
         } catch (IOException e) {
            IOUtils.closeQuietly(stream);
         }
      }

      return result;
   }

   public static PublicKey stringToPublicKey(String publicKey) {
      return (PublicKey) stringToObject(publicKey);
   }

   public static PrivateKey stringToPrivateKey(String privateKey) {
      return (PrivateKey) stringToObject(privateKey);
   }

   public static Object stringToObject(String object) {
      try {
         ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decodeBase64(object));
         ObjectInputStream serializedObject = new ObjectInputStream(bais);
         return serializedObject.readObject();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (ClassNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return null;
   }

   public static String objectToString(Object object) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos;
      try {
         oos = new ObjectOutputStream(baos);
         oos.writeObject(object);
      } catch (IOException e) {
         return null;
      }
      return Base64.encodeBase64String(baos.toByteArray());
      // return Utils.bytesToString(baos.toByteArray());
   }

   public static String hexStringFromBytes(byte[] input) {
      StringBuffer hexString = new StringBuffer();
      for (int i = 0; i < input.length; i++) {
         String hex = Integer.toHexString(0xFF & input[i]);

         if (hex.length() == 1) {
            hexString.append('0');
         }

         hexString.append(hex);
      }

      return hexString.toString();
   }
}
