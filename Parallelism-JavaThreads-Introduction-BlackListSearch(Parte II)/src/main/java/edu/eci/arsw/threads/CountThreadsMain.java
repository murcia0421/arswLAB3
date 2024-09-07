/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.threads;

/**import sun.jvm.hotspot.runtime.Threads;*/

/**
 *
 * @author hcadavid
 */

public class CountThreadsMain implements Runnable {
    @Override
    public void run() {
        System.out.println("thread is running..");
    }

    public static void main(String[] args) {
        CountThread thread1 = new CountThread(0, 99);
        CountThread thread2 = new CountThread(100, 199);
        CountThread thread3 = new CountThread(200, 299);

        thread1.run();
        thread2.run();
        thread3.run();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
