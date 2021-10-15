<#assign tableName = table.tableName/>
<#assign className = table.pojoName/>
<#assign propertys = table.properties2/>
<#assign columns = table.propertiesAnColumns/>
<#assign types = table.properties/>
<#assign k = table.key/>
<#assign p = package/>
package ${p};

import cn.ft.annotation.Column;
import cn.ft.annotation.Table;
import cn.ft.annotation.Entity;
import cn.ft.annotation.Id;
import lombok.Data;
import lombok.experimental.Accessors;


/**
* @author kangnan.chang
*/

@Accessors(chain=true)
@Table(value = "${tableName}")
@Entity
@Data
public class ${className} {
<#list propertys as p>

<#if p.propertyName = k>
@Id
</#if>
@Column(name = "${columns["${p.propertyName}"]}")
private ${types["${p.propertyName}"]} ${p.propertyName};

</#list>

}
