package Tests;

import jdbc.SimpleDataSource;
import models.Mentor;
import models.Student;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import repositories.StudentsRepository;
import repositories.StudentsRepositoryJdbcImpl;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TestStudentRepositoryJdbcImpl{

    List<Student> students;
    Statement statement;
    ResultSet resultSet;
    StudentsRepository repository;
    int age = 26;
    long id = 3;
    Student STUDENT = new Student(1, "bf", "fb", 43, 32);

    String SQL_SELECTED_ALL = "select s.id, s.first_name, s.last_name, age, group_number, " +
            "m.id as m_id, m.first_name as m_first_name, m.last_name as m_last_name " +
            "from student s left join mentor m on s.id = m.student_id";

    @Before
    public void takeConnectionAndDoListOfStudent() {
        try {
            SimpleDataSource dataSource = new SimpleDataSource();
            Connection connection = dataSource.openConnection();
            students = new LinkedList<>();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(SQL_SELECTED_ALL);
            while (resultSet.next()) {
                long studentID = resultSet.getLong("id");
                Student student;
                if ((student = students.stream().
                        filter(student1 -> student1.getId() == studentID).findAny().orElse(null)) == null) {
                    student = new Student(studentID,
                            resultSet.getString("first_name").trim(),
                            resultSet.getString("last_name").trim(),
                            resultSet.getInt("age"),
                            resultSet.getInt("group_number"));
                    students.add(student);
                }

                long mentorID;
                if ((mentorID = resultSet.getLong("m_id")) != 0) {
                    Mentor mentor = new Mentor(
                            mentorID, resultSet.getString("m_first_name"),
                            resultSet.getString("m_last_name"));
                    student.setMentor(mentor);
                }
            }
            repository = new StudentsRepositoryJdbcImpl(connection);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }


    @Test
    public void testMethodFindAll() {
        List<Student> actual = repository.findAll();
        List<Student> expected = students;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testMethodFindAllByAge() {
        List<Student> actual = repository.findAllByAge(age);
        List<Student> expected = students.stream().filter(student -> student.getAge() == age).collect(Collectors.toList());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testMethodFindAllByAgeShowMentor() {
        System.out.println(repository.findAllByAge(age).get(0).getMentors());
    }

    @Test
    public void testMethodFindById() {
        Student actual = repository.findById(id);
        Student expected = students.stream().filter(student1 -> student1.getId() == id).findAny().orElse(null);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testMethodSave() {
        repository.save(STUDENT);
        System.out.println(STUDENT.getId());
    }

    @Test
    public void testMethodUpdate() {
        Student expected = students.stream().filter(student -> student.getId() == id).findAny().orElse(null);
        int newAge = expected.getAge() -1;
        System.out.println(expected.getAge());
        expected.setAge(newAge);
        repository.update(expected);
    }
}
