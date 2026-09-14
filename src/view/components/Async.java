package view.components;

import javax.swing.SwingWorker;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Runs a (possibly slow) piece of work - typically a JDBC query - off the Swing event dispatch
 * thread, then delivers the result (or failure) back on the EDT so callers can safely touch
 * Swing components. Keeps every panel's refresh/search code free of manual thread juggling.
 */
public final class Async {

    private Async() {
    }

    public static <T> void run(Callable<T> backgroundWork, Consumer<T> onSuccess, Consumer<Exception> onError) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return backgroundWork.call();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    onError.accept(cause instanceof Exception ? (Exception) cause : e);
                }
            }
        }.execute();
    }
}
