package mn.edu.must.sqat;

import java.util.ArrayList;
import java.util.List;

/**
 * Хичээлд бүртгэх үйлчилгээ — Лекц 5-ын бүртгэлийн API-гийн
 * бизнес логик, гэхдээ энэ удаад НЭГЖИЙН түвшинд.
 * Хамаарлууд (repository, emailSender) constructor-оор ирнэ (dependency injection).
 */
public class CourseRegistrationService {

    private final StudentRepository repository;
    private final EmailSender emailSender;

    public CourseRegistrationService(StudentRepository repository, EmailSender emailSender) {
        this.repository = repository;
        this.emailSender = emailSender;
    }

    /**
     * Бүртгэлийн үр дүн:
     *   "OK"                    — амжилттай (бүртгэл хадгалагдаж, мэдэгдэл илгээгдэнэ)
     *   "ERROR_NO_STUDENT"      — оюутан олдсонгүй
     *   "ERROR_INACTIVE_STUDENT"— оюутан идэвхгүй
     *   "ERROR_NO_COURSE"       — хичээл олдсонгүй
     *   "ERROR_PREREQUISITES"   — өмнөх холбоо хичээл дутуу
     * И-мэйл илгээлт АМЖИЛТГҮЙ болсон ч бүртгэл хүчинтэй хэвээр ("OK").
     */
    public String register(String studentId, String courseId) {
        if (studentId == null || courseId == null) {
            throw new IllegalArgumentException("studentId, courseId хоёул шаардлагатай");
        }

        Student student = repository.findStudent(studentId);
        if (student == null) return "ERROR_NO_STUDENT";
        if (!student.active()) return "ERROR_INACTIVE_STUDENT";
        if (!repository.courseExists(courseId)) return "ERROR_NO_COURSE";

        List<String> missing = new ArrayList<>(repository.prerequisites(courseId));
        missing.removeAll(student.coursesTaken());
        if (!missing.isEmpty()) return "ERROR_PREREQUISITES";

        repository.saveRegistration(studentId, courseId);

        try {
            emailSender.send(student.email(),
                    "Бүртгэл баталгаажлаа",
                    courseId + " хичээлд амжилттай бүртгэгдлээ.");
        } catch (RuntimeException e) {
            // Мэдэгдэл чухал боловч бүртгэлийг унагаах ёсгүй.
        }
        return "OK";
    }
}
