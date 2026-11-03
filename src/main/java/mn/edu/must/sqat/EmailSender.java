package mn.edu.must.sqat;

/** И-мэйл илгээгч — гадаад үйлчилгээ, тестэд mock-оор орлуулна. */
public interface EmailSender {
    void send(String to, String subject, String body);
}
