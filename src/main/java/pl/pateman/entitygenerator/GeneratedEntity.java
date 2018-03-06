package pl.pateman.entitygenerator;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public final class GeneratedEntity {

  private String name;
  private String deployment;
  private GeneratedEntity root;
  private Collection<Attribute> attributes;

  GeneratedEntity() {

  }

  public String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }

  public String getDeployment() {
    return deployment;
  }

  void setDeployment(String deployment) {
    this.deployment = deployment;
  }

  public GeneratedEntity getRoot() {
    return root;
  }

  void setRoot(GeneratedEntity root) {
    this.root = root;
  }

  public Collection<Attribute> getAttributes() {
    return attributes == null ? Collections.emptyList() : Collections.unmodifiableCollection(attributes);
  }

  void setAttributes(Collection<Attribute> attributes) {
    this.attributes = attributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GeneratedEntity that = (GeneratedEntity) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {

    return Objects.hash(name);
  }

  public static final class Attribute {

    private String name;
    private Class<?> type;
    private boolean reintroduced;
    private Collection<String> flags;

    Attribute() {

    }

    public String getName() {
      return name;
    }

    void setName(String name) {
      this.name = name;
    }

    public Class<?> getType() {
      return type;
    }

    void setType(Class<?> type) {
      this.type = type;
    }

    public boolean isReintroduced() {
      return reintroduced;
    }

    void setReintroduced(boolean reintroduced) {
      this.reintroduced = reintroduced;
    }

    public Collection<String> getFlags() {
      return Collections.unmodifiableCollection(flags);
    }

    void setFlags(Collection<String> flags) {
      this.flags = flags;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Attribute attribute = (Attribute) o;
      return Objects.equals(name, attribute.name);
    }

    @Override
    public int hashCode() {

      return Objects.hash(name);
    }
  }
}
