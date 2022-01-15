package cn.ft.exception;

public class VUtils {

    public static ThrowExceptionFunction isError(boolean b) {

        return (errorMessage) -> {


            if (b) {
                throw new RuntimeException(errorMessage);
            }
        };
    }

    public static IfTrueOrFalseHandle isTrueOrFalse(boolean b) {
        return (success, fail) -> {
            if (b) {
                success.run();
            } else {
                fail.run();
            }
        };
    }

    public static IfTrueHandle isTrue(boolean b) {
        return (handle) -> {
            if (b) {
                handle.run();
            }
        };
    }

}
