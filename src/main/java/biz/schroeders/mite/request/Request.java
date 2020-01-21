package biz.schroeders.mite.request;

public interface Request<T> {
    T validate();
}
