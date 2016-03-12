package test;

import org.liquid.model.annotation.Column;
import org.liquid.model.annotation.MappedSuperClass;
import org.liquid.model.annotation.Persistent;

@Persistent
@MappedSuperClass
public class Person 
{
    @Column(name = "identity_number")
    private String identityNumber;
    
    @Column(name = "gender")
    private boolean gender;
    
    public Person()
    {
        
    }

    public String getIdentityNumber() {
        return identityNumber;
    }

    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "Person{" + "identityNumber=" + identityNumber + ", gender=" + gender + '}';
    }
    
    
}
