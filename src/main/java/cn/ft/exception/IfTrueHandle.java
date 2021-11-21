package cn.ft.exception;

@FunctionalInterface
public interface IfTrueHandle {
    void handle(Runnable handle);
}
