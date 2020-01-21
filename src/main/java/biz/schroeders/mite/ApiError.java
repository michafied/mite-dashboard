package biz.schroeders.mite;

public class ApiError extends RuntimeException {
    private final int httpCode;

    public ApiError(final String error, final int httpCode) {
        super("{\"error\":\"" + error + "\"}");
        this.httpCode = httpCode;
    }

    public int getHttpCode() {
        return httpCode;
    }
}
