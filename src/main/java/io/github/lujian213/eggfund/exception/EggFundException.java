package io.github.lujian213.eggfund.exception;

public class EggFundException extends RuntimeException {
    public EggFundException() {
    }

    public EggFundException(String s) {
        super(s);
    }

    public EggFundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public EggFundException(Throwable throwable) {
        super(throwable);
    }

    public EggFundException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}