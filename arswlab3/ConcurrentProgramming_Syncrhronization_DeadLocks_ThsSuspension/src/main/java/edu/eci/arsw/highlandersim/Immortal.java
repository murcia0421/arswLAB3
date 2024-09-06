package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;

    private int health;// Vida del inmortal

    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private boolean paused = false;// Control de pausa
    private static final Object pauseLock = new Object(); // Bloqueo compartido
    private boolean stopped = false;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue,
            ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue = defaultDamageValue;
    }

    public void run() {

        while (!stopped) {
            // Establecer condiciones de carrera
            synchronized (pauseLock) {
                while (paused) {
                    try {
                        pauseLock.wait();// Poner en espera hasta que sea notificado
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);// Posición en el arreglo de Inmortales

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());// Algún inmortal aleatorio dentro del tamaño
                                                                         // del arreglo

            // avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);

            // Lucha contra el inmortal "im"
            this.fight(im);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (this.health <= 0) {
                immortalsPopulation.remove(this);
                updateCallback.processReport(this.name + " ha muerto y ha sido eliminado de la simulación.\n");
                break;
            }

        }

    }

    public void fight(Immortal i2) {
        Immortal first, second;

        // Determinar el orden de bloqueo para evitar deadlock
        if (this.getId() < i2.getId()) {
            first = this;
            second = i2;
        } else {
            first = i2;
            second = this;
        }

        synchronized (first) {
            synchronized (second) {
                if (i2.getHealth() > 0) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                } else {
                    updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                    // immortalsPopulation.remove(i2); //Eliminación de Inmortal muertos del arreglo
                    // de inmortales
                }
            }
        }
    }

    public void setPaused(boolean newPaused) {
        this.paused = newPaused;
    }

    public void pauseThread() {
        paused = true;
    }

    public void stopImmortal(boolean isStop) {
        stopped = isStop;
    }

    public void resumeThread() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Reanudar los hilos pausados
        }
    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
