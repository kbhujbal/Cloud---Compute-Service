package com.cloud.compute.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud.compute.model.VM;
import com.cloud.compute.service.VMService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/vms")
@RequiredArgsConstructor
public class VMController {
    private final VMService vmService;

    @PostMapping
    @CircuitBreaker(name = "vmService")
    @RateLimiter(name = "vmService")
    public CompletableFuture<ResponseEntity<VM>> createVM(@RequestBody VM vm) {
        return vmService.createVM(vm)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> {
                    log.error("Error creating VM: {}", throwable.getMessage());
                    return ResponseEntity.internalServerError().build();
                });
    }

    @PostMapping("/{vmId}/start")
    @CircuitBreaker(name = "vmService")
    @RateLimiter(name = "vmService")
    public CompletableFuture<ResponseEntity<VM>> startVM(@PathVariable String vmId) {
        return vmService.startVM(vmId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> {
                    log.error("Error starting VM: {}", throwable.getMessage());
                    return ResponseEntity.internalServerError().build();
                });
    }

    @PostMapping("/{vmId}/stop")
    @CircuitBreaker(name = "vmService")
    @RateLimiter(name = "vmService")
    public CompletableFuture<ResponseEntity<VM>> stopVM(@PathVariable String vmId) {
        return vmService.stopVM(vmId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> {
                    log.error("Error stopping VM: {}", throwable.getMessage());
                    return ResponseEntity.internalServerError().build();
                });
    }

    @PostMapping("/{vmId}/terminate")
    @CircuitBreaker(name = "vmService")
    @RateLimiter(name = "vmService")
    public CompletableFuture<ResponseEntity<VM>> terminateVM(@PathVariable String vmId) {
        return vmService.terminateVM(vmId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> {
                    log.error("Error terminating VM: {}", throwable.getMessage());
                    return ResponseEntity.internalServerError().build();
                });
    }

    @PutMapping("/{vmId}/resources")
    @CircuitBreaker(name = "vmService")
    @RateLimiter(name = "vmService")
    public CompletableFuture<ResponseEntity<VM>> updateResources(
            @PathVariable String vmId,
            @RequestBody VM.ResourceSpec newResources) {
        return vmService.updateResources(vmId, newResources)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> {
                    log.error("Error updating VM resources: {}", throwable.getMessage());
                    return ResponseEntity.internalServerError().build();
                });
    }

    @PutMapping("/{vmId}/network")
    @CircuitBreaker(name = "vmService")
    @RateLimiter(name = "vmService")
    public CompletableFuture<ResponseEntity<VM>> updateNetworkConfig(
            @PathVariable String vmId,
            @RequestBody VM.NetworkConfig newConfig) {
        return vmService.updateNetworkConfig(vmId, newConfig)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> {
                    log.error("Error updating VM network config: {}", throwable.getMessage());
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/{vmId}")
    public ResponseEntity<VM> getVM(@PathVariable String vmId) {
        return vmService.getVM(vmId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VM>> getVMsByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(vmService.getVMsByUserId(userId));
    }

    @GetMapping("/state/{state}")
    public ResponseEntity<List<VM>> getVMsByState(@PathVariable VM.VMState state) {
        return ResponseEntity.ok(vmService.getVMsByState(state));
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<List<VM>> getVMsByRegion(@PathVariable String region) {
        return ResponseEntity.ok(vmService.getVMsByRegion(region));
    }

    @GetMapping("/availability-zone/{zone}")
    public ResponseEntity<List<VM>> getVMsByAvailabilityZone(@PathVariable String zone) {
        return ResponseEntity.ok(vmService.getVMsByAvailabilityZone(zone));
    }

    @GetMapping("/{vmId}/available")
    public ResponseEntity<Boolean> isVMAvailable(@PathVariable String vmId) {
        return ResponseEntity.ok(vmService.isVMAvailable(vmId));
    }
} 