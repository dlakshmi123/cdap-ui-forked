/*
 * Copyright © 2014 Cask Data, Inc.
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

package co.cask.cdap;

import co.cask.cdap.api.annotation.Output;
import co.cask.cdap.api.annotation.ProcessInput;
import co.cask.cdap.api.annotation.UseDataSet;
import co.cask.cdap.api.app.AbstractApplication;
import co.cask.cdap.api.data.stream.Stream;
import co.cask.cdap.api.dataset.lib.KeyValueTable;
import co.cask.cdap.api.flow.Flow;
import co.cask.cdap.api.flow.FlowSpecification;
import co.cask.cdap.api.flow.flowlet.AbstractFlowlet;
import co.cask.cdap.api.flow.flowlet.FlowletSpecification;
import co.cask.cdap.api.flow.flowlet.OutputEmitter;
import co.cask.cdap.api.flow.flowlet.StreamEvent;
import com.google.common.collect.Lists;

import java.net.URI;
import java.util.List;

/**
 * This is a Toy Application used for only testing.
 */
public class ToyApp extends AbstractApplication {
  @Override
  public void configure() {
    setName("ToyApp");
    setDescription("Toy Flow Application");
    addStream(new Stream("X"));
    addStream(new Stream("Y"));
    createDataset("data1", KeyValueTable.class);
    addFlow(new ToyFlow());
  }

  /**
   *
   */
  public static final class ToyFlow implements Flow {
    @Override
    public FlowSpecification configure() {
      return FlowSpecification.Builder.with()
        .setName("ToyFlow")
        .setDescription("Complex Toy Flow")
        .withFlowlets()
          .add(new A())
          .add(new B())
          .add(new C())
          .add(new D())
          .add(new E())
          .add(new F())
          .add(new G())
        .connect()
          .fromStream("X").to("A")
          .fromStream("Y").to("B")
          .from("A").to("C")
          .from("B").to("E")
          .from("A").to("E")
          .from("C").to("D")
          .from("C").to("F")
          .from("D").to("G")
          .from("F").to("G")
          .from("E").to("G")
        .build();
    }
  }

  /**
   *
   */
  public static final class A extends AbstractFlowlet {
    @UseDataSet("data1")
    private KeyValueTable myDataSet;

    private OutputEmitter<String> out;
    @Output("out1")
    private OutputEmitter<Float> out1;

    public A() {
      super("A");
    }

    @Override
    public FlowletSpecification configure() {
      return FlowletSpecification.Builder.with()
        .setName("A")
        .setDescription("A flowlet")
        .useDataSet("data2", "data3")
        .build();
    }

    @ProcessInput
    public void process(StreamEvent event) {
      KeyValueTable table = getContext().getDataset("data2");
      out.emit("out");
      out1.emit(2.3f);
    }
  }

  /**
   *
   */
  public static final class B extends AbstractFlowlet {
    private OutputEmitter<Boolean> out;

    public B() {
      super("B");
    }

    @ProcessInput
    public void process(StreamEvent event) {
      out.emit(false);
    }
  }

  /**
   *
   */
  public static final class C extends AbstractFlowlet {
    @Output("c1")
    private OutputEmitter<Long> c1;

    @Output("c2")
    private OutputEmitter<Integer> c2;

    public C() {
      super("C");
    }

    @ProcessInput
    public void process(String a) {
      c1.emit(1L);
      c2.emit(1);
    }
  }

  /**
   *
   */
  public static final class E extends AbstractFlowlet {
    private OutputEmitter<Double> out;

    public E() {
      super("E");
    }

    @ProcessInput("out1")
    void process(Float f) {
      out.emit(1.2);
    }

    @ProcessInput
    void process(Boolean b) {
      out.emit(1.5);
    }
  }

  /**
   *
   */
  public static final class D extends AbstractFlowlet {
    @Output("d1")
    private OutputEmitter<List<String>> out;

    public D() {
      super("D");
    }

    @ProcessInput("c1")
    void process(Long l) {
      List<String> p = Lists.newArrayList();
      out.emit(p);
    }
  }

  /**
   *
   */
  public static final class F extends AbstractFlowlet {
    @Output("f1")
    private OutputEmitter<URI> f1;

    public F() {
      super("F");
    }

    @ProcessInput("c2")
    void process(Integer i) {
      f1.emit(URI.create("http://www.google.com"));
    }
  }

  /**
   *
   */
  public static final class G extends AbstractFlowlet {
    public G() {
      super("G");
    }

    @ProcessInput("d1")
    public void process(List<String> s) {

    }

    @ProcessInput("f1")
    public void process(URI uri) {

    }

    @ProcessInput
    public void process(Double d) {

    }
  }
}
