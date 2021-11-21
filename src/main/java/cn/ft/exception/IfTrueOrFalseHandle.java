package cn.ft.exception;

@FunctionalInterface
public interface IfTrueOrFalseHandle {
    void handle(Runnable trueRunnable, Runnable falseRunnable);
}
