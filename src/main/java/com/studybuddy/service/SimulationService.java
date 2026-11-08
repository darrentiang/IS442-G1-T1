package com.studybuddy.service;

import org.springframework.stereotype.Service;

import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.engine.MatchingEngine;
import com.studybuddy.engine.simulation.ConfigSearch;
import com.studybuddy.engine.simulation.ConfigSimulator;
import com.studybuddy.engine.simulation.CurrentState;
import com.studybuddy.engine.simulation.Recommendation;
import com.studybuddy.engine.simulation.SimulationReport;
import com.studybuddy.repository.ConnectionRepository;
import com.studybuddy.repository.MatchRequestRepository;
import com.studybuddy.repository.StudentRepository;

/**
 * Projects how matching settings would perform, for the admin config page: compares unsaved
 * settings with the saved ones, or searches for better settings. Works on a copy of the app's
 * data, so nothing it simulates is ever saved.
 */
@Service
public class SimulationService {

    private final MatchingEngine engine = MatchingEngine.createDefault();
    private final StudentRepository studentRepository;
    private final MatchRequestRepository requestRepository;
    private final ConnectionRepository connectionRepository;
    private final MatchingConfigService matchingConfigService;

    /** Creates the service with the repositories it reads and the config service (constructor injection). */
    public SimulationService(StudentRepository studentRepository, MatchRequestRepository requestRepository,
            ConnectionRepository connectionRepository, MatchingConfigService matchingConfigService) {
        this.studentRepository = studentRepository;
        this.requestRepository = requestRepository;
        this.connectionRepository = connectionRepository;
        this.matchingConfigService = matchingConfigService;
    }

    /**
     * Compares settings from the form with the saved settings.
     *
     * @param form settings that have already passed MatchingConfigService.validate
     * @return the comparison report
     */
    public SimulationReport simulate(MatchingConfigForm form) {
        MatchingConfig proposed = matchingConfigService.fromForm(form);
        return new ConfigSimulator(engine).compare(currentState(), matchingConfigService.getConfig(), proposed);
    }

    /** Searches for settings that clearly beat the saved ones. */
    public Recommendation recommend() {
        return new ConfigSearch(engine).recommend(currentState(), matchingConfigService.getConfig());
    }

    /** Returns a copy of the app's students, buddy requests and connections. */
    private CurrentState currentState() {
        return new CurrentState(studentRepository.findAll(), requestRepository.findAll(),
                connectionRepository.findAll());
    }
}
