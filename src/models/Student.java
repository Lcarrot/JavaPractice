package models;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Student {
    private long id;
    private String firstName;
    private String lastName;
    private int age;
    private int groupNumber;
    List<Mentor> mentors;

    public Student(long id, String firstName, String lastName, int age, int groupNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.groupNumber = groupNumber;
        mentors = new LinkedList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(int groupNumber) {
        this.groupNumber = groupNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id == student.id &&
                age == student.age &&
                groupNumber == student.groupNumber &&
                Objects.equals(firstName, student.firstName) &&
                Objects.equals(lastName, student.lastName);
    }

    public List<Mentor> getMentors() {
        return mentors;
    }

    public void setMentor(Mentor mentor) {
        mentors.add(mentor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, age, groupNumber);
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                ", groupNumber=" + groupNumber +
                '}';
    }
}
