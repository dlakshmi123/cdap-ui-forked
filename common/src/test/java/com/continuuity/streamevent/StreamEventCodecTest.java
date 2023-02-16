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

package com.continuuity.streamevent;

import com.continuuity.api.flow.flowlet.StreamEvent;
import com.continuuity.common.io.BinaryDecoder;
import com.continuuity.common.stream.DefaultStreamEvent;
import com.continuuity.common.stream.StreamEventCodec;
import com.continuuity.internal.io.ByteBufferInputStream;
import com.continuuity.internal.io.ReflectionDatumReader;
import com.continuuity.internal.io.ReflectionSchemaGenerator;
import com.continuuity.internal.io.Schema;
import com.continuuity.internal.io.SchemaHash;
import com.continuuity.internal.io.UnsupportedTypeException;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 */
public class StreamEventCodecTest {

  @Test
  public void testEncodeDecode() {
    StreamEvent event = new DefaultStreamEvent(Maps.<String, String>newHashMap(),
                                               ByteBuffer.wrap("Event string".getBytes(Charsets.UTF_8)));

    StreamEventCodec codec = new StreamEventCodec();
    StreamEvent decodedEvent = codec.decodePayload(codec.encodePayload(event));

    Assert.assertEquals(event.getHeaders(), decodedEvent.getHeaders());
    Assert.assertEquals(event.getBody(), decodedEvent.getBody());
  }

  @Test
  public void testEncodeDecodeWithDatumDecoder() throws UnsupportedTypeException, IOException {
    StreamEvent event = new DefaultStreamEvent(Maps.<String, String>newHashMap(),
                                               ByteBuffer.wrap("Event string".getBytes(Charsets.UTF_8)));

    StreamEventCodec codec = new StreamEventCodec();
    ByteBuffer payload = ByteBuffer.wrap(codec.encodePayload(event));

    SchemaHash schemaHash = new SchemaHash(payload);
    Schema schema = new ReflectionSchemaGenerator().generate(StreamEvent.class);

    Assert.assertEquals(schema.getSchemaHash(), schemaHash);

    StreamEvent decoded = new ReflectionDatumReader<DefaultStreamEvent>(schema, TypeToken.of(DefaultStreamEvent.class))
          .read(new BinaryDecoder(new ByteBufferInputStream(payload)), schema);

    Assert.assertEquals(event.getHeaders(), decoded.getHeaders());
    Assert.assertEquals(event.getBody(), decoded.getBody());
  }
}
