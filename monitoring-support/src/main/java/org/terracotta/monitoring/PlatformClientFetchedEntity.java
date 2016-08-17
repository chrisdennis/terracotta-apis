/*
 *
 *  The contents of this file are subject to the Terracotta Public License Version
 *  2.0 (the "License"); You may not use this file except in compliance with the
 *  License. You may obtain a copy of the License at
 *
 *  http://terracotta.org/legal/terracotta-public-license.
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 *  the specific language governing rights and limitations under the License.
 *
 *  The Covered Software is Terracotta Core.
 *
 *  The Initial Developer of the Covered Software is
 *  Terracotta, Inc., a Software AG company
 *
 */
package org.terracotta.monitoring;

import java.io.Serializable;

import com.tc.classloader.CommonComponent;
import org.terracotta.entity.ClientDescriptor;


/**
 * A type which describes the connection between a client and an entity, created by a fetch.
 * 
 * WARNING:  The clientDescriptor is marked as transient since it can't be meaningfully deserialized and the cases where this
 *  object will be used are also cases where it won't be serialized.  Be careful of using this object in these other cases.
 */
@CommonComponent
public class PlatformClientFetchedEntity implements Serializable {
  private static final long serialVersionUID = 4741752382657834201L;

  public String clientIdentifier;
  public String entityIdentifier;
  public transient ClientDescriptor clientDescriptor;

  public PlatformClientFetchedEntity() {
    // For Serializable.
  }

  public PlatformClientFetchedEntity(String clientIdentifier, String entityIdentifier, ClientDescriptor clientDescriptor) {
    this.clientIdentifier = clientIdentifier;
    this.entityIdentifier = entityIdentifier;
    this.clientDescriptor = clientDescriptor;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("PlatformClientFetchedEntity{");
    sb.append("clientDescriptor=").append(clientDescriptor);
    sb.append(", clientIdentifier='").append(clientIdentifier).append('\'');
    sb.append(", entityIdentifier='").append(entityIdentifier).append('\'');
    sb.append('}');
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return this.clientIdentifier.hashCode()
        ^ this.entityIdentifier.hashCode()
        ^ this.clientDescriptor.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    boolean doesMatch = (this == other);
    if (!doesMatch && (null != other) && (getClass() == other.getClass()))
    {
      final PlatformClientFetchedEntity that = (PlatformClientFetchedEntity) other;
      doesMatch = this.clientIdentifier.equals(that.clientIdentifier)
          && this.entityIdentifier.equals(that.entityIdentifier)
          && (((null == this.clientDescriptor) && (null == that.clientDescriptor)) || this.clientDescriptor.equals(that.clientDescriptor));
    }
    return doesMatch;
  }
}
