/*
 * Copyright © 2015 Cask Data, Inc.
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

package co.cask.cdap.data2.metadata.writer;

import co.cask.cdap.api.dataset.module.DatasetDefinitionRegistry;
import co.cask.cdap.common.app.RunIds;
import co.cask.cdap.data.runtime.DataSetsModules;
import co.cask.cdap.data2.dataset2.DatasetDefinitionRegistryFactory;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.data2.dataset2.DatasetFrameworkTestUtil;
import co.cask.cdap.data2.dataset2.DefaultDatasetDefinitionRegistry;
import co.cask.cdap.data2.dataset2.InMemoryDatasetFramework;
import co.cask.cdap.data2.metadata.lineage.AccessType;
import co.cask.cdap.data2.metadata.lineage.LineageStore;
import co.cask.cdap.data2.metadata.publisher.MetadataChangePublisher;
import co.cask.cdap.data2.metadata.publisher.NoOpMetadataChangePublisher;
import co.cask.cdap.data2.metadata.service.BusinessMetadataStore;
import co.cask.cdap.data2.metadata.service.DefaultBusinessMetadataStore;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.ProgramType;
import co.cask.cdap.proto.metadata.MetadataRecord;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Tests BasicLineageWriter
 */
public class BasicLineageWriterTest {
  @ClassRule
  public static DatasetFrameworkTestUtil dsFrameworkUtil = new DatasetFrameworkTestUtil();

  @Test
  public void testWrites() throws Exception {
    Injector injector = getInjector();
    BusinessMetadataStore businessMetadataStore = injector.getInstance(BusinessMetadataStore.class);
    LineageStore lineageStore = injector.getInstance(LineageStore.class);
    LineageWriter lineageWriter = new BasicLineageWriter(businessMetadataStore, lineageStore);

    // Define entities
    Id.Program program = Id.Program.from("default", "app", ProgramType.FLOW, "flow");
    Id.Stream stream = Id.Stream.from("default", "stream");
    Id.Run run1 = new Id.Run(program, RunIds.generate(10000).getId());
    Id.Run run2 = new Id.Run(program, RunIds.generate(20000).getId());

    // Tag stream
    businessMetadataStore.addTags(stream, "stag1", "stag2");
    // Write access for run1
    lineageWriter.addAccess(run1, stream, AccessType.READ);
    // Assert tags on stream
    Assert.assertEquals(
      ImmutableSet.of(
        new MetadataRecord(stream, ImmutableMap.<String, String>of(), ImmutableSet.of("stag1", "stag2")),
        new MetadataRecord(program.getApplication(), ImmutableMap.<String, String>of(), ImmutableSet.<String>of()),
        new MetadataRecord(program, ImmutableMap.<String, String>of(), ImmutableSet.<String>of())),
      lineageStore.getRunMetadata(run1));

    // Add another tag to stream
    businessMetadataStore.addTags(stream, "stag3");
    // Write access for run1 again
    lineageWriter.addAccess(run1, stream, AccessType.READ);
    // The write should be no-op, and metadata for run1 should not be updated
    Assert.assertEquals(
      ImmutableSet.of(
        new MetadataRecord(stream, ImmutableMap.<String, String>of(), ImmutableSet.of("stag1", "stag2")),
        new MetadataRecord(program.getApplication(), ImmutableMap.<String, String>of(), ImmutableSet.<String>of()),
        new MetadataRecord(program, ImmutableMap.<String, String>of(), ImmutableSet.<String>of())),
      lineageStore.getRunMetadata(run1));

    // However, you can write access for another run
    lineageWriter.addAccess(run2, stream, AccessType.READ);
    // Assert new tags get picked up
    Assert.assertEquals(
      ImmutableSet.of(
        new MetadataRecord(stream, ImmutableMap.<String, String>of(), ImmutableSet.of("stag1", "stag2", "stag3")),
        new MetadataRecord(program.getApplication(), ImmutableMap.<String, String>of(), ImmutableSet.<String>of()),
        new MetadataRecord(program, ImmutableMap.<String, String>of(), ImmutableSet.<String>of())),
      lineageStore.getRunMetadata(run2));
  }

  private static Injector getInjector() {
    return dsFrameworkUtil.getInjector().createChildInjector(
      new AbstractModule() {
        @Override
        protected void configure() {
          install(new FactoryModuleBuilder()
                    .implement(DatasetDefinitionRegistry.class, DefaultDatasetDefinitionRegistry.class)
                    .build(DatasetDefinitionRegistryFactory.class));
          bind(DatasetFramework.class)
            .annotatedWith(Names.named(DataSetsModules.BASIC_DATASET_FRAMEWORK))
            .to(InMemoryDatasetFramework.class);
          bind(MetadataChangePublisher.class).to(NoOpMetadataChangePublisher.class);
          bind(BusinessMetadataStore.class).to(DefaultBusinessMetadataStore.class);
        }
      }
    );
  }
}
