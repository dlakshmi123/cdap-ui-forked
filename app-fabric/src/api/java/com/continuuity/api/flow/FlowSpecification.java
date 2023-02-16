/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */

package com.continuuity.api.flow;

import com.continuuity.api.data.stream.Stream;
import com.continuuity.api.flow.flowlet.Flowlet;
import com.continuuity.internal.flow.DefaultFlowSpecification;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * This class provides the specification of a Flow. Instances of this class should be created through
 * the {@link Builder} class by invoking the {@link Builder#with()} method.
 *
 * <pre>
 * {@code
 * FlowSpecification flowSpecification =
 *      FlowSpecification.Builder.with()
 *        .setName("tokenCount")
 *        .setDescription("Token counting flow")
 *        .withFlowlets().add("source", new StreamSource())
 *                       .add("tokenizer", new Tokenizer())
 *                       .add("count", new CountByField())
 *        .connect().fromStream("text").to("source")
 *                  .from("source").to("tokenizer")
 *                  .from("tokenizer").to("count")
 *        .build();
 * }
 * </pre>
 */
public interface FlowSpecification {

  /**
   * @return Class name of the {@link Flow}.
   */
  String getClassName();

  /**
   * @return Name of the {@link Flow}.
   */
  String getName();

  /**
   * @return Description of the {@link Flow}.
   */
  String getDescription();

  /**
   * @return Immutable Map from flowlet name to {@link FlowletDefinition}.
   */
  Map<String, FlowletDefinition> getFlowlets();

  /**
   * @return Immutable list of {@link FlowletConnection}s.
   */
  List<FlowletConnection> getConnections();

  /**
   * Defines builder for building connections or topology for a flow.
   */
  static final class Builder {
    private String name;
    private String description;
    private final Map<String, FlowletDefinition> flowlets = Maps.newHashMap();
    private final List<FlowletConnection> connections = Lists.newArrayList();

    /**
     * Creates a {@link Builder} for building instance of {@link FlowSpecification}.
     *
     * @return a new builder instance.
     */
    public static NameSetter with() {
      return new Builder().new NameSetter();
    }

    /**
     * Class for setting flow name.
     */
    public final class NameSetter {

      /**
       * Sets the name of the Flow
       * @param name of the flow.
       * @return An instance of {@link DescriptionSetter}
       */
      public DescriptionSetter setName(String name) {
        Preconditions.checkArgument(name != null, "Name cannot be null.");
        Builder.this.name = name;
        return new DescriptionSetter();
      }
    }

    /**
     * Defines a class for defining the actual description.
     */
    public final class DescriptionSetter {

      /**
       * Sets the description for the flow.
       * @param description of the flow.
       * @return A instance of {@link AfterDescription}
       */
      public AfterDescription setDescription(String description) {
        Preconditions.checkArgument(description != null, "Description cannot be null.");
        Builder.this.description = description;
        return new AfterDescription();
      }
    }

    /**
     * Defines a class that represents what needs to happen after a description
     * has been added.
     */
    public final class AfterDescription {

      /**
       * @return An instance of {@link FlowletAdder} for adding flowlets to specification.
       */
      public FlowletAdder withFlowlets() {
        return new MoreFlowlet();
      }
    }

    /**
     * FlowletAdder is responsible for capturing the information of a Flowlet during the
     * specification creation.
     */
    public interface FlowletAdder {

      /**
       * Add a flowlet to the flow.
       * @param flowlet {@link Flowlet} instance to be added to flow.
       * @return An instance of {@link MoreFlowlet} for adding more flowlets
       */
      MoreFlowlet add(Flowlet flowlet);

      /**
       * Add a flowlet to the flow with minimum number of instances to begin with.
       * @param flowlet {@link Flowlet} instance to be added to flow.
       * @param instances Number of instances for the flowlet
       * @return An instance of {@link MoreFlowlet} for adding more flowlets
       */
      MoreFlowlet add(Flowlet flowlet, int instances);

      /**
       * Add a flowlet to flow with the given name. The name given would overrides the one
       * in {@link com.continuuity.api.flow.flowlet.FlowletSpecification#getName() FlowletSpecification.getName()}
       * returned by {@link Flowlet#configure()}.
       * @param name Name of the flowlet
       * @param flowlet {@link Flowlet} instance to be added to flow.
       * @return An instance of {@link MoreFlowlet} for adding more flowlets.
       */
      MoreFlowlet add(String name, Flowlet flowlet);

      /**
       * Add a flowlet to flow with the given name with minimum number of instances to begin with.
       * The name given would overrides the one
       * in {@link com.continuuity.api.flow.flowlet.FlowletSpecification#getName() FlowletSpecification.getName()}
       * returned by {@link Flowlet#configure()}.
       * @param name Name of the flowlet
       * @param flowlet {@link Flowlet} instance to be added to flow.
       * @return An instance of {@link MoreFlowlet} for adding more flowlets.
       */
      MoreFlowlet add(String name, Flowlet flowlet, int instances);
    }

    /**
     * This class allows more flowlets to be defined. This is part of a controlled builder.
     */
    public final class MoreFlowlet implements FlowletAdder {

      @Override
      public MoreFlowlet add(Flowlet flowlet) {
        return add(flowlet, 1);
      }

      @Override
      public MoreFlowlet add(final Flowlet flowlet, int instances) {
        return add(null, flowlet, instances);
      }

      @Override
      public MoreFlowlet add(String name, Flowlet flowlet) {
        return add(name, flowlet, 1);
      }

      @Override
      public MoreFlowlet add(String name, Flowlet flowlet, int instances) {
        Preconditions.checkArgument(flowlet != null, "Flowlet cannot be null");
        Preconditions.checkArgument(instances > 0, "Number of instances must be > 0");

        FlowletDefinition flowletDef = new FlowletDefinition(name, flowlet, instances);
        String flowletName = flowletDef.getFlowletSpec().getName();
        Preconditions.checkArgument(!flowlets.containsKey(flowletName), "Flowlet %s already defined", flowletName);
        flowlets.put(flowletName, flowletDef);

        return this;
      }

      /**
       * Defines a connection between two flowlets.
       * @return An instance of {@link ConnectFrom}
       */
      public ConnectFrom connect() {
        return new Connector();
      }
    }

    /**
     * Defines the starting flowlet for a connection.
     */
    public interface ConnectFrom {

      /**
       * Defines the flowlet that is at the start of the connection.
       * @param flowlet that is at the start of connection.
       * @return An instance of {@link ConnectTo} specifying the flowlet it will connect to.
       */
      ConnectTo from(Flowlet flowlet);

      /**
       * Defines the stream that the connection is reading from.
       * @param stream Instance of stream.
       * @return An instance of {@link ConnectTo} specifying the flowlet it will connect to.
       */
      ConnectTo from(Stream stream);

      /**
       * Defines the flowlet that is at the start of the connection by the flowlet name.
       * @param flowlet Name of the flowlet.
       * @return And instance of {@link ConnectTo} specifying the flowlet it will connect to.
       */
      ConnectTo from(String flowlet);

      /**
       * Defines the stream that the connection is reading from by the stream name.
       * @param stream Instance of stream.
       * @return An instance of {@link ConnectTo} specifying the flowlet it will connect to.
       */
      ConnectTo fromStream(String stream);
    }

    /**
     * Class defining the connect to interface for a connection.
     */
    public interface ConnectTo {

      /**
       * Defines the flowlet that the connection is connecting to.
       * @param flowlet the connection connects to.
       * @return A instance of {@link MoreConnect} to define more connections of flowlets in a flow.
       */
      MoreConnect to(Flowlet flowlet);

      /**
       * Defines the flowlet that connection is connecting to by the flowlet name.
       * @param flowlet Name of the flowlet the connection connects to.
       * @return A instance of {@link MoreConnect} to define more connections of flowlets in a flow.
       */
      MoreConnect to(String flowlet);
    }

    /**
     * Interface defines the building of FlowSpecification.
     */
    public interface MoreConnect extends ConnectFrom {
      /**
       * Constructs a {@link FlowSpecification}
       * @return An instance of {@link FlowSpecification}
       */
      FlowSpecification build();
    }

    /**
     * Class defines the connection between two flowlets.
     */
    public final class Connector implements ConnectFrom, ConnectTo, MoreConnect {

      private String fromStream;
      private FlowletDefinition fromFlowlet;

      @Override
      public ConnectTo from(Flowlet flowlet) {
        Preconditions.checkArgument(flowlet != null, "Flowlet cannot be null");
        return from(flowlet.configure().getName());
      }

      @Override
      public ConnectTo from(Stream stream) {
        Preconditions.checkArgument(stream != null, "Stream cannot be null");
        return fromStream(stream.configure().getName());
      }

      @Override
      public ConnectTo from(String flowlet) {
        Preconditions.checkArgument(flowlets.containsKey(flowlet), "Undefined flowlet %s", flowlet);
        fromFlowlet = flowlets.get(flowlet);
        fromStream = null;
        return this;
      }

      @Override
      public ConnectTo fromStream(String stream) {
        Preconditions.checkArgument(stream != null, "Stream cannot be null");
        fromFlowlet = null;
        fromStream = stream;
        return this;
      }

      @Override
      public MoreConnect to(Flowlet flowlet) {
        Preconditions.checkArgument(flowlet != null, "Flowlet cannot be null");
        return to(flowlet.configure().getName());
      }

      @Override
      public MoreConnect to(String flowlet) {
        Preconditions.checkArgument(flowlet != null, "Flowlet cannot be null");
        Preconditions.checkArgument(flowlets.containsKey(flowlet), "Undefined flowlet %s", flowlet);

        FlowletConnection.Type sourceType;
        String sourceName;
        if (fromStream != null) {
          sourceType = FlowletConnection.Type.STREAM;
          sourceName = fromStream;
        } else {
          sourceType = FlowletConnection.Type.FLOWLET;
          sourceName = fromFlowlet.getFlowletSpec().getName();
        }
        connections.add(new FlowletConnection(sourceType, sourceName, flowlet));
        return this;

      }

      @Override
      public FlowSpecification build() {
        return new DefaultFlowSpecification(name, description, flowlets, connections);
      }
    }

    private Builder() {
    }
  }
}