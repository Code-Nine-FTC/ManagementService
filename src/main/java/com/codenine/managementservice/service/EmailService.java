package com.codenine.managementservice.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.codenine.managementservice.dto.purchaseOrder.EmailStatus;
import com.codenine.managementservice.dto.purchaseOrder.Status;
import com.codenine.managementservice.entity.PurchaseOrder;
import com.codenine.managementservice.entity.SupplierCompany;
import com.codenine.managementservice.repository.PurchaseOrderRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender emailSender;
  private final PurchaseOrderRepository purchaseOrderRepository;

  public void sendCommitmentNoteEmail(
      PurchaseOrder po, SupplierCompany supplier, String toEmail, MultipartFile[] files) {
    String subject = "Solicitação de entrega de materiais por Nota de Empenho";
    // guard against null sender (scheduled jobs or older records may not have sender set)
    String senderName = "";
    String senderSection = "";
    if (po.getSender() != null) {
      senderName = po.getSender().getName();
      if (po.getSender().getSections() != null && !po.getSender().getSections().isEmpty())
        senderSection = po.getSender().getSections().get(0).getTitle();
    } else if (po.getCreatedBy() != null) {
      senderName = po.getCreatedBy().getName();
      if (po.getCreatedBy().getSections() != null && !po.getCreatedBy().getSections().isEmpty())
        senderSection = po.getCreatedBy().getSections().get(0).getTitle();
    } else {
      senderName = "Comando";
      senderSection = "Cmdo Brigada de Infantaria Aeromóvel";
    }

    String body =
        String.format(
            "<h2>Solicitação de entrega de materiais por Nota de Empenho</h2>"
                + "<p><b>Anexos:</b> cópia da Nota de empenho nº %s de %s</p>"
                + "<p>Informo que o Comando da Brigada de Infantaria Leve Aeromóvel (UG 160462) emitiu a Nota de Empenho nº %s, de %s em favor da empresa <b>%s</b>, CNPJ <b>%s</b> cuja cópia segue anexa para fins de conhecimento e medidas pertinentes visando o fornecimento dos itens contratados.</p>"
                + "<p>Por meio do presente comunicado, informamos oficialmente a essa empresa que a <b>ENTREGA</b> deverá ser efetuada em até <b>30 (trinta) dias</b> a contar desta data.</p>"
                + "<p>A entrega deverá ser realizada no <b></b>, situado no interior do Forte Ipiranga - Rua José Bonifácio, 33, Jardim Rafael, Caçapava/SP, CEP: 12280-470.<br>"
                + "Entrada pelo Portão A - na Avenida Marechal Castelo Branco.</p>"
                + "<p><b>HORÁRIO DE ENTREGA:</b><br>"
                + "SEGUNDA A QUINTA 09:30 AS 11:30HS e 13:00H AS 16:00H<br>"
                + "SEXTA 08:00 ÀS 11:30hs</p>"
                + "<p><i>Qualquer horário diferente do mencionado acima, impossibilita a presença de algum militar responsável pelo recebimento.</i></p>"
                + "<p><b>Ressaltamos</b> que o descumprimento total ou parcial do prazo de entrega sujeitará a empresa às penalidades previstas no Edital licitatório correspondente, Lei 14.133/21 de 1 de abril de 2021, Decreto nº 10.024 de 20 de setembro de 2019 e demais dispositivos legais aplicáveis, conforme o caso.</p>"
                + "<p>Solicitamos que os itens sejam entregues conforme proposta anexa e que por gentileza, confirme o recebimento da presente mensagem.</p>"
                + "<br><p>Atenciosamente,</p>"
                + "<p><b> %s <br> %s <br>Cmdo Brigada de Infantaria Aeromóvel</b></p>",
            po.getCommitmentNoteNumber(),
            po.getCreatedAt() != null ? po.getCreatedAt().toLocalDate() : "",
            po.getCommitmentNoteNumber(),
            po.getCreatedAt() != null ? po.getCreatedAt().toLocalDate() : "",
            supplier != null ? supplier.getName() : "",
            supplier != null ? supplier.getCnpj() : "",
            senderName,
            senderSection);
    try {
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom("nine.codek9@gmail.com");
      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(body, true);

      // Adicionar arquivos anexos, se fornecidos
      if (files != null && files.length > 0) {
        for (MultipartFile file : files) {
          if (file != null && !file.isEmpty()) {
            helper.addAttachment(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "attachment",
                () -> file.getInputStream());
          }
        }
      }

      emailSender.send(message);
    } catch (MessagingException e) {
      e.printStackTrace();
      throw new RuntimeException("Erro ao enviar email de nota de empenho: " + e.getMessage(), e);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Erro inesperado ao enviar email: " + e.getMessage(), e);
    }
  }

  public void sendCommitmentNoteEmail(PurchaseOrder po, SupplierCompany supplier, String toEmail) {
    sendCommitmentNoteEmail(po, supplier, toEmail, null);
  }

  public void sendChatInvitationEmail(
      String toEmail, String guestName, String inviterName, String chatLink) {
    String subject = "Convite para conversar - Sistema de Chat";

    String body =
        String.format(
            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<h2 style='color: #0084ff;'>Voce recebeu um convite para conversar!</h2>"
                + "<p>Ola%s,</p>"
                + "<p><b>%s</b> convidou voce para iniciar uma conversa no nosso sistema de chat.</p>"
                + "<p>Para acessar a conversa, clique no botao abaixo:</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='%s' style='background-color: #0084ff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;'>Acessar Conversa</a>"
                + "</div>"
                + "<p style='color: #666; font-size: 12px;'>Ou copie e cole este link no seu navegador:</p>"
                + "<p style='background-color: #f0f2f5; padding: 10px; border-radius: 5px; word-break: break-all; font-size: 12px;'>%s</p>"
                + "<p style='color: #666; font-size: 12px; margin-top: 30px;'><b>Importante:</b> Este link e valido por 7 dias e pode ser usado apenas uma vez.</p>"
                + "<p style='color: #666; font-size: 12px;'>Apos acessar, voce tera acesso apenas a esta conversa especifica.</p>"
                + "<hr style='border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;'>"
                + "<p style='color: #999; font-size: 11px; text-align: center;'>Este e um email automatico, por favor nao responda.</p>"
                + "</div>",
            guestName != null && !guestName.isEmpty() ? " " + guestName : "",
            inviterName,
            chatLink,
            chatLink);

    try {
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom("nine.codek9@gmail.com");
      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(body, true);

      emailSender.send(message);
    } catch (MessagingException e) {
      e.printStackTrace();
      throw new RuntimeException("Erro ao enviar email de convite: " + e.getMessage());
    }
  }

  public void sendGuestCredentialsEmail(
      String toEmail, String guestName, String email, String password, String loginUrl) {
    System.out.println("=== INICIANDO ENVIO DE EMAIL DE CREDENCIAIS ===");
    System.out.println("Para: " + toEmail);
    System.out.println("Nome: " + guestName);
    System.out.println("Email de login: " + email);
    System.out.println("URL de login: " + loginUrl);
    
    String subject = "Bem-vindo(a)! Suas credenciais de acesso ao sistema";

    String body =
        String.format(
            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<h2 style='color: #0084ff;'>Bem-vindo(a) ao nosso sistema!</h2>"
                + "<p>Ola <b>%s</b>,</p>"
                + "<p>Seu cadastro foi realizado com sucesso! Voce ja pode acessar o sistema e conversar com nossa equipe atraves do chat.</p>"
                + "<div style='background-color: #f0f2f5; padding: 20px; border-radius: 8px; margin: 20px 0;'>"
                + "<h3 style='margin-top: 0; color: #333;'>Suas credenciais de acesso:</h3>"
                + "<p style='margin: 10px 0;'><b>E-mail:</b> <span style='background-color: white; padding: 5px 10px; border-radius: 4px; font-family: monospace;'>%s</span></p>"
                + "<p style='margin: 10px 0;'><b>Senha:</b> <span style='background-color: white; padding: 5px 10px; border-radius: 4px; font-family: monospace;'>%s</span></p>"
                + "</div>"
                + "<p style='color: #d93025; font-size: 14px;'><b>Importante:</b> Guarde suas credenciais em um local seguro. Recomendamos que voce altere sua senha no primeiro acesso.</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='%s' style='background-color: #0084ff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;'>Acessar Sistema</a>"
                + "</div>"
                + "<p style='color: #666; font-size: 12px;'>Ou copie e cole este link no seu navegador:</p>"
                + "<p style='background-color: #f0f2f5; padding: 10px; border-radius: 5px; word-break: break-all; font-size: 12px;'>%s</p>"
                + "<div style='background-color: #e8f4fd; border-left: 4px solid #0084ff; padding: 15px; margin: 20px 0;'>"
                + "<h4 style='margin-top: 0; color: #0084ff;'>O que voce pode fazer:</h4>"
                + "<ul style='margin: 10px 0; padding-left: 20px;'>"
                + "<li>Conversar em tempo real com nossa equipe</li>"
                + "<li>Receber informacoes importantes sobre seus atendimentos</li>"
                + "<li>Tirar duvidas de forma rapida e pratica</li>"
                + "</ul>"
                + "</div>"
                + "<hr style='border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;'>"
                + "<p style='color: #999; font-size: 11px; text-align: center;'>Este e um email automatico, por favor nao responda.<br>Se tiver duvidas, entre em contato atraves do chat apos fazer login.</p>"
                + "</div>",
            guestName,
            email,
            password,
            loginUrl,
            loginUrl);

    try {
      System.out.println("Criando mensagem de email...");
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      
      System.out.println("Configurando destinatario...");
      helper.setTo(toEmail);
      helper.setFrom("nine.codek9@gmail.com");
      helper.setSubject(subject);
      helper.setText(body, true);

      System.out.println("Enviando email...");
      emailSender.send(message);
      System.out.println("EMAIL ENVIADO COM SUCESSO para: " + toEmail);
    } catch (MessagingException e) {
      System.err.println("ERRO ao enviar email de credenciais!");
      System.err.println("Tipo de erro: " + e.getClass().getName());
      System.err.println("Mensagem: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Erro ao enviar email de credenciais: " + e.getMessage(), e);
    } catch (Exception e) {
      System.err.println("ERRO INESPERADO ao enviar email!");
      System.err.println("Tipo: " + e.getClass().getName());
      System.err.println("Mensagem: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Erro inesperado ao enviar email: " + e.getMessage(), e);
    }
  }

  @Scheduled(cron = "0 0 1 * * ?")
  private void checkLateDeliveries() {
    LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
    List<PurchaseOrder> lateOrders =
        purchaseOrderRepository.findPendingOrdersOlderThan(thirtyDaysAgo);

    for (PurchaseOrder po : lateOrders) {
      SupplierCompany supplier = po.getSupplierCompany();
      String supplierEmail = supplier.getEmail();
      this.sendCommitmentNoteEmail(po, supplier, supplierEmail, null);
      po.setStatus(Status.LATE);
      po.setEmailStatus(EmailStatus.REMINDER_SENT);
      purchaseOrderRepository.save(po);
    }
  }
}