package com.saptarshi.doubtconnect.google;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private JavaMailSender mailSender;

    // Internal helper — deliberately NOT @Async on its own. Self-invocation
    // (this.sendEmail(...)) from other methods in this class bypasses Spring's
    // proxy, so @Async here alone would silently do nothing for those callers.
    // Each public method below carries @Async instead, so it actually applies.
    private boolean sendEmail(String to, String subject, String body) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Async
    public void sendStudentWelcomeEmail(String to, String username) {
        sendEmail(
                to,
                "Welcome to DoubtConnect!",
                "Hi " + username + ",\n\n" +
                        "Your DoubtConnect student account has been created successfully.\n\n" +
                        "Welcome aboard!\n\n" +
                        "Regards,\n" +
                        "Team DoubtConnect"
        );
    }

    @Async
    public void sendTeacherWelcomeEmail(String to, String username) {
        sendEmail(
                to,
                "Welcome to DoubtConnect!",
                "Hi " + username + ",\n\n" +
                        "Your DoubtConnect teacher account has been created successfully.\n\n" +
                        "Welcome aboard!\n\n" +
                        "Regards,\n" +
                        "Team DoubtConnect"
        );
    }

    @Async
    public void sendSessionReminderEmail(String to, String username) {

        sendEmail(
                to,
                "Session Reminder - DoubtConnect",
                "Hi " + username + ",\n\n" +
                        "This is a reminder that your DoubtConnect session is scheduled within the next 24 hours.\n\n" +
                        "Please make sure you are available on time and have a stable internet connection.\n\n" +
                        "We wish you a great session!\n\n" +
                        "Regards,\n" +
                        "Team DoubtConnect"
        );
    }

    @Async
    public void sendSessionAcceptedEmail(String to, String username) {

        sendEmail(
                to,
                "Session Request Accepted - DoubtConnect",
                "Hi " + username + ",\n\n" +
                        "Great news! Your session request has been accepted by the teacher.\n\n" +
                        "You can now view the session details in DoubtConnect.\n\n" +
                        "Regards,\n" +
                        "Team DoubtConnect"
        );
    }

    @Async
    public void sendSessionRejectedEmail(String to, String username) {

        sendEmail(
                to,
                "Session Request Rejected - DoubtConnect",
                "Hi " + username + ",\n\n" +
                        "We regret to inform you that your session request has been declined by the teacher.\n\n" +
                        "You can explore other teachers and submit a new request anytime.\n\n" +
                        "Regards,\n" +
                        "Team DoubtConnect"
        );
    }

    @Async
    public void sendPasswordResetEmail(String to, String resetLink) {
        sendEmail(
                to,
                "Reset Your DoubtConnect Password",
                "Hello,\n\n" +
                        "We received a request to reset your DoubtConnect password.\n\n" +
                        "Click the link below to reset your password:\n\n" +
                        resetLink +
                        "\n\nThis link is valid for 5 minutes.\n\n" +
                        "If you did not request a password reset, you can safely ignore this email.\n\n" +
                        "Regards,\n" +
                        "Team DoubtConnect"
        );
    }

    @Async
    public void sendPasswordChangedEmail(String to) {
        sendEmail(
                to,
                "Your DoubtConnect Password Was Changed",
                "Hello,\n\n" +
                        "Your DoubtConnect password has been changed successfully.\n\n" +
                        "If you did not perform this action, please contact support immediately.\n\n" +
                        "Regards,\n" +
                        "Team DoubtConnect"
        );
    }

    @Async
    public void sendStudentPaymentAvailableEmail(
            String to,
            String username) {

        sendEmail(
                to,
                "Session Payment Available",
                "Hi " + username + ",\n\n" +
                        "Please complete the payment .\n\n" +
                        "Thank you for learning with DoubtConnect.\n\n" +
                        "Regards,\n" +
                        "Team DoubtConnect"
        );
    }

    @Async
    public void sendTeacherPaymentAvailableEmail(
            String to,
            String username) {

        sendEmail(
                to,
                "Session Payment Available",
                "Hi " + username + ",\n\n" +
                        "The payment for your completed session is now available for withdrawal.\n\n" +
                        "You can view your payout details in your teacher dashboard.\n\n" +
                        "Regards,\n" +
                        "Team DoubtConnect"
        );
    }

}