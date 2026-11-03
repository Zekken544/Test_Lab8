package mn.edu.must.sqat;

import java.util.List;

/** Оюутны мэдээлэл — repository-гоос ирдэг өгөгдөл. */
public record Student(String id, String email, boolean active, List<String> coursesTaken) {}
