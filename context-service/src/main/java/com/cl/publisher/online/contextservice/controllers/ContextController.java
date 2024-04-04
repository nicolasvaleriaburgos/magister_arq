package com.cl.publisher.online.contextservice.controllers;

import java.util.Map;

import com.cl.publisher.online.contextservice.dto.request.Create;
import com.cl.publisher.online.contextservice.dto.request.Update;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class ContextController {

    private final HazelcastInstance hazelcastInstance;
    private static final String HAZELCAST_NAME_MAP = "tpa-cdc-map";

    ContextController(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @PostMapping(value = "/")
    public String createContext(@RequestBody Create values) {
        Map<String, Map<String, Object>> hazelcastMap = hazelcastInstance
                .getMap(ContextController.HAZELCAST_NAME_MAP);
        hazelcastMap.put(values.getToken(), values.getValue());
        return "Data is stored.";
    }

    @GetMapping(value = "/")
    public Map<String, Object> getContext(@RequestParam String key) {
        Map<String, Map<String, Object>> hazelcastMap = hazelcastInstance
                .getMap(ContextController.HAZELCAST_NAME_MAP);
        return hazelcastMap.get(key);
    }

    @PutMapping(value = "/")
    public Map<String, Object> updateContext(@RequestBody Update values) {
        Map<String, Map<String, Object>> hazelcastMap = hazelcastInstance
                .getMap(ContextController.HAZELCAST_NAME_MAP);
        Map<String, Object> map = hazelcastMap.get(values.getToken());
        map.putAll(values.getValue());
        hazelcastMap.put(values.getToken(), map);
        return map;
    }

    @DeleteMapping(value = "/")
    public String deleteContext(@RequestParam String token) {
        Map<String, Map<String, Object>> hazelcastMap = hazelcastInstance
                .getMap(ContextController.HAZELCAST_NAME_MAP);
        hazelcastMap.remove(token);
        return "Data is remove";
    }
}
