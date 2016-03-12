package test;

import org.liquid.model.annotation.Column;
import org.liquid.model.annotation.Id;
import org.liquid.model.annotation.Persistent;
import org.liquid.model.annotation.Table;

@Persistent
@Table(name = "module")
public class Module 
{
    @Id
    @Column(name = "module_id")
    private long moduleId;
    
    @Column(name = "module_code")
    private String moduleCode;
    
    @Column(name = "module_name")
    private String moduleName;
    
    public Module() {}

    public long getModuleId() {
        return moduleId;
    }

    public void setModuleId(long moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public String toString() {
        return "Module{" + "moduleId=" + moduleId + ", moduleCode=" + moduleCode + ", moduleName=" + moduleName + '}';
    }
    
    
}
