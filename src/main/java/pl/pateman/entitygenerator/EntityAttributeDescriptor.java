package pl.pateman.entitygenerator;

import java.util.Collection;
import java.util.Collections;

final class EntityAttributeDescriptor {

  private String name;
  private String type;
  private Boolean reintroduce;
  private Collection<String> flags;

  private EntityAttributeDescriptor() {
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public boolean isReintroduced() {
    return this.reintroduce == null ? false : this.reintroduce;
  }

  public Collection<String> getFlags() {
    return this.flags == null ? Collections.emptyList() : Collections.unmodifiableCollection(this.flags);
  }
}
