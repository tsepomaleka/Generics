package test;

import java.util.Collection;
import org.liquid.model.condition.Condition;
import org.liquid.model.enumeration.JoinType;
import org.liquid.sql.Liquid;
import org.liquid.util.scanner.ColumnScannerException;

public class TestClass 
{
    public static void main(String[] args) throws ColumnScannerException  
    {
        Liquid liquid = Liquid.getInstance();
        
        Collection collection = 
                liquid.createAlias(Student.class, "student")
                .addJoin("student.faculty", JoinType.INNER_JOIN)
                .addIntermediateJoin("student.registeredModules", JoinType.LEFT_OUTER_JOIN)
                .addCondition(Condition.and(
                        Condition.equals("student.studentNumber", 200916023),
                        Condition.equals("student.registered", true)))
                .executeAndFetch();
        
        for (Object o : collection)
        {
            Student student = (Student)o;
            System.out.println(student.toString());
        }
    }
}
