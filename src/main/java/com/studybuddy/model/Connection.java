package com.studybuddy.model;

import java.time.LocalDate;
import java.util.Objects;

/** An accepted match. While active, each side may see the other's connected view. */
public class Connection {

    private final String id;
    private final Student a;
    private final Student b;
    private final LocalDate since;
    private boolean active;

    /** Creates a connection between two students; it starts active. */
    public Connection(String id, Student a, Student b, LocalDate since) {
        this.id = id;
        this.a = a;
        this.b = b;
        this.since = since;
        this.active = true;
    }

    /** Returns the unique connection id. */
    public String getId() {
        return id;
    }

    /** Returns one side of the connection. */
    public Student getA() {
        return a;
    }

    /** Returns the other side of the connection. */
    public Student getB() {
        return b;
    }

    /** Returns the date the connection was accepted. */
    public LocalDate getSince() {
        return since;
    }

    /** Returns true while the connection has not been ended. */
    public boolean isActive() {
        return active;
    }

    /** Ends (false) or restores (true) the connection. */
    public void setActive(boolean active) {
        this.active = active;
    }

    /** Returns a short description of this connection for logs and debugging. */
    @Override
    public String toString() {
        return "Connection[" + id + " " + a.getName() + " <-> " + b.getName() + "]";
    }

    /** Two connections are equal when they have the same id. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Connection)) {
            return false;
        }
        Connection that = (Connection) other;
        return Objects.equals(id, that.id);
    }

    /** Returns a hash code from the id, consistent with equals. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
