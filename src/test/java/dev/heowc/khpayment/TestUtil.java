package dev.heowc.khpayment;

public abstract class TestUtil {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignored
        }
    }

    private TestUtil() {}
}
