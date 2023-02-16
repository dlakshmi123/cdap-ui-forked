package com.continuuity.app.program;

import com.continuuity.app.ApplicationSpecification;
import com.continuuity.app.Id;
import com.continuuity.common.lang.jar.JarResources;
import com.continuuity.common.lang.jar.ProgramClassLoader;
import com.continuuity.common.lang.jar.ProgramJarResources;
import com.continuuity.internal.app.ApplicationSpecificationAdapter;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import org.apache.twill.filesystem.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Default implementation of program.
 */
public final class DefaultProgram implements Program {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultProgram.class);
  private final ClassLoader classLoader;
  private final String mainClassName;
  private final Type processorType;
  private final ApplicationSpecification specification;
  private final Id.Program id;
  private final Location programJarLocation;

  DefaultProgram(Location programJarLocation, File bundleJarFolder, final ProgramJarResources jarResources,
                 ClassLoader parentClassLoader) throws IOException {
    this.programJarLocation = programJarLocation;
    this.classLoader = new ProgramClassLoader(bundleJarFolder, parentClassLoader);

    Manifest manifest = jarResources.getManifest();

    mainClassName = getAttribute(manifest, ManifestFields.MAIN_CLASS);
    String accountId = getAttribute(manifest, ManifestFields.ACCOUNT_ID);
    String applicationId = getAttribute(manifest, ManifestFields.APPLICATION_ID);
    String programName = getAttribute(manifest, ManifestFields.PROGRAM_NAME);
    id = Id.Program.from(accountId, applicationId, programName);

    String type = getAttribute(manifest, ManifestFields.PROCESSOR_TYPE);
    processorType = type == null ? null : Type.valueOf(type);

    final String appSpecFile = getAttribute(manifest, ManifestFields.SPEC_FILE);

    specification = appSpecFile == null ? null : ApplicationSpecificationAdapter.create()
      .fromJson(CharStreams.newReaderSupplier(
        new InputSupplier<InputStream>() {
          @Override
          public InputStream getInput() throws IOException {
            return jarResources.getResourceAsStream(appSpecFile);
          }
        },
        Charsets.UTF_8)
      );
  }

  // TODO: Remove this.
  public DefaultProgram(Location programJarLocation,
                        final JarResources jarResources, ClassLoader classLoader) throws IOException {
    this.programJarLocation = programJarLocation;
    this.classLoader = classLoader;

    Manifest manifest = jarResources.getManifest();

    mainClassName = getAttribute(manifest, ManifestFields.MAIN_CLASS);
    String accountId = getAttribute(manifest, ManifestFields.ACCOUNT_ID);
    String applicationId = getAttribute(manifest, ManifestFields.APPLICATION_ID);
    String programName = getAttribute(manifest, ManifestFields.PROGRAM_NAME);
    id = Id.Program.from(accountId, applicationId, programName);

    String type = getAttribute(manifest, ManifestFields.PROCESSOR_TYPE);
    processorType = type == null ? null : Type.valueOf(type);

    final String appSpecFile = getAttribute(manifest, ManifestFields.SPEC_FILE);

    specification = appSpecFile == null ? null : ApplicationSpecificationAdapter.create()
      .fromJson(CharStreams.newReaderSupplier(
        new InputSupplier<InputStream>() {
          @Override
          public InputStream getInput() throws IOException {
            return jarResources.getResourceAsStream(appSpecFile);
          }
        },
        Charsets.UTF_8)
      );
  }

  @Override
  public <T> Class<T> getMainClass() throws ClassNotFoundException {
    return (Class<T>) classLoader.loadClass(mainClassName);
  }

  @Override
  public Type getType() {
    return processorType;
  }

  @Override
  public Id.Program getId() {
    return id;
  }

  @Override
  public String getName() {
    return id.getId();
  }

  @Override
  public String getAccountId() {
    return id.getAccountId();
  }

  @Override
  public String getApplicationId() {
    return id.getApplicationId();
  }

  @Override
  public ApplicationSpecification getSpecification() {
    return specification;
  }

  @Override
  public Location getJarLocation() {
    return programJarLocation;
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  private String getAttribute(Manifest manifest, Attributes.Name name) throws IOException {
    Preconditions.checkNotNull(manifest);
    Preconditions.checkNotNull(name);
    String value = manifest.getMainAttributes().getValue(name);
    check(value != null, "Fail to get %s attribute from jar", name);
    return value;
  }

  private void check(boolean condition, String fmt, Object... objs) throws IOException {
    if (!condition) {
      throw new IOException(String.format(fmt, objs));
    }
  }
}
