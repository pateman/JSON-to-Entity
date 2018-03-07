package pl.pateman.entitygenerator;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Entity metadata.
 */
public final class GeneratedEntity {

  private String name;
  private String deployment;
  private String classFile;
  private GeneratedEntity root;
  private Collection<Attribute> attributes;

  GeneratedEntity() {

  }

  /**
   * Returns the name of the entity.
   *
   * @return Entity name,
   */
  public String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the deployment of the entity, i.e. the table name that will be used in the database to store records.
   *
   * @return Entity deployment.
   */
  public String getDeployment() {
    return deployment;
  }

  void setDeployment(String deployment) {
    this.deployment = deployment;
  }

  /**
   * Returns the class file of the entity, i.e. a fully-qualified name of the class which will store the entity's
   * source code.
   *
   * @return Entity class file.
   */
  public String getClassFile() {
    return classFile;
  }

  void setClassFile(String classFile) {
    this.classFile = classFile;
  }

  /**
   * Returns the reference of the root entity of this entity. Note that the root is not mandatory and can be
   * {@code null}.
   *
   * @return Entity root.
   */
  public GeneratedEntity getRoot() {
    return root;
  }

  void setRoot(GeneratedEntity root) {
    this.root = root;
  }

  /**
   * Returns a read-only collection of attributes that belong to this entity.
   *
   * @return Entity attributes.
   */
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

  /**
   * Entity attribute metadata.
   */
  public static final class Attribute {

    private String name;
    private Class<?> type;
    private boolean reintroduced;
    private Collection<String> flags;

    Attribute() {

    }

    /**
     * Returns the name of the attribute.
     *
     * @return Attribute name.
     */
    public String getName() {
      return name;
    }

    void setName(String name) {
      this.name = name;
    }

    /**
     * Returns the class pointer which indicates what type the attribute is of.
     *
     * @return Attribute type.
     */
    public Class<?> getType() {
      return type;
    }

    void setType(Class<?> type) {
      this.type = type;
    }

    /**
     * Determines whether the attribute has been reintroduced.
     *
     * @return Whether the attribute has been reintroduced or not.
     */
    public boolean isReintroduced() {
      return reintroduced;
    }

    void setReintroduced(boolean reintroduced) {
      this.reintroduced = reintroduced;
    }

    /**
     * Returns a read-only collection of flags associated with this attribute.
     *
     * @return Attribute flags.
     */
    public Collection<String> getFlags() {
      return flags == null ? Collections.emptyList() : Collections.unmodifiableCollection(flags);
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
