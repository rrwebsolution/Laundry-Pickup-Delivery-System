package event;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central Observer/pub-sub hub that lets the GUI stay in sync with the database without manual
 * refreshes. Services call {@link #notifyListeners(DataChangeEvent)} right after a successful
 * create/update/delete; every panel that cares about that kind of data re-queries itself.
 * <p>
 * Also runs a lightweight {@link Timer}-based poll (see {@link #startPolling(int)}) so changes
 * made outside this running instance - directly in MySQL, or from another instance of the app -
 * are picked up too, without ever blocking the Swing event dispatch thread.
 */
public final class DataChangeManager {

    private static final List<DataChangeListener> listeners = new CopyOnWriteArrayList<>();
    private static Timer pollingTimer;

    private DataChangeManager() {
    }

    public static void addListener(DataChangeListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    /** Notifies every registered listener. Safe to call from any thread - always dispatches on the EDT. */
    public static void notifyListeners(DataChangeEvent event) {
        if (SwingUtilities.isEventDispatchThread()) {
            fire(event);
        } else {
            SwingUtilities.invokeLater(() -> fire(event));
        }
    }

    private static void fire(DataChangeEvent event) {
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged(event);
        }
    }

    /**
     * Starts a background heartbeat (via a Swing {@link Timer}, so it already fires on the EDT)
     * that re-announces every event type every {@code intervalMs} milliseconds. Each listener's
     * own refresh runs its DB query off the EDT (see {@code view.components.Async}), so this
     * never freezes the UI even though it fires periodically.
     */
    public static void startPolling(int intervalMs) {
        stopPolling();
        pollingTimer = new Timer(intervalMs, e -> {
            for (DataChangeEvent event : DataChangeEvent.values()) {
                fire(event);
            }
        });
        pollingTimer.setRepeats(true);
        pollingTimer.start();
    }

    public static void stopPolling() {
        if (pollingTimer != null) {
            pollingTimer.stop();
            pollingTimer = null;
        }
    }

    /** Clears every listener and stops polling. Call before rebuilding the main window (e.g. re-login). */
    public static void reset() {
        stopPolling();
        listeners.clear();
    }
}
