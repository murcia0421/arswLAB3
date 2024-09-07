package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;

public class HostBlackListSearchThread extends Thread {

    private final HostBlacklistsDataSourceFacade skds;
    private final String ipaddress;
    private final int startRange;
    private final int endRange;
    private int occurrencesCount;
    private final List<Integer> blackListOccurrences;
    private final HostBlackListsValidator validator;

    public HostBlackListSearchThread(String ipaddress, int startRange, int endRange, HostBlackListsValidator validator) {
        this.skds = HostBlacklistsDataSourceFacade.getInstance();
        this.ipaddress = ipaddress;
        this.startRange = startRange;
        this.endRange = endRange;
        this.occurrencesCount = 0;
        this.blackListOccurrences = new LinkedList<>();
        this.validator = validator;
    }

    @Override
    public void run() {
        for (int i = startRange; i <= endRange; i++) {
            if (skds.isInBlackListServer(i, ipaddress)) {
                //blackListOccurrences.add(i);
                //occurrencesCount++;
                boolean stop = validator.reportOccurrence(i); // Cada hilo verifica si debe detenerse llamando a validator.reportOccurrence(i)
                if(stop){
                    break; // Detener búsqueda si se alcanzó el número de ocurrencias requerido
                }
            }
        }
    }

    public int getOccurrencesCount() {
        return occurrencesCount;
    }

    public List<Integer> getBlackListOccurrences() {
        return blackListOccurrences;
    }
}
