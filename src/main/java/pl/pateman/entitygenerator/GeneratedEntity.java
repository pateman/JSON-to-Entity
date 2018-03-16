package pl.pateman.entitygenerator;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;

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

  /**
   * Returns an attribute that matches the given predicate.
   *
   * @param predicate A valid predicate that should be used to locate an attribute.
   * @return An {@link Optional<Attribute>} which holds a reference to the attribute.
   */
  public Optional<Attribute> findAttribute(final Predicate<Attribute> predicate) {
    if (predicate == null) {
      throw new IllegalArgumentException("A valid predicate is required");
    }

    if (this.attributes == null) {
      return Optional.empty();
    }

    return this.attributes.stream().filter(predicate).findFirst();
  }

  /**
   * Finds an attribute under the given name.
   *
   * @param attributeName Name of the attribute to find.
   * @return An {@link Optional<Attribute>} which holds a reference to the attribute.
   */
  public Optional<Attribute> findAttribute(final String attributeName) {
    if (StringUtils.isBlank(attributeName)) {
      throw new IllegalArgumentException("A valid attribute name is required");
    }
    return this.findAttribute(a -> attributeName.equalsIgnoreCase(a.getName()));
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
   * Entity relation metadata.
   */
  public static final class RelationInfo {

    /**
     * Collection type of the relation.
     */
    public enum CollectionType {
      LIST,
      SET
    }

    /**
     * Relation side.
     *
     * A side is either {@code ONE} or {@code MANY}. For example, in a one-to-many relation, the source of the relation
     * will have its {@link RelationInfo#side) set to {@code ONE}, and the target to {@code MANY}.
     */
    public enum Side {
      ONE,
      MANY
    }

    /**
     * Relation type.
     */
    public enum Type {
      ONE_TO_ONE,
      ONE_TO_MANY,
      MANY_TO_MANY
    }

    private GeneratedEntity target;
    private Attribute targetAttribute;
    private String joinTable;
    private String joinColumn;
    private CollectionType collectionType;
    private Side side;
    private boolean source;

    RelationInfo() {

    }

    /**
     * Determines what type of relation it is.
     *
     * @return The relation {@link Type}.
     */
    public Type getType() {
      final Side targetSide = this.targetAttribute.getRelationInfo().getSide();
      if (Side.ONE.equals(this.side) && Side.ONE.equals(targetSide)) {
        return Type.ONE_TO_ONE;
      } else if ((Side.ONE.equals(this.side) && Side.MANY.equals(targetSide)) || (Side.MANY.equals(this.side)
          && Side.ONE.equals(targetSide))) {
        return Type.ONE_TO_MANY;
      } else if (Side.MANY.equals(this.side) && Side.MANY.equals(targetSide)) {
        return Type.MANY_TO_MANY;
      } else {
        throw new IllegalStateException("Invalid relation type");
      }
    }

    /**
     * Returns the target side of the relation.
     *
     * @return A {@link GeneratedEntity} which is the target side of this relation.
     */
    public GeneratedEntity getTarget() {
      return target;
    }

    void setTarget(GeneratedEntity target) {
      this.target = target;
    }

    /**
     * Returns the target attribute of the relation. The attribute is owned by the target side of the relation.
     *
     * @return A target {@link Attribute} of the relation.
     */
    public Attribute getTargetAttribute() {
      return targetAttribute;
    }

    void setTargetAttribute(Attribute targetAttribute) {
      this.targetAttribute = targetAttribute;
    }

    /**
     * Returns the join table of this relation.
     *
     * @return Relation join table.
     */
    public String getJoinTable() {
      return joinTable;
    }

    void setJoinTable(String joinTable) {
      this.joinTable = joinTable;
    }

    /**
     * Returns the join column of this relation.
     *
     * @return Relation join column.
     */
    public String getJoinColumn() {
      return joinColumn;
    }

    void setJoinColumn(String joinColumn) {
      this.joinColumn = joinColumn;
    }

    /**
     * Returns the collection type of this relation.
     *
     * @return Relation collection type.
     */
    public CollectionType getCollectionType() {
      return collectionType;
    }

    void setCollectionType(CollectionType collectionType) {
      this.collectionType = collectionType;
    }

    /**
     * Returns the side of this relation.
     *
     * @see Side
     * @return Relation side.
     */
    public Side getSide() {
      return side;
    }

    void setSide(Side side) {
      this.side = side;
    }

    /**
     * Determines whether this entity is the source of this relation.
     *
     * @return {@code true} if it is, {@code false} otherwise.
     */
    public boolean isSource() {
      return source;
    }

    void setSource(boolean source) {
      this.source = source;
    }
  }

  /**
   * Entity attribute metadata.
   */
  public static final class Attribute {

    private String name;
    private String type;
    private boolean reintroduced;
    private Collection<String> flags;
    private RelationInfo relationInfo;

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
     * Returns the fully-qualified class name of the attribute.
     *
     * @return Attribute type.
     */
    public String getType() {
      return type;
    }

    void setType(String type) {
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

    public RelationInfo getRelationInfo() {
      return relationInfo;
    }

    void setRelationInfo(RelationInfo relationInfo) {
      this.relationInfo = relationInfo;
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
