package test;

import org.liquid.model.annotation.Column;
import org.liquid.model.annotation.Id;
import org.liquid.model.annotation.Persistent;
import org.liquid.model.annotation.Table;

@Persistent
@Table(name = "faculty")
public class Faculty 
{
    @Id
    @Column(name = "faculty_id")
    private long facultyId;
    
    @Column(name = "faculty_name")
    private String facultyName;
    
    public Faculty() {}

    public long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(long facultyId) {
        this.facultyId = facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    @Override
    public String toString() {
        return "Faculty{" + "facultyId=" + facultyId + ", facultyName=" + facultyName + '}';
    }
    
    
}
