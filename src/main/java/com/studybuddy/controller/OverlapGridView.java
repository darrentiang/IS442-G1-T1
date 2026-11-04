package com.studybuddy.controller;

import com.studybuddy.model.AvailabilityGrid;

/**
 * Presentation helper for a candidate's 7-day availability grid: says, cell by cell, whether
 * both students are free, only the candidate is free, or the candidate is busy.
 */
public class OverlapGridView extends GridView {

    private final AvailabilityGrid viewerGrid;
    private final AvailabilityGrid candidateGrid;

    /** Creates the view from the viewer's and the candidate's grids; a null grid counts as never free. */
    public OverlapGridView(AvailabilityGrid viewerGrid, AvailabilityGrid candidateGrid) {
        this.viewerGrid = viewerGrid;
        this.candidateGrid = candidateGrid;
    }

    /** Returns "cell-both", "cell-free" (only the candidate is free) or "cell-busy" for one cell. */
    @Override
    public String cellClass(int day, int hourIndex) {
        boolean candidateFree = candidateGrid != null && candidateGrid.isAvailable(day, hourIndex);
        boolean viewerFree = viewerGrid != null && viewerGrid.isAvailable(day, hourIndex);
        if (candidateFree && viewerFree) {
            return "cell-both";
        }
        if (candidateFree) {
            return "cell-free";
        }
        return "cell-busy";
    }
}
