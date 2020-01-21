package biz.schroeders.mite.request;

import static biz.schroeders.mite.constants.HttpCodes.BAD_REQUEST;

import biz.schroeders.mite.ApiError;

public class TokenRequest implements Request<TokenRequest> {
    private final String token;

    public TokenRequest(final String token) {
        this.token = token;
    }

    public String getToken() {
        return token != null ? token : "";
    }

    @Override
    public TokenRequest validate() {
        if (getToken().isEmpty()) {
            throw new ApiError("no token specified", BAD_REQUEST);
        }

        return this;
    }
}
