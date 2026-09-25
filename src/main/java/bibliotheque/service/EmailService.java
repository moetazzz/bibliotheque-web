package bibliotheque.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${mail.from:noreply@bibliotheque.com}")
    private String mailFrom;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * Envoie un email HTML.
     * Si mailEnabled=false, on log seulement.
     */
    public boolean envoyer(String destinataire, String sujet, String templateName, Context context) {
        if (!mailEnabled || mailSender == null) {
            log.info("📧 [EMAIL DÉSACTIVÉ] À: {} | Sujet: {} | Template: {}",
                    destinataire, sujet, templateName);
            return false;
        }

        try {
            String htmlContent = templateEngine.process("emails/" + templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Email envoyé à {} : {}", destinataire, sujet);
            return true;
        } catch (Exception e) {
            log.error("❌ Erreur envoi email à {} : {}", destinataire, e.getMessage());
            return false;
        }
    }
}