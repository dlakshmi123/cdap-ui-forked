package com.continuuity.security.guice;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.security.auth.InMemoryKeyManager;
import com.continuuity.security.auth.KeyManager;
import com.google.common.base.Throwables;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Guice bindings for InMemoryKeyManagers. This extends {@code SecurityModule} to provide
 * an instance of {@code InMemoryKeyManager}.
 */
public class InMemorySecurityModule extends SecurityModule {

  @Override
  protected void bindKeyManager(Binder binder) {
    binder.bind(KeyManager.class).toProvider(InMemoryKeyManagerProvider.class);
  }

  private static final class InMemoryKeyManagerProvider implements Provider<KeyManager> {
    private CConfiguration cConf;

    @Inject
    InMemoryKeyManagerProvider(CConfiguration conf) {
      this.cConf = conf;
    }

    @Override
    public KeyManager get() {
      InMemoryKeyManager keyManager = new InMemoryKeyManager(cConf);
      try {
        keyManager.init();
      } catch (NoSuchAlgorithmException nsae) {
        throw Throwables.propagate(nsae);
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
      return keyManager;
    }
  };
}
