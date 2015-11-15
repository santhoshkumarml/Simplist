package com.example.ankit.simplist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by santhosh on 11/15/15.
 */
public class ConfigContent {
    private List<ConfigElement> configElements = new ArrayList<ConfigElement>();
    private Map<String, ConfigElement> configElementsMap = new HashMap<String, ConfigElement>();

    public ConfigElement getConfigElement(String id) {
        return this.configElementsMap.get(id);
    }

    public ConfigElement getConfigElement(int pos) {
        return this.configElements.get(pos);
    }

    public List<ConfigElement> getConfigElements() {
        return this.configElements;
    }

    public void addAllConfigElements(List<ConfigElement> configElements) {
        this.configElements.addAll(configElements);
        for(ConfigElement element : configElements) {
            this.configElementsMap.put(element.getId(), element);
        }
    }

    public void addConfigElement(ConfigElement configElement) {
        this.configElements.add(configElement);
        this.configElementsMap.put(configElement.getId(), configElement);
    }

    public void removeConfigElement(String id) {
        ConfigElement configElement = this.configElementsMap.get(id);
        this.configElements.remove(configElement);
        this.configElementsMap.remove(id);
    }
}
