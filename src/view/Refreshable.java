package view;

/** Implemented by panels that should reload their data the moment the user navigates to them. */
public interface Refreshable {
    void refreshNow();
}
