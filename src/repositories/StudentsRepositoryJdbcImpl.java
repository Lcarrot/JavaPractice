package repositories;

import models.Mentor;
import models.Student;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class StudentsRepositoryJdbcImpl implements StudentsRepository {

    Connection connection;

    public StudentsRepositoryJdbcImpl(Connection connection) {
        this.connection = connection;
    }

    // language=SQL
    private  final static String SQL_SELECTED_ALL =
            "select s.id, s.first_name, s.last_name, s.age, s.group_number, " +
                    "m.id as m_id, m.first_name as m_first_name, m.last_name as m_last_name " +
                    "from student s left join mentor m on s.id = m.student_id";
    @Override
    public List<Student> findAll() {
        return findAllWithSelectedParameter(SQL_SELECTED_ALL);
    }

    // language=SQL
    private final static String SQL_SELECTED_ALL_BY_AGE = SQL_SELECTED_ALL + " where s.age = ";

    @Override
    public List<Student> findAllByAge(int age) {
        String request = SQL_SELECTED_ALL_BY_AGE + age;
        return findAllWithSelectedParameter(request);
    }

    private List<Student> findAllWithSelectedParameter(String request) {
        List<Student> students;
        try(Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(request)) {
            students = new LinkedList<>();
            while (result.next()) {
                long studentID = result.getLong("id");
                Student currentStudent = students.stream().
                        filter(student1 -> student1.getId() == studentID).findAny().orElse(null);
                if (currentStudent == null) {
                    currentStudent = getStudentFromSqlUsingResultSet(result, studentID);
                    students.add(currentStudent);
                }

                long mentorID;
                if ((mentorID = result.getLong("m_id")) != 0) {
                    Mentor mentor = new Mentor(
                            mentorID, result.getString("m_first_name"),
                            result.getString("m_last_name"));
                    currentStudent.setMentor(mentor);
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
        return students;
    }

    private Student getStudentFromSqlUsingResultSet(ResultSet result, long id) {
        try {
            return new Student(id, result.getString("first_name").trim(),
                    result.getString("last_name").trim(), result.getInt("age"),
                    result.getInt("group_number"));
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }


    // language=SQL
    private final static String SQL_SELECTED_BY_ID = "select * from student where id = ";

    @Override
    public Student findById(Long id) {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(SQL_SELECTED_BY_ID + id)) {
            if (result.next()) {
                int studentID = result.getInt("id");
                return getStudentFromSqlUsingResultSet(result, studentID);
            } else return null;
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // language=SQL
    private final static String SQL_INSERTED =
            "insert into student(first_name, last_name, age, group_number) values(?,?,?,?);";

    @Override
    public void save(Student entity) {
        try  {
            PreparedStatement preparedStatement = preparedStatementForSaveOrUpdateStudentWithoutID(entity, SQL_INSERTED);
            preparedStatement.execute();
            ResultSet result = preparedStatement.getGeneratedKeys();
            if (result.next()) {
                entity.setId(result.getLong("id"));
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // language=SQL
    private final static String SQL_UPDATED_BY_ID = "update student set first_name = ?, " +
            "last_name = ?, age = ?, group_number = ? where id = ";

    @Override
    public void update(Student entity) {
        try {
            String request = SQL_UPDATED_BY_ID + entity.getId();
            PreparedStatement preparedStatement = preparedStatementForSaveOrUpdateStudentWithoutID(entity, request);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private PreparedStatement preparedStatementForSaveOrUpdateStudentWithoutID(Student entity, String request) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(request)) {
            preparedStatement.setString(1, entity.getFirstName());
            preparedStatement.setString(2, entity.getLastName());
            preparedStatement.setInt(3, entity.getAge());
            preparedStatement.setInt(4, entity.getGroupNumber());
            return preparedStatement;
        }
        catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
