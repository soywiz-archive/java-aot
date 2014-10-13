package jflash.backend;

public class States {
    private State[] states = new State[100];
    private int index = 0;

    public States() {
        for (int n = 0; n < states.length; n++) states[n] = new State();
    }

    public State get() {
        return states[index];
    }

    public void save() {
        states[index + 1].copyFrom(states[index + 0]);
        index++;
    }

    public void restore() {
        index--;
    }
}
