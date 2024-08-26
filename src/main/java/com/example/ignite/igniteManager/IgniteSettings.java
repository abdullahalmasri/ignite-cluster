package com.example.ignite.igniteManager;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheRebalanceMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.PartitionLossPolicy;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.failure.StopNodeOrHaltFailureHandler;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import java.util.Arrays;
import java.util.Collections;

public class IgniteSettings {
    private static volatile IgniteSettings instance;

    private IgniteConfiguration igniteConfig;

    private IgniteSettings() {
        initConfig();
    }

    public static IgniteSettings getInstance() {
        if (instance == null) {
            synchronized (IgniteSettings.class) {
                if (instance == null) {
                    instance = new IgniteSettings();
                }
            }
        }
        return instance;
    }

    private void initConfig() {
        try {
            igniteConfig = getIgniteConfiguration();
            Ignite ignite = Ignition.start(igniteConfig);
            ignite.cluster().active(true); // Activate the cluster
            System.out.println("Cluster is active: " + ignite.cluster().active());
            /*
            * With this configuration:
            * Server nodes can store data, participate in computations, and execute queries.
            *Client nodes (if you had any) would be used primarily for issuing queries and computations without storing data.
            */
            // Print all nodes in the cluster
            System.out.println("Cluster nodes:");
            ignite.cluster().nodes().forEach(node ->
                    System.out.println("Node ID: " + node.id() + ", Address: " + node.addresses())
            );

            // Get the number of server and client nodes
            int serverNodes = ignite.cluster().forServers().nodes().size();
            int clientNodes = ignite.cluster().forClients().nodes().size();

            System.out.println("Number of server nodes: " + serverNodes);
            System.out.println("Number of client nodes: " + clientNodes);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize GridGain configuration", e);
        }
    }

    public IgniteConfiguration getIgniteConfiguration() {
        IgniteConfiguration cfg = new IgniteConfiguration();

        // Set the discovery SPI and other configurations as needed
        TcpDiscoverySpi spi = new TcpDiscoverySpi();
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Arrays.asList("ignite-node1:47510", "ignite-node2:47520", "ignite-node3:47530"));
        spi.setIpFinder(ipFinder);
        cfg.setDiscoverySpi(spi);

        // Set the work directory, if necessary
        cfg.setWorkDirectory("/config/work");

        cfg.setClientMode(true); // or false, depending on your node type

        return cfg;
    }

}
