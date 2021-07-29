package com.ryanjhuston.Types.Serializers;

import java.util.List;

public class SerializedNetwork {

    public String name;
    public List<String> portals;

    public SerializedNetwork() {
        super();
    }

    public SerializedNetwork(String name, List<String> portals) {
        this.name = name;
        this.portals = portals;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPortals() {
        return portals;
    }

    public void setPortals(List<String> portals) {
        this.portals = portals;
    }
}
