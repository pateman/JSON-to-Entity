package ${packageName};

<#list imports?keys as imp>
<#if !imp?starts_with("java.lang")>
import ${imp?replace("\\<(.*)\\>", '', 'r')};
</#if>
</#list>

<#outputformat "plainText">
@Entity
@Table(name = "${entity.deployment}")
public class ${entity.name}Entity {

  <#list entity.attributes as attr>
    <#assign attrType = imports[attr.type]>
    <#if attr.flags?seq_contains("PRIMARY_KEY")>
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
    </#if>
    <#if attr.relationInfo??>
      <#assign rel = attr.relationInfo>
      <#assign relType = rel.getType()>
      <#if relType.name() == "ONE_TO_ONE">
  @OneToOne
      <#elseif relType.name() == "ONE_TO_MANY">
        <#if rel.side.name() == "ONE">
  @OneToMany
        <#else>
  @ManyToOne
        </#if>
      <#elseif relType.name() == "MANY_TO_MANY">
  @ManyToMany
      </#if>
    </#if>
  private ${attrType} ${attr.name};
  </#list>

  <#list entity.attributes as attr>
    <#assign capitalizedAttr = attr.name?cap_first>
    <#assign attrType = imports[attr.type]>
  public ${attrType} get${capitalizedAttr}() {
    return this.${attr.name};
  }

  public void set${capitalizedAttr}(${attrType} value) {
    this.${attr.name} = value;
  }

  </#list>
}
</#outputformat>