package test;

import org.liquid.model.annotation.Column;
import org.liquid.model.annotation.Id;
import org.liquid.model.annotation.JoinColumn;
import org.liquid.model.annotation.JoinTable;
import org.liquid.model.annotation.OneToMany;
import org.liquid.model.annotation.OneToOne;
import org.liquid.model.annotation.Persistent;
import org.liquid.model.annotation.Table;
import java.io.Serializable;
import java.util.Collection;

@Persistent
@Table(name = "student")
public class Student extends Person implements Serializable
{
    @Id
    @Column(name = "student_number")
    private long studentNumber;
    
    @Column(name = "first_names")
    private String firstNames;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "is_registered")
    private boolean registered;
    
    @JoinColumn(targetClass = Faculty.class, columnName = "faculty_id", referencedColumnName = "faculty_id")
    @OneToOne
    private Faculty faculty;
    
    @OneToMany
    @JoinTable
    (
            intermediateTableName = "student_registered_modules",
            
            leftTargetClass = Student.class, 
            leftReferenceColumnName = "student_number", 
            leftIntermediateColumnName = "student_number",
            
            rightTargetClass = Module.class,
            rightReferenceColumnName = "module_id",
            rightIntermediateReferenceColumnName = "module_id"
    )
    private Collection<Module> registeredModules;
    
    public Student()
    {
        
    }

    public long getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(long studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public void setFirstNames(String firstNames) {
        this.firstNames = firstNames;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) 
    {
        this.faculty = faculty;
    }

    public Collection<Module> getRegisteredModules() 
    {
        return registeredModules;
    }

    public void setRegisteredModules(Collection<Module> registeredModules) 
    {
        this.registeredModules = registeredModules;
    }

    @Override
    public String toString() {
        
        return super.toString() + "\n"
                + "Student{" + "studentNumber=" + studentNumber + ", firstNames=" + firstNames + ", lastName=" + lastName + ", registered=" + registered + ", faculty=" + faculty + ", registeredModules=" + registeredModules + '}';
    }
    
    
}
