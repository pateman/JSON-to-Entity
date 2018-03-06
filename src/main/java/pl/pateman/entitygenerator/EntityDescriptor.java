package pl.pateman.entitygenerator;

import java.util.Collection;
import java.util.Collections;

final class EntityDescriptor {

  private String name;
  private String deployment;
  private EntityRootDescriptor root;
  private Collection<EntityAttributeDescriptor> attributes;

  private EntityDescriptor() {

  }

  public String getName() {
    return name;
  }

  public String getDeployment() {
    return deployment;
  }

  public EntityRootDescriptor getRoot() {
    return root;
  }

  public Collection<EntityAttributeDescriptor> getAttributes() {
    return attributes == null ? Collections.emptyList() : Collections.unmodifiableCollection(attributes);
  }
}
