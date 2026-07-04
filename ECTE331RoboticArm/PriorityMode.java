package ECTE331RoboticArm;

/**
 * The three priority-management strategies compared in this project
 * (Tasks 3-6): no management at all (baseline), priority inheritance,
 * and priority ceiling.
 */
public enum PriorityMode {
    /** No priority management: classic setup where priority inversion can occur. */
    NONE,
    /** Simulated priority inheritance: the resource holder's priority is
     *  temporarily boosted only when a higher-priority thread blocks on it. */
    INHERITANCE,
    /** Priority ceiling: the resource holder's priority is immediately raised
     *  to the resource's ceiling priority for the whole time it holds it. */
    CEILING
}
