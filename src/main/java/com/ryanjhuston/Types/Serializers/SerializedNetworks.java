package com.ryanjhuston.Types.Serializers;

import java.util.*;

public class SerializedNetworks {

    public List<String> networksList = new ArrayList<>();
    public List<SerializedNetwork> networks = new ArrayList<>();

    public SerializedNetworks() {
        super();
    }

    public SerializedNetworks(HashMap<String, List<String>> networkMap) {
        this.networksList = networksList;

        Iterator it = networkMap.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            this.networksList.add((String)pair.getKey());
            this.networks.add(new SerializedNetwork((String)pair.getKey(), (List<String>)pair.getValue()));
        }
    }

    public List<String> getNetworksList() {
        return networksList;
    }

    public void setNetworksList(List<String> networksList) {
        this.networksList = networksList;
    }

    public List<SerializedNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(List<SerializedNetwork> networks) {
        this.networks = networks;
    }
}
