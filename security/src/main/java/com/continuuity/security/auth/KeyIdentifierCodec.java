/*
 * Copyright 2012-2014 Continuuity, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.continuuity.security.auth;

import com.continuuity.common.io.BinaryDecoder;
import com.continuuity.common.io.BinaryEncoder;
import com.continuuity.common.io.Codec;
import com.continuuity.common.io.Decoder;
import com.continuuity.common.io.Encoder;
import com.continuuity.internal.io.DatumReader;
import com.continuuity.internal.io.DatumReaderFactory;
import com.continuuity.internal.io.DatumWriter;
import com.continuuity.internal.io.DatumWriterFactory;
import com.continuuity.internal.io.Schema;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Utility to encode and decode keys that are shared between keyManagers.
 */
public class KeyIdentifierCodec implements Codec<KeyIdentifier> {
  private static final TypeToken<KeyIdentifier> KEY_IDENTIFIER_TYPE = new TypeToken<KeyIdentifier>() { };

  private final DatumReaderFactory readerFactory;
  private final DatumWriterFactory writerFactory;

  @Inject
  public KeyIdentifierCodec(DatumReaderFactory readerFactory, DatumWriterFactory writerFactory) {
    this.readerFactory = readerFactory;
    this.writerFactory = writerFactory;
  }

  @Override
  public byte[] encode(KeyIdentifier keyIdentifier) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Encoder encoder = new BinaryEncoder(bos);

    encoder.writeInt(KeyIdentifier.Schemas.getVersion());
    DatumWriter<KeyIdentifier> writer = writerFactory.create(KEY_IDENTIFIER_TYPE,
                                                             KeyIdentifier.Schemas.getCurrentSchema());
    writer.encode(keyIdentifier, encoder);
    return bos.toByteArray();
  }

  @Override
  public KeyIdentifier decode(byte[] data) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(data);
    Decoder decoder = new BinaryDecoder(bis);

    DatumReader<KeyIdentifier> reader = readerFactory.create(KEY_IDENTIFIER_TYPE,
                                                             KeyIdentifier.Schemas.getCurrentSchema());
    int readVersion = decoder.readInt();
    Schema readSchema = KeyIdentifier.Schemas.getSchemaVersion(readVersion);
    if (readSchema == null) {
      throw new IOException("Unknown schema version for KeyIdentifier: " + readVersion);
    }
    return reader.read(decoder, readSchema);
  }
}
