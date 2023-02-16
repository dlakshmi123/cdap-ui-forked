/*
 * Copyright (c) 2013, Continuuity Inc
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms,
 * with or without modification, are not permitted
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.payvment.continuuity;


import com.continuuity.api.common.Bytes;

/**
 * Contains utility functions.
 */
public class Helpers {

  /**
   * Returns the reverse timestamp of the specified timestamp
   * ({@link Long#MAX_VALUE} - timestamp).
   *
   * @param timestamp
   * @return reverse of specified timestamp
   */
  public static long reverse(long timestamp) {
    return Long.MAX_VALUE - timestamp;
  }

  /**
   * Returns the time-bucketed timestamp of the specified timestamp in
   * milliseconds: timestamp - (timestamp modulo 60*60*1000).
   *
   * @param timestamp stamp in milliseconds
   * @return hour time bucket of specified timestamp in milliseconds
   */
  public static Long hour(Long timestamp) {
    return timestamp - (timestamp % 3600000);
  }

  /**
   * Converts the specified array of strings to an array of byte arrays.
   *
   * @param stringArray array of string
   * @return array of byte arrays
   */
  public static byte[][] saToBa(String[] stringArray) {
    byte[][] byteArrays = new byte[stringArray.length][];
    for (int i = 0; i < stringArray.length; i++) {
      byteArrays[i] = Bytes.toBytes(stringArray[i]);
    }
    return byteArrays;
  }
}
