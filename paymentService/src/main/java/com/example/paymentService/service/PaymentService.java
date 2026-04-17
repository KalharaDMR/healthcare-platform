package com.example.paymentService.service;

import com.example.paymentService.client.AppointmentsClient;
import com.example.paymentService.client.DoctorClient;
import com.example.paymentService.client.UserClient;
import com.example.paymentService.dto.*;
import com.example.paymentService.entity.Transaction;
import com.example.paymentService.entity.TransactionStatus;
import com.example.paymentService.repository.TransactionRepository;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final TransactionRepository repository;
    private final UserClient client;
    private final DoctorClient doctorClient;
    private final EmailService email;
    private final AppointmentsClient appointmentsClient;
    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    // 🔹 CHARGE PAYMENT
    public ResponseEntity<?> chargePayment(BookRequest request,String patientUserName,Boolean enableVideo) throws Exception {

        Optional<Transaction> optionalTx = repository.findBySlotId(request.getSlotId());
        Transaction transaction = optionalTx.orElse(null);
        if(transaction!=null)
        {
            if(transaction.getStatus()==TransactionStatus.SUCCESS)
            {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("The Slot is already booked");
            }
        }
        BigDecimal amount;
        DoctorProfileResponse doctorProfileResponse1;
        try {
            ResponseEntity<AvailabilitySlot> response = doctorClient.getSlot(request.getSlotId());
            AvailabilitySlot slot = response.getBody();
            if(slot==null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The doctor slot is not found");
            }
            if(!slot.isAvailable())
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The slot is not available");
            }
            if(enableVideo)
            {
                amount = slot.getCostForTheVideoConferencingAppointment();
            }
            else
            {
                amount = slot.getCostForTheNormalAppointment();
            }
            try {
                ResponseEntity<?> doctorProfileResponse = client.getDoctorProfile(slot.getDoctorUsername());
                ObjectMapper mapper = new ObjectMapper();
                doctorProfileResponse1= mapper.convertValue(doctorProfileResponse.getBody(),DoctorProfileResponse.class);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Doctor is not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        try {
            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount(amount.multiply(new BigDecimal("100")).longValue())
                            .setCurrency("usd")
                            // 🔥 THIS SHOWS IN STRIPE DASHBOARD / UI
                            .setDescription("Appointment for Dr. " + doctorProfileResponse1.getDoctorName())
                            // 🔥 EXTRA DATA (VERY POWERFUL)
                            .putMetadata("slotId",request.getSlotId().toString())
                            .putMetadata("doctorName",doctorProfileResponse1.getDoctorName())
                            .putMetadata("patientUserName",patientUserName)
                            .putMetadata("isEnableVideoConferencing",enableVideo.toString())
                            .setAutomaticPaymentMethods(
                                    PaymentIntentCreateParams.AutomaticPaymentMethods
                                            .builder()
                                            .setEnabled(true)
                                            .build()
                            )
                            .build();
            PaymentIntent intent = PaymentIntent.create(params);

            if (transaction == null) {
                transaction = new Transaction();
                transaction.setSlotId(request.getSlotId());
            }

            transaction.setAmount(amount);
            transaction.setStatus(TransactionStatus.PENDING);
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setStripePaymentIntentId(intent.getId());
            repository.save(transaction);
            return ResponseEntity.ok(Map.of("client_secret",intent.getClientSecret()));

        } catch (Exception e) {

            e.printStackTrace(); // 🔥 VERY IMPORTANT (or use logger)

            if (transaction == null) {
                transaction = new Transaction();
                transaction.setSlotId(request.getSlotId());
            }
            transaction.setAmount(amount);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setStripePaymentIntentId(null);
            repository.save(transaction);

            throw new Exception(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<String> handleWebHook(String payload, String sigHeader)
    {
        Event event;

        // 1. Verify Stripe signature
        try {
            event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    endpointSecret
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        // 2. Handle SUCCESS payment
        if ("payment_intent.succeeded".equals(event.getType())) {
            System.out.println("[WEBHOOK] payment_intent.succeeded received. eventId=" + event.getId());
            try {
                System.out.println("[WEBHOOK][STEP 1] Resolve payment intent");
                PaymentIntent intent = resolvePaymentIntent(event, payload);

                if (intent == null) {
                    System.out.println("[WEBHOOK][ERROR] PaymentIntent is null for eventId=" + event.getId());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("PaymentIntent payload is null");
                }

                System.out.println("[WEBHOOK][STEP 2] Read metadata");
                Long slotId = Long.parseLong(intent.getMetadata().get("slotId"));
                String patientUserName = intent.getMetadata().get("patientUserName");
                String doctorName = intent.getMetadata().get("doctorName");
                Boolean isEnableVideoConferencing = Boolean.parseBoolean(intent.getMetadata().get("isEnableVideoConferencing"));
                System.out.println("[WEBHOOK] slotId=" + slotId + ", patientUserName=" + patientUserName + ", video=" + isEnableVideoConferencing);

                System.out.println("[WEBHOOK][STEP 3] Update transaction status to SUCCESS");
                repository.findBySlotId(slotId)
                        .ifPresent(tx -> {
                            tx.setStatus(TransactionStatus.SUCCESS);
                            tx.setStripePaymentIntentId(intent.getId());
                            tx.setPatientUserName(patientUserName);
                            repository.save(tx);
                        });
                System.out.println("[WEBHOOK][STEP 3 DONE] Transaction update attempted");

                System.out.println("[WEBHOOK][STEP 4] Book appointment");
                AppointmentCreateRequest appointmentCreate = new AppointmentCreateRequest();
                appointmentCreate.setSlotId(slotId);
                AppointmentResponse appointmentResponse = appointmentsClient.book(patientUserName, appointmentCreate, isEnableVideoConferencing);
                System.out.println("[WEBHOOK][STEP 4 DONE] Appointment booked. id=" + appointmentResponse.getId());

                System.out.println("[WEBHOOK][STEP 5] Build and send confirmation email");
                String htmlContent =
                        "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px;'>"
                                + "  <h2 style='color: #2E86C1;'>Appointment Confirmed ✅</h2>"
                                + "  <p>Dear <b>" + "Patient" + "</b>,</p>"
                                + "  <p>Your appointment has been successfully booked.</p>"
                                + "  <hr/>"
                                + "  <h3>📅 Appointment Details</h3>"
                                + "  <p><b>Doctor:</b> " + doctorName + "</p>"
                                + "  <p><b>Hospital:</b> " + appointmentResponse.getHospital() + "</p>"
                                + "  <p><b>Start Time:</b> " + appointmentResponse.getStartTime() + "</p>"
                                + "  <p><b>End Time:</b> " + appointmentResponse.getEndTime() + "</p>"
                                + "  <p><b>Mode:</b> " + (Boolean.TRUE.equals(appointmentResponse.getIsVideoConferencingAppointment()) ? "Online (Video Consultation)" : "Physical Visit") + "</p>"
                                + "  <hr/>"
                                + "  <p style='color: #555;'>Please arrive 10 minutes before your scheduled time.</p>"
                                + "  <p>If you have any questions, feel free to contact us.</p>"
                                + "  <br/>"
                                + "  <p>Thank you for choosing our service 🙏</p>"
                                + "  <p style='margin-top: 30px;'>"
                                + "    Best Regards,<br/>"
                                + "    <b>Your Healthcare Team</b>"
                                + "  </p>"
                                + "</div>";

                UserResponse userResponse = client.getUserByUserName(patientUserName);
                email.sendHtmlEmail(userResponse.getEmail(), "Appointment Booking Successful", htmlContent);
                System.out.println("[WEBHOOK][STEP 5 DONE] Email sent to " + userResponse.getEmail());

                return ResponseEntity.status(HttpStatus.ACCEPTED).body("Modify system successfully");
            } catch (Exception e) {
                System.out.println("[WEBHOOK][ERROR] payment_intent.succeeded flow failed");
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }
        }

        // 3. Handle FAILED payment
        if ("payment_intent.payment_failed".equals(event.getType())) {

            PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElse(null);

            if (intent != null) {

                String slotId = intent.getMetadata().get("slotId");

                repository.findBySlotId(Long.parseLong(slotId))
                        .ifPresent(tx -> {
                            tx.setStatus(TransactionStatus.FAILED);
                            tx.setStripePaymentIntentId(intent.getId());
                            repository.save(tx);
                        });
                return  ResponseEntity.status(HttpStatus.ACCEPTED).body("Payment failed event processed");
            }
            return  ResponseEntity.status(HttpStatus.ACCEPTED).body("Payment failed event processed (no intent)");
        }

        if ("refund.created".equals(event.getType())) {
            System.out.println("[WEBHOOK] refund.created received. eventId=" + event.getId());
            try {
                Refund refund = resolveRefund(event, payload);
                if (refund == null) {
                    System.out.println("[WEBHOOK][REFUND] Refund object is null for eventId=" + event.getId());
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body("Refund event accepted (refund not resolved)");
                }
                return processRefund(refund);
            } catch (Exception e) {
                System.out.println("[WEBHOOK][REFUND][ERROR] refund.created flow failed");
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }
        }

        if ("charge.refunded".equals(event.getType())) {
            System.out.println("[WEBHOOK] charge.refunded received. eventId=" + event.getId());
            try {
                Charge charge = resolveRefundCharge(event, payload);

                if (charge == null) {
                    System.out.println("[WEBHOOK][REFUND] Charge is null for eventId=" + event.getId());
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body("Refund event accepted (charge not resolved)");
                }

                    // 🔥 Get refund list (IMPORTANT)
                    if (charge.getRefunds() == null || charge.getRefunds().getData() == null) {
                        System.out.println("[WEBHOOK][REFUND] Charge has no embedded refunds data. chargeId=" + charge.getId());
                        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Refund event accepted (charge refunds missing)");
                    }
                    List<Refund> refunds = charge.getRefunds().getData();

                    if (refunds.isEmpty()) {
                        System.out.println("[WEBHOOK][REFUND] No refunds list in charge payload. chargeId=" + charge.getId());
                        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Refund event accepted (no refund entries)");
                    }

                        Refund refund = refunds.get(0); // first refund
                        if (refund.getMetadata() == null || refund.getMetadata().isEmpty()) {
                            System.out.println("[WEBHOOK][REFUND] Metadata empty on embedded refund; retrieving full refund by id=" + refund.getId());
                            refund = Refund.retrieve(refund.getId());
                        }
                        return processRefund(refund);

            } catch (Exception e) {
                System.out.println("[WEBHOOK][REFUND][ERROR] charge.refunded flow failed");
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }
        }
        System.out.println("[WEBHOOK] Ignored event type=" + event.getType() + ", eventId=" + event.getId());
        return ResponseEntity.ok("Event ignored");
    }

    /**
     * Handles API-version model mismatches by falling back to raw payload parsing.
     * If deserialization fails, parse event JSON to get PaymentIntent id and retrieve it from Stripe.
     */
    private PaymentIntent resolvePaymentIntent(Event event, String payload) {
        try {
            PaymentIntent directIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElse(null);
            if (directIntent != null) {
                return directIntent;
            }
            System.out.println("[WEBHOOK][STEP 1 FALLBACK] Deserializer returned null, trying payload parse");
        } catch (Exception ex) {
            System.out.println("[WEBHOOK][STEP 1 FALLBACK] Direct deserialization threw exception, trying payload parse");
            ex.printStackTrace();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<?, ?> root = mapper.readValue(payload, Map.class);
            Object dataObj = root.get("data");
            if (!(dataObj instanceof Map<?, ?> dataMap)) {
                return null;
            }

            Object objectObj = dataMap.get("object");
            if (!(objectObj instanceof Map<?, ?> objectMap)) {
                return null;
            }

            Object paymentIntentIdObj = objectMap.get("id");
            if (!(paymentIntentIdObj instanceof String paymentIntentId) || paymentIntentId.isBlank()) {
                return null;
            }

            System.out.println("[WEBHOOK][STEP 1 FALLBACK] Retrieving PaymentIntent by id=" + paymentIntentId);
            return PaymentIntent.retrieve(paymentIntentId);
        } catch (Exception ex) {
            System.out.println("[WEBHOOK][STEP 1 FALLBACK] Unable to recover PaymentIntent from payload");
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Handles API-version model mismatches by falling back to payload parsing for charge.refunded.
     */
    private Charge resolveRefundCharge(Event event, String payload) {
        try {
            Charge directCharge = (Charge) event.getDataObjectDeserializer()
                    .getObject()
                    .orElse(null);
            if (directCharge != null) {
                return directCharge;
            }
            System.out.println("[WEBHOOK][REFUND FALLBACK] Deserializer returned null, trying payload parse");
        } catch (Exception ex) {
            System.out.println("[WEBHOOK][REFUND FALLBACK] Direct deserialization threw exception");
            ex.printStackTrace();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<?, ?> root = mapper.readValue(payload, Map.class);
            Object dataObj = root.get("data");
            if (!(dataObj instanceof Map<?, ?> dataMap)) {
                return null;
            }

            Object objectObj = dataMap.get("object");
            if (!(objectObj instanceof Map<?, ?> objectMap)) {
                return null;
            }

            Object chargeIdObj = objectMap.get("id");
            if (!(chargeIdObj instanceof String chargeId) || chargeId.isBlank()) {
                return null;
            }

            System.out.println("[WEBHOOK][REFUND FALLBACK] Retrieving Charge by id=" + chargeId);
            return Charge.retrieve(chargeId);
        } catch (Exception ex) {
            System.out.println("[WEBHOOK][REFUND FALLBACK] Unable to recover Charge from payload");
            ex.printStackTrace();
            return null;
        }
    }

    private Refund resolveRefund(Event event, String payload) {
        try {
            Refund directRefund = (Refund) event.getDataObjectDeserializer()
                    .getObject()
                    .orElse(null);
            if (directRefund != null) {
                return directRefund;
            }
            System.out.println("[WEBHOOK][REFUND FALLBACK] Refund deserializer returned null, trying payload parse");
        } catch (Exception ex) {
            System.out.println("[WEBHOOK][REFUND FALLBACK] Refund direct deserialization threw exception");
            ex.printStackTrace();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<?, ?> root = mapper.readValue(payload, Map.class);
            Object dataObj = root.get("data");
            if (!(dataObj instanceof Map<?, ?> dataMap)) {
                return null;
            }
            Object objectObj = dataMap.get("object");
            if (!(objectObj instanceof Map<?, ?> objectMap)) {
                return null;
            }
            Object refundIdObj = objectMap.get("id");
            if (!(refundIdObj instanceof String refundId) || refundId.isBlank()) {
                return null;
            }
            System.out.println("[WEBHOOK][REFUND FALLBACK] Retrieving Refund by id=" + refundId);
            return Refund.retrieve(refundId);
        } catch (Exception ex) {
            System.out.println("[WEBHOOK][REFUND FALLBACK] Unable to recover Refund from payload");
            ex.printStackTrace();
            return null;
        }
    }

    private ResponseEntity<String> processRefund(Refund refund) throws Exception {
        String refundStatus = refund.getStatus();
        if (!"succeeded".equalsIgnoreCase(refundStatus)) {
            System.out.println("[WEBHOOK][REFUND] Refund not succeeded yet. refundId=" + refund.getId() + ", status=" + refundStatus);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Refund event accepted (status not succeeded)");
        }

        Map<String, String> metadata = refund.getMetadata();
        if (metadata == null
                || metadata.get("slotId") == null
                || metadata.get("appointmentId") == null
                || metadata.get("reason") == null) {
            System.out.println("[WEBHOOK][REFUND] Required metadata missing on refund id=" + refund.getId());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Refund event accepted (metadata missing)");
        }

        Long slotId = Long.parseLong(metadata.get("slotId"));
        String reason = metadata.get("reason");
        Long appointmentId = Long.parseLong(metadata.get("appointmentId"));
        System.out.println("[WEBHOOK][REFUND][META] metadata=" + metadata);
        System.out.println("[WEBHOOK][REFUND][META] reason=" + reason + ", slotId=" + slotId + ", appointmentId=" + appointmentId);

        if ("Patient cancelled".equals(reason)) {
            String username = metadata.get("patientUserName");
            Optional<Transaction> transaction = repository.findBySlotId(slotId);
            if (transaction.isPresent()) {
                Transaction transaction1 = transaction.get();
                transaction1.setStripeRefundId(refund.getId());
                transaction1.setStatus(TransactionStatus.REFUNDED);
                transaction1.setPatientUserName(username);
                repository.save(transaction1);
            }
            try {
                AppointmentResponse appointmentResponse = appointmentsClient.cancelMyAppointment(appointmentId);
                DoctorProfileResponse doctorProfileResponse1;
                try {
                    ResponseEntity<?> doctorProfileResponse = client.getDoctorProfile(appointmentResponse.getDoctorUsername());
                    ObjectMapper mapper = new ObjectMapper();
                    doctorProfileResponse1 = mapper.convertValue(doctorProfileResponse.getBody(), DoctorProfileResponse.class);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Doctor is not found");
                }
                UserResponse userResponse = client.getUserByUserName(username);
                String htmlContent =
                        "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px;'>"
                                + "  <h2 style='color: #2E86C1;'>Appointment Canceled and Refund Money successful</h2>"
                                + "  <p>Dear <b>" + "Patient" + "</b>,</p>"
                                + "  <p>Your appointment has been successfully cancelled and money is refund</p>"
                                + "  <hr/>"
                                + "  <h3>📅 Appointment Details</h3>"
                                + "  <p><b>Doctor:</b> " + doctorProfileResponse1.getDoctorName() + "</p>"
                                + "  <p><b>Hospital:</b> " + appointmentResponse.getHospital() + "</p>"
                                + "  <p><b>Start Time:</b> " + appointmentResponse.getStartTime() + "</p>"
                                + "  <p><b>End Time:</b> " + appointmentResponse.getEndTime() + "</p>"
                                + "  <p><b>Mode:</b> " + (Boolean.TRUE.equals(appointmentResponse.getIsVideoConferencingAppointment()) ? "Online (Video Consultation)" : "Physical Visit") + "</p>"
                                + "  <hr/>"
                                + "  <br/>"
                                + "  <p>Thank you for choosing our service 🙏</p>"
                                + "  <p style='margin-top: 30px;'>"
                                + "    Best Regards,<br/>"
                                + "    <b>Your Healthcare Team</b>"
                                + "  </p>"
                                + "</div>";
                email.sendHtmlEmail(userResponse.getEmail(), "Appointment cancel and Refund payment successfully", htmlContent);
                String DoctorHtmlContent =
                        "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px;'>"
                                + "  <h2 style='color: #2E86C1;'>Appointment Canceled by patient</h2>"
                                + "  <p>Dear <b>" + doctorProfileResponse1.getDoctorName() + "</b>,</p>"
                                + "  <p>Appointment is canceled by patient</p>"
                                + "  <hr/>"
                                + "  <h3>📅 Appointment Details</h3>"
                                + "  <p><b>Doctor:</b> " + doctorProfileResponse1.getDoctorName() + "</p>"
                                + "  <p><b>Hospital:</b> " + appointmentResponse.getHospital() + "</p>"
                                + "  <p><b>Start Time:</b> " + appointmentResponse.getStartTime() + "</p>"
                                + "  <p><b>End Time:</b> " + appointmentResponse.getEndTime() + "</p>"
                                + "  <p><b>Mode:</b> " + (Boolean.TRUE.equals(appointmentResponse.getIsVideoConferencingAppointment()) ? "Online (Video Consultation)" : "Physical Visit") + "</p>"
                                + "  <hr/>"
                                + "  <br/>"
                                + "  <p style='margin-top: 30px;'>"
                                + "    Best Regards,<br/>"
                                + "    <b>Your Healthcare Team</b>"
                                + "  </p>"
                                + "</div>";
                email.sendHtmlEmail(doctorProfileResponse1.getEmail(), "Appointment cancel by patient", DoctorHtmlContent);
                return ResponseEntity.ok("Success");
            } catch (Exception e) {
                throw new Exception("Appointment is not updated");
            }
        } else if ("Doctor cancelled".equals(reason)) {
            try {
                Optional<Transaction> transaction = repository.findBySlotId(slotId);
                if (transaction.isPresent()) {
                    Transaction transaction1 = transaction.get();
                    transaction1.setStripeRefundId(refund.getId());
                    transaction1.setStatus(TransactionStatus.REFUNDED);
                    transaction1.setPatientUserName(null);
                    repository.save(transaction1);
                }
                AppointmentResponse appointmentResponse = appointmentsClient.cancelMyAppointment(appointmentId);
                DoctorProfileResponse doctorProfileResponse1;
                try {
                    ResponseEntity<?> doctorProfileResponse = client.getDoctorProfile(appointmentResponse.getDoctorUsername());
                    ObjectMapper mapper = new ObjectMapper();
                    doctorProfileResponse1 = mapper.convertValue(doctorProfileResponse.getBody(), DoctorProfileResponse.class);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Doctor is not found");
                }
                UserResponse userResponse = client.getUserByUserName(appointmentResponse.getPatientUsername());
                String htmlContent =
                        "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px;'>"
                                + "  <h2 style='color: #2E86C1;'>Appointment Canceled by doctor and Refund Money successful</h2>"
                                + "  <p>Dear <b>" + "Patient" + "</b>,</p>"
                                + "  <p>Your appointment has been successfully cancelled by the doctor and money is refund</p>"
                                + "  <hr/>"
                                + "  <h3>📅 Appointment Details</h3>"
                                + "  <p><b>Doctor:</b> " + doctorProfileResponse1.getDoctorName() + "</p>"
                                + "  <p><b>Hospital:</b> " + appointmentResponse.getHospital() + "</p>"
                                + "  <p><b>Start Time:</b> " + appointmentResponse.getStartTime() + "</p>"
                                + "  <p><b>End Time:</b> " + appointmentResponse.getEndTime() + "</p>"
                                + "  <p><b>Mode:</b> " + (Boolean.TRUE.equals(appointmentResponse.getIsVideoConferencingAppointment()) ? "Online (Video Consultation)" : "Physical Visit") + "</p>"
                                + "  <hr/>"
                                + "  <br/>"
                                + "  <p>Thank you for choosing our service 🙏</p>"
                                + "  <p style='margin-top: 30px;'>"
                                + "    Best Regards,<br/>"
                                + "    <b>Your Healthcare Team</b>"
                                + "  </p>"
                                + "</div>";
                email.sendHtmlEmail(userResponse.getEmail(), "Appointment cancellation by doctor", htmlContent);
                String doctorHtmlContent =
                        "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px;'>"
                                + "  <h2 style='color: #2E86C1;'>Appointment Canceled Successful and money refunded to the patient</h2>"
                                + "  <p>Dear <b>" + doctorProfileResponse1.getDoctorName() + "</b>,</p>"
                                + "  <p>The appointment is successfully canceled and refund is successful</p>"
                                + "  <hr/>"
                                + "  <h3>📅 Appointment Details</h3>"
                                + "  <p><b>Doctor:</b> " + doctorProfileResponse1.getDoctorName() + "</p>"
                                + "  <p><b>Hospital:</b> " + appointmentResponse.getHospital() + "</p>"
                                + "  <p><b>Start Time:</b> " + appointmentResponse.getStartTime() + "</p>"
                                + "  <p><b>End Time:</b> " + appointmentResponse.getEndTime() + "</p>"
                                + "  <p><b>Mode:</b> " + (Boolean.TRUE.equals(appointmentResponse.getIsVideoConferencingAppointment()) ? "Online (Video Consultation)" : "Physical Visit") + "</p>"
                                + "  <hr/>"
                                + "  <br/>"
                                + "  <p>Thank you for choosing our service 🙏</p>"
                                + "  <p style='margin-top: 30px;'>"
                                + "    Best Regards,<br/>"
                                + "    <b>Your Healthcare Team</b>"
                                + "  </p>"
                                + "</div>";
                email.sendHtmlEmail(doctorProfileResponse1.getEmail(), "Appointment cancel successful", doctorHtmlContent);
                return ResponseEntity.ok("Success");
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }
        }

        System.out.println("[WEBHOOK][REFUND] Unknown reason value='" + reason + "', skipping business update.");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Refund event accepted (unknown reason)");
    }

    @Transactional
    public ResponseEntity<?> patientRefund(Long slotId,Long appointmentId,String userName)
    {
        try {
            Optional<Transaction> transaction = repository.findBySlotId(slotId);
            if(transaction.isEmpty())
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The transaction data is not found");
            }
            if(transaction.get().getStatus()!=TransactionStatus.SUCCESS)
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This Appointment is not booked one");
            }
            if(!transaction.get().getPatientUserName().equals(userName))
            {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to cancel this appointment");
            }

            AppointmentResponse appointmentResponse = appointmentsClient.myAppointment(appointmentId);
            LocalDateTime now = LocalDateTime.now();

            LocalDateTime appointmentDateTime = LocalDateTime.of(
                    appointmentResponse.getAppointmentDate(),
                    appointmentResponse.getStartTime()
            );

            Duration duration = Duration.between(now, appointmentDateTime);
            long hours = duration.toHours();
            if(hours<6)
            {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot Cancel this appointment");
            }

            RefundCreateParams params =
                    RefundCreateParams.builder()
                            .setPaymentIntent(transaction.get().getStripePaymentIntentId())
                            .putMetadata("slotId",slotId.toString())
                            .putMetadata("appointmentId",appointmentId.toString())
                            .putMetadata("reason", "Patient cancelled")
                            .putMetadata("patientUserName",userName)
                            .build();

            Refund refund = Refund.create(params);
            transaction.get().setStripeRefundId(refund.getId());
            repository.save(transaction.get());
            return ResponseEntity.ok("Refund create successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> DoctorRefund(Long slotId,Long appointmentId,String userName)
    {
        try {
            Optional<Transaction> transaction = repository.findBySlotId(slotId);
            if (transaction.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment is not found");
            }
            if (transaction.get().getStatus() != TransactionStatus.SUCCESS) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This appointment is not booked one");
            }
            AppointmentResponse appointmentResponse = appointmentsClient.myAppointment(appointmentId);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime appointmentDateTime = LocalDateTime.of(
                    appointmentResponse.getAppointmentDate(),
                    appointmentResponse.getStartTime()
            );

            Duration duration = Duration.between(now, appointmentDateTime);
            long hours = duration.toHours();
            if(hours<1)
            {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot Cancel this appointment");
            }
            RefundCreateParams params =
                    RefundCreateParams.builder()
                            .setPaymentIntent(transaction.get().getStripePaymentIntentId())
                            .putMetadata("slotId", slotId.toString())
                            .putMetadata("appointmentId",appointmentId.toString())
                            .putMetadata("reason", "Doctor cancelled")
                            .putMetadata("DoctorUserName",userName)
                            .build();
            Refund refund = Refund.create(params);
            transaction.get().setStripeRefundId(refund.getId());
            repository.save(transaction.get());
            return ResponseEntity.ok("Refund create successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
