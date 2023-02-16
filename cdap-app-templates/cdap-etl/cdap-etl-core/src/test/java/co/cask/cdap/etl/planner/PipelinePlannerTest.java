/*
 * Copyright © 2016 Cask Data, Inc.
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

package co.cask.cdap.etl.planner;

import co.cask.cdap.api.artifact.ArtifactId;
import co.cask.cdap.api.artifact.ArtifactScope;
import co.cask.cdap.api.artifact.ArtifactVersion;
import co.cask.cdap.api.data.schema.Schema;
import co.cask.cdap.etl.common.Constants;
import co.cask.cdap.etl.common.PipelinePhase;
import co.cask.cdap.etl.proto.Connection;
import co.cask.cdap.etl.spec.PipelineSpec;
import co.cask.cdap.etl.spec.PluginSpec;
import co.cask.cdap.etl.spec.StageSpec;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class PipelinePlannerTest {
  private static final PluginSpec NODE =
    new PluginSpec("node", "name", ImmutableMap.<String, String>of(),
                   new ArtifactId("dummy", new ArtifactVersion("1.0.0"), ArtifactScope.USER));
  private static final PluginSpec REDUCE =
    new PluginSpec("reduce", "name", ImmutableMap.<String, String>of(),
                   new ArtifactId("dummy", new ArtifactVersion("1.0.0"), ArtifactScope.USER));
  private static final PluginSpec CONDITION =
    new PluginSpec("condition", "condition", ImmutableMap.<String, String>of(), null);
  private static final PluginSpec CONDITION1 =
    new PluginSpec("condition", "condition1", ImmutableMap.<String, String>of(), null);
  private static final PluginSpec CONDITION2 =
    new PluginSpec("condition", "condition2", ImmutableMap.<String, String>of(), null);
  private static final PluginSpec CONDITION3 =
    new PluginSpec("condition", "condition3", ImmutableMap.<String, String>of(), null);
  private static final PluginSpec CONDITION4 =
    new PluginSpec("condition", "condition4", ImmutableMap.<String, String>of(), null);
  private static final PluginSpec CONDITION5 =
    new PluginSpec("condition", "condition5", ImmutableMap.<String, String>of(), null);
  @Test
  public void testGeneratePlan() {
    /*
             |--- n2(r) ----------|
             |                    |                                    |-- n10
        n1 --|--- n3(r) --- n5 ---|--- n6 --- n7(r) --- n8 --- n9(r) --|
             |                    |                                    |-- n11
             |--- n4(r) ----------|
     */
    // create the spec for this pipeline
    Schema schema = Schema.recordOf("stuff", Schema.Field.of("x", Schema.of(Schema.Type.INT)));
    Set<StageSpec> stageSpecs = ImmutableSet.of(
      StageSpec.builder("n1", NODE)
        .addOutputSchema(schema, "n2", "n3", "n4")
        .build(),
      StageSpec.builder("n2", REDUCE)
        .addInputSchema("n1", schema)
        .addOutputSchema(schema, "n6")
        .build(),
      StageSpec.builder("n3", REDUCE)
        .addInputSchema("n1", schema)
        .addOutputSchema(schema, "n5")
        .build(),
      StageSpec.builder("n4", REDUCE)
        .addInputSchema("n1", schema)
        .addOutputSchema(schema, "n6")
        .build(),
      StageSpec.builder("n5", NODE)
        .addInputSchema("n3", schema)
        .addOutputSchema(schema, "n6")
        .build(),
      StageSpec.builder("n6", NODE)
        .addInputSchemas(ImmutableMap.of("n2", schema, "n5", schema, "n4", schema))
        .addOutputSchema(schema, "n7")
        .build(),
      StageSpec.builder("n7", REDUCE)
        .addInputSchema("n6", schema)
        .addOutputSchema(schema, "n8")
        .build(),
      StageSpec.builder("n8", NODE)
        .addInputSchema("n7", schema)
        .addOutputSchema(schema, "n9")
        .build(),
      StageSpec.builder("n9", REDUCE)
        .addInputSchema("n8", schema)
        .addOutputSchema(schema, "n10", "n11")
        .build(),
      StageSpec.builder("n10", NODE)
        .addInputSchema("n9", schema)
        .build(),
      StageSpec.builder("n11", NODE)
        .addInputSchema("n9", schema)
        .build()
    );
    Set<Connection> connections = ImmutableSet.of(
      new Connection("n1", "n2"),
      new Connection("n1", "n3"),
      new Connection("n1", "n4"),
      new Connection("n2", "n6"),
      new Connection("n3", "n5"),
      new Connection("n4", "n6"),
      new Connection("n5", "n6"),
      new Connection("n6", "n7"),
      new Connection("n7", "n8"),
      new Connection("n8", "n9"),
      new Connection("n9", "n10"),
      new Connection("n9", "n11")
    );
    Set<String> pluginTypes = ImmutableSet.of(NODE.getType(), REDUCE.getType(), Constants.CONNECTOR_TYPE);
    Set<String> reduceTypes = ImmutableSet.of(REDUCE.getType());
    Set<String> emptySet = ImmutableSet.of();
    PipelinePlanner planner = new PipelinePlanner(pluginTypes, reduceTypes, emptySet, emptySet);
    PipelineSpec pipelineSpec = PipelineSpec.builder().addStages(stageSpecs).addConnections(connections).build();

    Map<String, PipelinePhase> phases = new HashMap<>();
    /*
             |--- n2.connector
             |
        n1 --|--- n3.connector
             |
             |--- n4.connector
     */
    PipelinePhase phase1 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("n1", NODE).addOutputSchema(schema, "n2", "n3", "n4").build())
      .addStage(StageSpec.builder("n2.connector", connectorSpec("n2")).build())
      .addStage(StageSpec.builder("n3.connector", connectorSpec("n3")).build())
      .addStage(StageSpec.builder("n4.connector", connectorSpec("n4")).build())
      .addConnections("n1", ImmutableSet.of("n2.connector", "n3.connector", "n4.connector"))
      .build();
    String phase1Name = getPhaseName("n1", "n2.connector", "n3.connector", "n4.connector");
    phases.put(phase1Name, phase1);

    /*
        phase2:
        n2.connector --- n2(r) --- n6 --- n7.connector
     */
    PipelinePhase phase2 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("n2", REDUCE)
                  .addInputSchema("n1", schema)
                  .addOutputSchema(schema, "n6")
                  .build())
      .addStage(StageSpec.builder("n6", NODE)
                  .addInputSchema("n2", schema)
                  .addInputSchema("n4", schema)
                  .addInputSchema("n5", schema)
                  .addOutputSchema(schema, "n7")
                  .build())
      .addStage(StageSpec.builder("n2.connector", connectorSpec("n2")).build())
      .addStage(StageSpec.builder("n7.connector", connectorSpec("n7")).build())
      .addConnection("n2.connector", "n2")
      .addConnection("n2", "n6")
      .addConnection("n6", "n7.connector")
      .build();
    String phase2Name = getPhaseName("n2.connector", "n7.connector");
    phases.put(phase2Name, phase2);

    /*
        phase3:
        n3.connector --- n3(r) --- n5 --- n6 --- n7.connector
     */
    PipelinePhase phase3 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("n5", NODE)
                  .addInputSchema("n3", schema)
                  .addOutputSchema(schema, "n6")
                  .build())
      .addStage(StageSpec.builder("n6", NODE)
                  .addInputSchema("n2", schema)
                  .addInputSchema("n4", schema)
                  .addInputSchema("n5", schema)
                  .addOutputSchema(schema, "n7")
                  .build())
      .addStage(StageSpec.builder("n3", REDUCE)
                  .addInputSchema("n1", schema)
                  .addOutputSchema(schema, "n5")
                  .build())
      .addStage(StageSpec.builder("n3.connector", connectorSpec("n3")).build())
      .addStage(StageSpec.builder("n7.connector", connectorSpec("n7")).build())
      .addConnection("n3.connector", "n3")
      .addConnection("n3", "n5")
      .addConnection("n5", "n6")
      .addConnection("n6", "n7.connector")
      .build();
    String phase3Name = getPhaseName("n3.connector", "n7.connector");
    phases.put(phase3Name, phase3);

    /*
        phase4:
        n4.connector --- n4(r) --- n6 --- n7.connector
     */
    PipelinePhase phase4 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("n4", REDUCE)
                  .addInputSchema("n1", schema)
                  .addOutputSchema(schema, "n6")
                  .build())
      .addStage(StageSpec.builder("n6", NODE)
                  .addInputSchema("n2", schema)
                  .addInputSchema("n4", schema)
                  .addInputSchema("n5", schema)
                  .addOutputSchema(schema, "n7")
                  .build())
      .addStage(StageSpec.builder("n4.connector", connectorSpec("n4")).build())
      .addStage(StageSpec.builder("n7.connector", connectorSpec("n7")).build())
      .addConnection("n4.connector", "n4")
      .addConnection("n4", "n6")
      .addConnection("n6", "n7.connector")
      .build();
    String phase4Name = getPhaseName("n4.connector", "n7.connector");
    phases.put(phase4Name, phase4);

    /*
        phase5:
        n7.connector --- n7(r) --- n8 --- n9.connector
     */
    PipelinePhase phase5 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("n8", NODE)
                  .addInputSchema("n7", schema)
                  .addOutputSchema(schema, "n9")
                  .build())
      .addStage(StageSpec.builder("n7", REDUCE)
                  .addInputSchema("n6", schema)
                  .addOutputSchema(schema, "n8")
                  .build())
      .addStage(StageSpec.builder("n7.connector", connectorSpec("n7")).build())
      .addStage(StageSpec.builder("n9.connector", connectorSpec("n9")).build())
      .addConnection("n7.connector", "n7")
      .addConnection("n7", "n8")
      .addConnection("n8", "n9.connector")
      .build();
    String phase5Name = getPhaseName("n7.connector", "n9.connector");
    phases.put(phase5Name, phase5);

    /*
        phase6:
                                 |-- n10
        n9.connector --- n9(r) --|
                                 |-- n11
     */
    PipelinePhase phase6 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("n10", NODE).addInputSchema("n9", schema).build())
      .addStage(StageSpec.builder("n11", NODE).addInputSchema("n9", schema).build())
      .addStage(StageSpec.builder("n9", REDUCE)
                  .addInputSchema("n8", schema)
                  .addOutputSchema(schema, "n10", "n11")
                  .build())
      .addStage(StageSpec.builder("n9.connector", connectorSpec("n9")).build())
      .addConnection("n9.connector", "n9")
      .addConnection("n9", "n10")
      .addConnection("n9", "n11")
      .build();
    String phase6Name = getPhaseName("n9.connector", "n10", "n11");
    phases.put(phase6Name, phase6);

    Set<Connection> phaseConnections = new HashSet<>();
    phaseConnections.add(new Connection(phase1Name, phase2Name));
    phaseConnections.add(new Connection(phase1Name, phase3Name));
    phaseConnections.add(new Connection(phase1Name, phase4Name));
    phaseConnections.add(new Connection(phase2Name, phase5Name));
    phaseConnections.add(new Connection(phase3Name, phase5Name));
    phaseConnections.add(new Connection(phase4Name, phase5Name));
    phaseConnections.add(new Connection(phase5Name, phase6Name));

    PipelinePlan expected = new PipelinePlan(phases, phaseConnections);
    PipelinePlan actual = planner.plan(pipelineSpec);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testSimpleCondition() throws Exception {
    /*
      n1 - n2 - condition - n3
                      |
                      |---- n4
     */

    Set<StageSpec> stageSpecs = ImmutableSet.of(
      StageSpec.builder("n1", NODE)
        .build(),
      StageSpec.builder("n2", NODE)
        .build(),
      StageSpec.builder("condition", CONDITION)
        .build(),
      StageSpec.builder("n3", NODE)
        .build(),
      StageSpec.builder("n4", NODE)
        .build()
    );

    Set<Connection> connections = ImmutableSet.of(
      new Connection("n1", "n2"),
      new Connection("n2", "condition"),
      new Connection("condition", "n3", true),
      new Connection("condition", "n4", false)
    );

    Set<String> pluginTypes = ImmutableSet.of(NODE.getType(), REDUCE.getType(), Constants.CONNECTOR_TYPE,
                                              CONDITION.getType());
    Set<String> reduceTypes = ImmutableSet.of(REDUCE.getType());
    Set<String> emptySet = ImmutableSet.of();
    PipelinePlanner planner = new PipelinePlanner(pluginTypes, reduceTypes, emptySet, emptySet);
    PipelineSpec pipelineSpec = PipelineSpec.builder().addStages(stageSpecs).addConnections(connections).build();


    Map<String, PipelinePhase> phases = new HashMap<>();
    /*
      n1--n2--condition.connector
     */
    PipelinePhase phase1 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("n1", NODE).build())
      .addStage(StageSpec.builder("n2", NODE).build())
      .addStage(StageSpec.builder("condition.connector", connectorSpec("condition.connector")).build())
      .addConnection("n1", "n2")
      .addConnection("n2", "condition.connector")
      .build();
    String phase1Name = getPhaseName("n1", "condition");
    phases.put(phase1Name, phase1);

    /*
      condition
     */
    PipelinePhase phase2 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition", CONDITION).build())
      .build();
    String phase2Name = "condition";
    phases.put(phase2Name, phase2);

    /*
      condition.connector -- n3
     */
    PipelinePhase phase3 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition.connector", connectorSpec("condition.connector")).build())
      .addStage(StageSpec.builder("n3", NODE).build())
      //.addConnection("condition.connector", "n3", true)
      .addConnection("condition.connector", "n3")
      .build();
    String phase3Name = getPhaseName("condition", "n3");
    phases.put(phase3Name, phase3);

     /*
      condition.connector -- n4
     */
    PipelinePhase phase4 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition.connector", connectorSpec("condition.connector")).build())
      .addStage(StageSpec.builder("n4", NODE).build())
      .addConnection("condition.connector", "n4")
      .build();
    String phase4Name = getPhaseName("condition", "n4");
    phases.put(phase4Name, phase4);

    Set<Connection> phaseConnections = new HashSet<>();
    phaseConnections.add(new Connection(phase1Name, phase2Name));
    phaseConnections.add(new Connection(phase2Name, phase3Name, true));
    phaseConnections.add(new Connection(phase2Name, phase4Name, false));

    PipelinePlan expected = new PipelinePlan(phases, phaseConnections);
    PipelinePlan actual = planner.plan(pipelineSpec);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testMultipleConditions() throws Exception {
   /*
      n1 - n2 - condition1 - n3 - n4 - condition2 - n5 - condition3 - n6
                    |                       |                   |
                    |--n10                  |---condition4 - n8 |------n7
                                                  |
                                                  |----condition5 - n9
     */

    Set<StageSpec> stageSpecs = ImmutableSet.of(
      StageSpec.builder("n1", NODE)
        .build(),
      StageSpec.builder("n2", NODE)
        .build(),
      StageSpec.builder("condition1", CONDITION1)
        .build(),
      StageSpec.builder("n3", NODE)
        .build(),
      StageSpec.builder("n4", NODE)
        .build(),
      StageSpec.builder("condition2", CONDITION2)
        .build(),
      StageSpec.builder("n5", NODE)
        .build(),
      StageSpec.builder("condition3", CONDITION3)
        .build(),
      StageSpec.builder("n6", NODE)
        .build(),
      StageSpec.builder("condition4", CONDITION4)
        .build(),
      StageSpec.builder("n7", NODE)
        .build(),
      StageSpec.builder("condition5", CONDITION5)
        .build(),
      StageSpec.builder("n8", NODE)
        .build(),
      StageSpec.builder("n9", NODE)
        .build(),
      StageSpec.builder("n10", NODE)
        .build()
      );

    Set<Connection> connections = ImmutableSet.of(
      new Connection("n1", "n2"),
      new Connection("n2", "condition1"),
      new Connection("condition1", "n3", true),
      new Connection("condition1", "n10", false),
      new Connection("n3", "n4"),
      new Connection("n4", "condition2"),
      new Connection("condition2", "n5", true),
      new Connection("n5", "condition3"),
      new Connection("condition3", "n6", true),
      new Connection("condition3", "n7", false),
      new Connection("condition2", "condition4", false),
      new Connection("condition4", "n8", true),
      new Connection("condition4", "condition5", false),
      new Connection("condition5", "n9", true)
    );

    Set<String> pluginTypes = ImmutableSet.of(NODE.getType(), REDUCE.getType(), Constants.CONNECTOR_TYPE,
                                              CONDITION1.getType(), CONDITION2.getType(), CONDITION3.getType(),
                                              CONDITION4.getType(), CONDITION5.getType());
    Set<String> reduceTypes = ImmutableSet.of(REDUCE.getType());
    Set<String> emptySet = ImmutableSet.of();
    PipelinePlanner planner = new PipelinePlanner(pluginTypes, reduceTypes, emptySet, emptySet);
    PipelineSpec pipelineSpec = PipelineSpec.builder().addStages(stageSpecs).addConnections(connections).build();

    Map<String, PipelinePhase> phases = new HashMap<>();
    /*
      n1--n2--condition1.connector
     */
    PipelinePhase phase1 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("n1", NODE).build())
      .addStage(StageSpec.builder("n2", NODE).build())
      .addStage(StageSpec.builder("condition1.connector", connectorSpec("condition1.connector")).build())
      .addConnection("n1", "n2")
      .addConnection("n2", "condition1.connector")
      .build();
    String phase1Name = getPhaseName("n1", "condition1");
    phases.put(phase1Name, phase1);

    /*
      condition1
     */
    PipelinePhase phase2 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition1", CONDITION1).build())
      .build();
    String phase2Name = "condition1";
    phases.put(phase2Name, phase2);

    /*
      condition1.connector -- n3 - n4 - condition2.connector
     */
    PipelinePhase phase3 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition1.connector", connectorSpec("condition1.connector")).build())
      .addStage(StageSpec.builder("condition2.connector", connectorSpec("condition2.connector")).build())
      .addStage(StageSpec.builder("n3", NODE).build())
      .addStage(StageSpec.builder("n4", NODE).build())
      .addConnection("condition1.connector", "n3")
      .addConnection("n3", "n4")
      .addConnection("n4", "condition2.connector")
      .build();
    String phase3Name = getPhaseName("condition1", "condition2");
    phases.put(phase3Name, phase3);

    /*
      condition1.connector -- n10
     */
    PipelinePhase phase4 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition1.connector", connectorSpec("condition1.connector")).build())
      .addStage(StageSpec.builder("n10", NODE).build())
      .addConnection("condition1.connector", "n10")
      .build();
    String phase4Name = getPhaseName("condition1", "n10");
    phases.put(phase4Name, phase4);

    /*
      condition2
     */
    PipelinePhase phase5 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition2", CONDITION2).build())
      .build();
    String phase5Name = "condition2";
    phases.put(phase5Name, phase5);

    /*
      condition2.connector -- n5 -- condition3.connector
     */
    PipelinePhase phase6 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition2.connector", connectorSpec("condition2.connector")).build())
      .addStage(StageSpec.builder("n5", NODE).build())
      .addStage(StageSpec.builder("condition3.connector", connectorSpec("condition3.connector")).build())
      .addConnection("condition2.connector", "n5")
      .addConnection("n5", "condition3.connector")
      .build();
    String phase6Name = getPhaseName("condition2", "condition3");
    phases.put(phase6Name, phase6);

    /*
      condition3
     */
    PipelinePhase phase7 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition3", CONDITION3).build())
      .build();
    String phase7Name = "condition3";
    phases.put(phase7Name, phase7);

    /*
      condition3.connector -- n6
     */
    PipelinePhase phase8 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("n6", NODE).build())
      .addStage(StageSpec.builder("condition3.connector", connectorSpec("condition3.connector")).build())
      .addConnection("condition3.connector", "n6")
      .build();
    String phase8Name = getPhaseName("condition3", "n6");
    phases.put(phase8Name, phase8);

    /*
      condition3.connector -- n7
     */
    PipelinePhase phase9 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("n7", NODE).build())
      .addStage(StageSpec.builder("condition3.connector", connectorSpec("condition3.connector")).build())
      .addConnection("condition3.connector", "n7")
      .build();
    String phase9Name = getPhaseName("condition3", "n7");
    phases.put(phase9Name, phase9);

    /*
      condition4
     */
    PipelinePhase phase10 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition4", CONDITION4).build())
      .build();
    String phase10Name = "condition4";
    phases.put(phase10Name, phase10);

    /*
      condition4(condition2.connector) -- n8
     */
    PipelinePhase phase11 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("n8", NODE).build())
      .addStage(StageSpec.builder("condition2.connector", connectorSpec("condition2.connector")).build())
      .addConnection("condition2.connector", "n8")
      .build();
    String phase11Name = getPhaseName("condition4", "n8");
    phases.put(phase11Name, phase11);

    /*
      condition5
     */
    PipelinePhase phase12 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition5", CONDITION5).build())
      .build();
    String phase12Name = "condition5";
    phases.put(phase12Name, phase12);

    /*
      condition5(condition2.connector) -- n9
     */
    PipelinePhase phase13 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("n9", NODE).build())
      .addStage(StageSpec.builder("condition2.connector", connectorSpec("condition2.connector")).build())
      .addConnection("condition2.connector", "n9")
      .build();
    String phase13Name = getPhaseName("condition5", "n9");
    phases.put(phase13Name, phase13);

    Set<Connection> phaseConnections = new HashSet<>();
    phaseConnections.add(new Connection(phase1Name, phase2Name));
    phaseConnections.add(new Connection(phase2Name, phase3Name, true));
    phaseConnections.add(new Connection(phase2Name, phase4Name, false));
    phaseConnections.add(new Connection(phase3Name, phase5Name));
    phaseConnections.add(new Connection(phase5Name, phase6Name, true));
    phaseConnections.add(new Connection(phase6Name, phase7Name));
    phaseConnections.add(new Connection(phase7Name, phase8Name, true));
    phaseConnections.add(new Connection(phase7Name, phase9Name, false));
    phaseConnections.add(new Connection(phase5Name, phase10Name, false));
    phaseConnections.add(new Connection(phase10Name, phase11Name, true));
    phaseConnections.add(new Connection(phase10Name, phase12Name, false));
    phaseConnections.add(new Connection(phase12Name, phase13Name, true));

    PipelinePlan expected = new PipelinePlan(phases, phaseConnections);
    PipelinePlan actual = planner.plan(pipelineSpec);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testConditionsToConditions() throws Exception {
   /*
      n1 - c1----c2---n2
           |
           |-----c3---n3
     */

    Set<StageSpec> stageSpecs = ImmutableSet.of(
      StageSpec.builder("n1", NODE)
        .build(),
      StageSpec.builder("n2", NODE)
        .build(),
      StageSpec.builder("condition1", CONDITION1)
        .build(),
      StageSpec.builder("n3", NODE)
        .build(),
      StageSpec.builder("condition2", CONDITION2)
        .build(),
      StageSpec.builder("condition3", CONDITION3)
        .build()
    );

    Set<Connection> connections = ImmutableSet.of(
      new Connection("n1", "condition1"),
      new Connection("condition1", "condition2", true),
      new Connection("condition1", "condition3", false),
      new Connection("condition2", "n2", true),
      new Connection("condition3", "n3", true)
    );

    Set<String> pluginTypes = ImmutableSet.of(NODE.getType(), REDUCE.getType(), Constants.CONNECTOR_TYPE,
                                              CONDITION1.getType(), CONDITION2.getType(), CONDITION3.getType(),
                                              CONDITION4.getType(), CONDITION5.getType());
    Set<String> reduceTypes = ImmutableSet.of(REDUCE.getType());
    Set<String> emptySet = ImmutableSet.of();
    PipelinePlanner planner = new PipelinePlanner(pluginTypes, reduceTypes, emptySet, emptySet);
    PipelineSpec pipelineSpec = PipelineSpec.builder().addStages(stageSpecs).addConnections(connections).build();
    Map<String, PipelinePhase> phases = new HashMap<>();
    /*
      n1--condition1.connector
     */
    PipelinePhase phase1 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("n1", NODE).build())
      .addStage(StageSpec.builder("condition1.connector", connectorSpec("condition1.connector")).build())
      .addConnection("n1", "condition1.connector")
      .build();
    String phase1Name = getPhaseName("n1", "condition1");
    phases.put(phase1Name, phase1);

    /*
      condition1
     */
    PipelinePhase phase2 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition1", CONDITION1).build())
      .build();
    String phase2Name = "condition1";
    phases.put(phase2Name, phase2);

    /*
      condition2
     */
    PipelinePhase phase3 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition2", CONDITION2).build())
      .build();
    String phase3Name = "condition2";
    phases.put(phase3Name, phase3);

    /*
      condition3
     */
    PipelinePhase phase4 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition3", CONDITION3).build())
      .build();
    String phase4Name = "condition3";
    phases.put(phase4Name, phase4);


    /*
      condition1.connector -- n2
     */
    PipelinePhase phase5 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition1.connector", connectorSpec("condition1.connector")).build())
      .addStage(StageSpec.builder("n2", NODE).build())
      .addConnection("condition1.connector", "n2")
      .build();
    String phase5Name = getPhaseName("condition2", "n2");
    phases.put(phase5Name, phase5);

    /*
      condition1.connector -- n3
     */
    PipelinePhase phase6 = PipelinePhase.builder(pluginTypes)
      .addStage(StageSpec.builder("condition1.connector", connectorSpec("condition1.connector")).build())
      .addStage(StageSpec.builder("n3", NODE).build())
      .addConnection("condition1.connector", "n3")
      .build();
    String phase6Name = getPhaseName("condition3", "n3");
    phases.put(phase6Name, phase6);

    Set<Connection> phaseConnections = new HashSet<>();
    phaseConnections.add(new Connection(phase1Name, phase2Name));
    phaseConnections.add(new Connection(phase2Name, phase3Name, true));
    phaseConnections.add(new Connection(phase2Name, phase4Name, false));
    phaseConnections.add(new Connection(phase3Name, phase5Name, true));
    phaseConnections.add(new Connection(phase4Name, phase6Name, true));

    PipelinePlan expected = new PipelinePlan(phases, phaseConnections);
    PipelinePlan actual = planner.plan(pipelineSpec);
    Assert.assertEquals(expected, actual);
  }

  private static String getPhaseName(String source, String... sinks) {
    Set<String> sources = ImmutableSet.of(source);
    Set<String> sinkNames = new HashSet<>();
    Collections.addAll(sinkNames, sinks);
    return PipelinePlanner.getPhaseName(sources, sinkNames);
  }

  private static PluginSpec connectorSpec(String originalName) {
    return new PluginSpec(Constants.CONNECTOR_TYPE, "connector",
                          ImmutableMap.of(Constants.CONNECTOR_ORIGINAL_NAME, originalName), null);
  }
}
