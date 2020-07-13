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
    private final static String SQL_SELECTED_ALL =
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
        try (Statement statement = connection.createStatement();
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

                long mentorID = result.getLong("m_id");
                if (mentorID != 0) {
                    Mentor mentor = getMentorFromSqlUsingResultSet(result, mentorID);
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

    private Mentor getMentorFromSqlUsingResultSet(ResultSet result, long id) {
        try {
            return new Mentor(
                    id, result.getString("m_first_name"),
                    result.getString("m_last_name"));
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }


    // language=SQL
    private final static String SQL_SELECTED_BY_ID = "select s.id, s.first_name, s.last_name, s.age, s.group_number, \n" +
            "m.id as m_id, m.first_name as m_first_name, m.last_name as m_last_name \n" +
            "from student s left join mentor m on s.id = m.student_id where s.id = ";

    @Override
    public Student findById(Long id) {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(SQL_SELECTED_BY_ID + id)) {
            boolean studentAdded = false;
            Student currentStudent = null;
            while (result.next()) {
                if (!studentAdded) {
                    int studentID = result.getInt("id");
                    currentStudent = getStudentFromSqlUsingResultSet(result, studentID);
                    studentAdded = true;
                }
                long mentorID = result.getLong("m_id");
                if (mentorID != 0) {
                    Mentor mentor = getMentorFromSqlUsingResultSet(result, mentorID);
                    currentStudent.setMentor(mentor);
                }
            }
            return currentStudent;
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // language=SQL
    private final static String SQL_INSERTED_STUDENT =
            "insert into student (first_name, last_name, age, group_number) values(?,?,?,?);";
    // language=SQL
    private final static String SQL_INSERTED_MENTOR = "insert into mentor (first_name, last_name, student_id) values(?,?,?)";

    @Override
    public void save(Student entity) {
        try (PreparedStatement preparedStatement = preparedStatementForSaveOrUpdateStudentWithoutID(entity, SQL_INSERTED_STUDENT)) {
            preparedStatement.execute();
            long studentID = getGeneratedKeysForStudent(preparedStatement);
            for (Mentor mentor:entity.getMentors()) {
                insertMentor(mentor, studentID);
            }
        }
        catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // language=SQL
    private final static String SQL_UPDATED_STUDENT_BY_ID = "update student set first_name = ?, " +
            "last_name = ?, age = ?, group_number = ? where id = ";
    // language=SQL
    private final static String SQL_DELETED_MENTOR_BY_STUDENT_ID = "delete from mentor where student_id = ";

    @Override
    public void update(Student entity) {
        try {
            long student_id = entity.getId();
            String request = SQL_UPDATED_STUDENT_BY_ID + student_id;
            PreparedStatement preparedStatement = preparedStatementForSaveOrUpdateStudentWithoutID(entity, request);
            preparedStatement.executeUpdate();
            deleteMentor();
            for (Mentor mentor: entity.getMentors()) {
                insertMentor(mentor, student_id);
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private long getGeneratedKeysForStudent(PreparedStatement preparedStatement) {
        long key = 0;
        try (ResultSet result = preparedStatement.getGeneratedKeys()){
            if (result.next()) {
                key = result.getLong("id");
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
        return key;
    }

    private void insertMentor(Mentor mentor, long student_id) {
        try (PreparedStatement preparedStatement = preparedStatementForSaveOrUpdateMentorWithoutID(mentor, student_id)) {
            preparedStatement.execute();
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
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private PreparedStatement preparedStatementForSaveOrUpdateMentorWithoutID(Mentor mentor, long student_id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(StudentsRepositoryJdbcImpl.SQL_INSERTED_MENTOR)) {
            preparedStatement.setString(1, mentor.getFirstName());
            preparedStatement.setString(2, mentor.getLastName());
            preparedStatement.setLong(3, student_id);
            return preparedStatement;
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void deleteMentor() {
        try(Statement statement = connection.createStatement()) {
            statement.execute(StudentsRepositoryJdbcImpl.SQL_DELETED_MENTOR_BY_STUDENT_ID);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
