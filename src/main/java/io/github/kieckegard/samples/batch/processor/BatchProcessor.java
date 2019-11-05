/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.kieckegard.samples.batch.processor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 *
 * @author Pedro Arthur <pfernandesvasconcelos@gmail.com>
 * @param <Input>
 * @param <Output>
 */
public class BatchProcessor<Input, Output> {
    
    private final BatchLoader<Input, Output> batchLoader;
    private final Integer size;
    
    /* mutable part */
    
    private final Queue<CompletableFuture<Output>> promises;
    
    private final Queue<Input> inputs;
    private final Queue<Input> currentBatchInputs;

    private final Queue<CompletableFuture<Collection<Output>>> thunks;
    
    /**
     * Creates a new batch processor that will trigger the given batchLoader
     * every time it reaches the given size,
     * 
     * @param batchLoader batch function that processes a collection of inputs and returns
     * a collection of outputs in the same order.
     * 
     * @param size size of each batch
     */
    public BatchProcessor(BatchLoader<Input, Output> batchLoader, Integer size) {
        this.batchLoader = batchLoader;
        this.size = size;
    
        this.promises = new ArrayDeque<>();
        this.inputs = new ArrayDeque<>();
        this.currentBatchInputs = new ArrayDeque<>();
        this.thunks = new ArrayDeque<>();
    }
    
    private CompletableFuture<Output> emptyPromise() {
        return new CompletableFuture<>();
    }
    
    /**
     * creates a empty promise and adds it to the promise queue
     */
    private CompletableFuture<Output> newPromise() {
        final CompletableFuture<Output> emptyPromise = this.emptyPromise();
        this.promises.add(emptyPromise);
        return emptyPromise;
    }
    
    /**
     * adds a new input to the input list and
     * the current batch input list.
     * 
     * @param input 
     */
    private void newInput(final Input input) {
        
        this.inputs.add(input);
        this.currentBatchInputs.add(input);
    }
    
    private Boolean isBatchFull() {
        
        return this.size.equals(this.currentBatchInputs.size());
    }
    
    private void clearCurrentBatchInputs() {
        this.currentBatchInputs.clear();
    }
    
    public CompletableFuture<Output> load(final Input input) {
        
        final CompletableFuture<Output> emptyPromise = this.newPromise();
        
        this.newInput(input);
        
        if (this.isBatchFull()) {
            
            this.newThunk(this.currentBatchInputs);
            this.clearCurrentBatchInputs();
        }
        
        return emptyPromise;
    }
    
    public Collection<Output> resolve() throws InterruptedException, ExecutionException {
        
        final List<Output> results = this.resolveAll()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        
        return results;
    }
    
    public Collection<Collection<Output>> resolveAll() throws InterruptedException, ExecutionException {
        
        this.flush();
        
        final List<Collection<Output>> results = new ArrayList<>();
        
        while(!this.inputs.isEmpty()) {
       
            final CompletableFuture<Collection<Output>> thunk = this.thunks.poll();
            final Collection<Output> thunkResults = thunk.get();
            
            thunkResults
                    .stream()
                    .forEach((Output thunkResult) -> {
                        final Input input = this.inputs.poll();
                        final CompletableFuture<Output> promise = this.promises.poll();
                        promise.complete(thunkResult);
                    });
            
            results.add(thunkResults);
        }
        
        return results;
    }
    
    private void flush() {
        this.newThunk(this.currentBatchInputs);
        this.clearCurrentBatchInputs();
    }
    
    private void newThunk(final Collection<Input> inputs) {
        
        final ArrayList<Input> copiedInputs = new ArrayList<>(inputs);
        
        final CompletableFuture<Collection<Output>> inputProcessorThunk = this.createThunk(copiedInputs);
        this.thunks.add(inputProcessorThunk);
    }
    
    private CompletableFuture<Collection<Output>> createThunk(final Collection<Input> inputs) {
        
        return CompletableFuture.supplyAsync(() -> {
           return this.batchLoader.apply(inputs);
        });
    }
    
    
}
