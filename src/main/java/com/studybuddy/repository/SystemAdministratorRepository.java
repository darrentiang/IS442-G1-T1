package com.studybuddy.repository;

import java.util.List;

import com.studybuddy.model.SystemAdministrator;

/** Stores and finds administrators. The rest of the app only ever sees this interface. */
public interface SystemAdministratorRepository {

    /** Saves an administrator, replacing any existing one with the same id. */
    void save(SystemAdministrator admin);

    /** Returns the administrator with this id, or null if no such administrator. */
    SystemAdministrator findById(String id);

    /** Returns all administrators. */
    List<SystemAdministrator> findAll();
}
