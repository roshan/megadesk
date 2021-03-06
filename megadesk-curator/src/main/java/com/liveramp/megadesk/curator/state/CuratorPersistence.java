/**
 *  Copyright 2014 LiveRamp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.liveramp.megadesk.curator.state;

import org.apache.curator.framework.CuratorFramework;

import com.liveramp.commons.util.serialization.SerializationHandler;
import com.liveramp.megadesk.core.state.Persistence;
import com.liveramp.megadesk.core.state.PersistenceTransaction;
import com.liveramp.megadesk.recipes.state.persistence.SerializationPersistence;

public class CuratorPersistence<VALUE> extends SerializationPersistence<VALUE> implements Persistence<VALUE> {

  private CuratorFramework curator;
  private final String path;

  public CuratorPersistence(CuratorFramework curator, String path, SerializationHandler<VALUE> serializer) {
    super(serializer);
    this.curator = curator;
    this.path = path;

    try {
      if (curator.checkExists().forPath(path) == null) {
        curator.create().creatingParentsIfNeeded().forPath(path);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected byte[] readBytes() {
    try {
      return curator.getData().forPath(path);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void writeBytes(byte[] serializedValue) {
    try {
      curator.setData().forPath(path, serializedValue);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object transactionCategory() {
    return curator;
  }

  @Override
  public PersistenceTransaction newTransaction() {
    return new CuratorPersistenceTransaction(curator.inTransaction());
  }

  @Override
  public void writeInTransaction(PersistenceTransaction transaction, byte[] serializedValue) {
    try {
      ((CuratorPersistenceTransaction)transaction).transaction().setData().forPath(path, serializedValue);
    } catch (Exception e) {
      throw new RuntimeException(e); // TODO
    }
  }
}
