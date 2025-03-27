package com.cloud.compute.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "vms")
public class VM {
    @Id
    private String id;
    private String name;
    private String imageId;
    private String instanceType;
    private VMState state;
    private ResourceSpec resources;
    private NetworkConfig networkConfig;
    private List<String> securityGroups;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String region;
    private String availabilityZone;
    private List<String> tags;
    private VMMetadata metadata;

    public enum VMState {
        PENDING,
        RUNNING,
        STOPPED,
        TERMINATED,
        ERROR
    }

    @Data
    public static class ResourceSpec {
        private int cpuCores;
        private int memoryGB;
        private int storageGB;
        private int networkBandwidthMbps;
    }

    @Data
    public static class NetworkConfig {
        private String vpcId;
        private String subnetId;
        private String privateIp;
        private String publicIp;
        private List<String> securityGroupIds;
    }

    @Data
    public static class VMMetadata {
        private String hostname;
        private String sshKey;
        private String userData;
        private Map<String, String> customMetadata;
    }
} 