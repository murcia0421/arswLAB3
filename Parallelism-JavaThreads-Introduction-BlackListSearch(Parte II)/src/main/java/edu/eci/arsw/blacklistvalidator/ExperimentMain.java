package edu.eci.arsw.blacklistvalidator;

import java.util.List;

public class ExperimentMain {

    public static void main(String[] args) {
        HostBlackListsValidator hblv = new HostBlackListsValidator();
        String ipaddress = "202.24.34.55";

        // 1. Un solo hilo
        runExperiment(hblv, ipaddress, 1);

        // 2. Tantos hilos como núcleos de procesamiento
        int cores = Runtime.getRuntime().availableProcessors();
        runExperiment(hblv, ipaddress, cores);

        // 3. Tantos hilos como el doble de núcleos de procesamiento
        runExperiment(hblv, ipaddress, cores * 2);

        // 4. 50 hilos
        runExperiment(hblv, ipaddress, 50);

        // 5. 100 hilos
        runExperiment(hblv, ipaddress, 100);
    }

    /**
     * Run the experiment with the specified number of threads and print the result.
     *
     * @param validator The instance of HostBlackListsValidator.
     * @param ipaddress The IP address to validate.
     * @param numThreads The number of threads to use in the experiment.
     */
    private static void runExperiment(HostBlackListsValidator validator, String ipaddress, int numThreads) {
        long startTime = System.currentTimeMillis();
        List<Integer> blackListOccurrences = validator.checkHost(ipaddress, numThreads);
        long endTime = System.currentTimeMillis();

        System.out.println("Experiment with " + numThreads + " threads:");
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
        System.out.println("Blacklists found: " + blackListOccurrences.size());
        System.out.println("Blacklist servers: " + blackListOccurrences);
        System.out.println("---------------------------------------");
    }
}
