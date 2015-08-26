package org.terracotta.passthrough;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.terracotta.connection.entity.Entity;
import org.terracotta.connection.entity.EntityMaintenanceRef;
import org.terracotta.entity.EntityClientService;


public class PassthroughMaintenanceRef <T extends Entity, C> implements EntityMaintenanceRef<T, C> {
  private final PassthroughConnection passthroughConnection;
  private final EntityClientService<T, C> service;
  private final long version;
  private final Class<T> entityClass;
  private final String entityName;
  
  public PassthroughMaintenanceRef(PassthroughConnection passthroughConnection, EntityClientService<T, C> service, Class<T> clazz, long version, String name) {
    this.passthroughConnection = passthroughConnection;
    this.service = service;
    this.version = version;
    this.entityClass = clazz;
    this.entityName = name;
  }

  @Override
  public boolean doesExist() {
    PassthroughMessage getMessage = PassthroughMessageCodec.createExistsMessage(this.entityClass, this.entityName, this.version);
    Future<byte[]> received = this.passthroughConnection.sendInternalMessageAfterAcks(getMessage);
    boolean doesExist = false;
    try {
      received.get();
      doesExist = true;
    } catch (InterruptedException e) {
      Assert.fail(e);
    } catch (ExecutionException e) {
      Assert.fail(e);
    }
    return doesExist;
  }

  @Override
  public void destroy() {
    PassthroughMessage getMessage = PassthroughMessageCodec.createDestroyMessage(this.entityClass, this.entityName);
    Future<byte[]> received = this.passthroughConnection.sendInternalMessageAfterAcks(getMessage);
    try {
      received.get();
    } catch (InterruptedException e) {
      Assert.fail(e);
    } catch (ExecutionException e) {
      Assert.fail(e);
    }
  }

  @Override
  public void create(C configuration) {
    byte[] serializedConfiguration = this.service.serializeConfiguration(configuration);
    PassthroughMessage getMessage = PassthroughMessageCodec.createCreateMessage(this.entityClass, this.entityName, this.version, serializedConfiguration);
    Future<byte[]> received = this.passthroughConnection.sendInternalMessageAfterAcks(getMessage);
    try {
      received.get();
    } catch (InterruptedException e) {
      Assert.fail(e);
    } catch (ExecutionException e) {
      // This means there was actually a problem.
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub
    
  }
}
