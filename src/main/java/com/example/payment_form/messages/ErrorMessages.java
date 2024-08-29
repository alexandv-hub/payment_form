package com.example.payment_form.messages;

public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static class Database {
        public static final String ERR_DATABASE_INIT_FAILED = "Database init failed.";
    }

    public static class Validation {
        public static final String ERR_RESULT_IS_MISSING_IN_THE_RESPONSE = "Result is missing in the response";
        public static final String ERR_FAILED_TO_PARSE_RESPONSE_JSON = "Failed to parse response JSON";
        public static final String ERR_VALIDATION_FAILED_ON_THE_SERVER = "Validation failed on the server";
    }

    public static class PaymentServiceImpl {
        public static final String ERR_PAYMENT_FAILED_DUE_TO_NO_RESPONSE_FROM_THE_PAYMENT_SERVICE = "Payment failed due to no response from the payment service.";
        public static final String ERR_FAILED_TO_SERIALIZE_PAYMENT_OBJECT = "Failed to serialize Payment object";
        public static final String ERR_FAILED_TO_SAVE_PAYMENT_REQUEST = "Failed to save payment request.";
        public static final String ERR_FAILED_TO_SAVE_PAYMENT_RESPONSE = "Failed to save payment response.";
    }
}
