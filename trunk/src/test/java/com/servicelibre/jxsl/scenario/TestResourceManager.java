package com.servicelibre.jxsl.scenario;

import java.util.Map;

import org.springframework.core.io.Resource;

public class TestResourceManager
{
    private Map<String, Resource> resources;

    public Map<String, Resource> getResources()
    {
        return resources;
    }

    public void setResources(Map<String, Resource> resources)
    {
        this.resources = resources;
    }
    
    public Resource getResource(String resourceName) {
        return resources.get(resourceName);
    }
}
