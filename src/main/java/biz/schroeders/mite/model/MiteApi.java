package biz.schroeders.mite.model;

public class MiteApi {
    private final String host;
    private final int port;

    public MiteApi(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
