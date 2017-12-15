package com.symmetrylabs.slstudio.network;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class NetworkManager {

    private static NetworkManager instance;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
