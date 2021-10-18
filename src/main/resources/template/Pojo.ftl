<#assign tableName = table.tableName/>
<#assign className = table.pojoName/>
<#assign propertys = table.columns/>
package ${table.pojoPackagePath};

import cn.ft.annotation.Column;
import cn.ft.annotation.Table;
import cn.ft.annotation.Entity;
import cn.ft.annotation.Id;
import lombok.Data;
import lombok.experimental.Accessors;
<#list table.packages as package>
${package}
</#list>

/**
* @author kangnan.chang
*/

@Accessors(chain=true)
@Table(value = "${tableName}")
@Entity
@Data
public class ${className} {
<#list propertys as p>

<#if p.key==true>
    @Id
</#if>
    @Column(name = "${p.columnName}")
    private ${p.propertyType} ${p.propertyName};

</#list>

}
