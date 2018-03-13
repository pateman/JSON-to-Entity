package ${packageName};

<#list imports?keys as imp>
<#if !imp?starts_with("java.lang")>
import ${imp};
</#if>
</#list>

@Entity
@Table(name = "${entity.deployment}")
public class ${entity.name}Entity {

  <#list entity.attributes as attr>
  <#assign attrType = imports[attr.type]>
  <#if attr.flags?seq_contains("PRIMARY_KEY")>
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
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