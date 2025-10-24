package com.codenine.managementservice.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

  public void sendCommitmentNoteEmail(PurchaseOrder po, SupplierCompany supplier, String toEmail) {
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
      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(body, true);

      emailSender.send(message);
    } catch (MessagingException e) {
      e.printStackTrace();
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
      this.sendCommitmentNoteEmail(po, supplier, supplierEmail);
      po.setStatus(Status.LATE);
      po.setEmailStatus(EmailStatus.REMINDER_SENT);
      purchaseOrderRepository.save(po);
    }
  }
}
