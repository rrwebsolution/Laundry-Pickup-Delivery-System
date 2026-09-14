package event;

/** Implemented by any panel/component that wants to react when data of a given kind changes. */
public interface DataChangeListener {
    void onDataChanged(DataChangeEvent event);
}
