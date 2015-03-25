package com.F8Full.bixhistorique.backend.datamodel;

/**
* Created by F8Full on 2015-03-25.
*/
public class AvailabilityPair<A, B> {
    public final A nbBikes;
    public final B nbEmptyDocks;

    public AvailabilityPair(A nbBikes, B nbEmptyDocks) {
        this.nbBikes = nbBikes;
        this.nbEmptyDocks = nbEmptyDocks;
    }
}
