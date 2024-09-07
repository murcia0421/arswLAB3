/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
    List<Integer> blackListOccurrences = new ArrayList<>();
    int totalOccurrences = 0;
    
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int N) {
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        int totalServers = skds.getRegisteredServersCount();
        int segmentSize = totalServers / N;
        int remainder = totalServers % N;
    
        List<HostBlackListSearchThread> threads = new ArrayList<>();
    
        int startRange = 0;
    
        for (int i = 0; i < N; i++) {
            int endRange = startRange + segmentSize - 1;
            if (i == N - 1) {
                endRange += remainder;
            }
    
            //Declaración de hilos
            HostBlackListSearchThread thread = new HostBlackListSearchThread(ipaddress, startRange, endRange, this);

            //Adicion de hilo al arreglo
            threads.add(thread);

            //Inicio de Hilo
            thread.start();
    
            startRange = endRange + 1;
        }
           
        
        //Recorrido de Hilos
        for (HostBlackListSearchThread thread : threads) {
            try {
                thread.join(); //Todos los hilos se unen (join), y los resultados se recopilan y se procesan como antes.
                /*totalOccurrences += thread.getOccurrencesCount();
                blackListOccurrences.addAll(thread.getBlackListOccurrences());
    
                if (totalOccurrences >= BLACK_LIST_ALARM_COUNT) {
                    break;
                }*/
            } catch (InterruptedException e) {
                LOG.log(Level.SEVERE, "Thread was interrupted", e);
            }
        }
    
        LOG.log(Level.INFO, "Checked {0} of {1} blacklists.", 
                new Object[]{blackListOccurrences.size(), totalServers});
    
        if (totalOccurrences >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(ipaddress);
            LOG.log(Level.INFO, "HOST {0} Reported as NOT trustworthy", ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
            LOG.log(Level.INFO, "HOST {0} Reported as trustworthy", ipaddress);
        }
    
        return blackListOccurrences;
    }

    // Método sincronizado para actualizar las ocurrencias y detener los hilos si es necesario
    public synchronized boolean reportOccurrence(int blacklistIndex) {
        if (totalOccurrences < BLACK_LIST_ALARM_COUNT) {
            blackListOccurrences.add(blacklistIndex);
            totalOccurrences++;
            return totalOccurrences >= BLACK_LIST_ALARM_COUNT;
        }
        return true; // Si ya se alcanzó el límite, retorna true para indicar que se debe detener la búsqueda
    }
    
}
