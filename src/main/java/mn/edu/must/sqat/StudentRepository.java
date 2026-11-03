package mn.edu.must.sqat;

import java.util.List;

/**
 * Өгөгдлийн сантай харьцах интерфейс.
 * Лабораторид БОДИТ хэрэгжүүлэлт байхгүй — тестэд mock-оор орлуулна.
 */
public interface StudentRepository {
    /** Оюутан олдохгүй бол null буцаана. */
    Student findStudent(String studentId);

    boolean courseExists(String courseId);

    /** Хичээлийн өмнөх холбоо хичээлүүд (байхгүй бол хоосон жагсаалт). */
    List<String> prerequisites(String courseId);

    void saveRegistration(String studentId, String courseId);
}
