/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.kieckegard.samples.batch.processor;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 *
 * @author Pedro Arthur <pfernandesvasconcelos@gmail.com>
 */
public class Loader {
    
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        
        final BatchLoader<String, Integer> sizeRetriever = (strings) -> {
            System.out.println("strings: " + strings);
            System.out.println("current thread: " + Thread.currentThread().getId());
          return (Collection<Integer>) strings.stream().map(string -> string.length()).collect(Collectors.toList());
        };
        
        final BatchProcessor<String, Integer> processor = new BatchProcessor<>(sizeRetriever, 10);
        
        CompletableFuture<Integer> future4 = processor.load("AAAA"); // 4
        future4.thenAccept(length -> {
            System.out.printf("%d is my length. :)\n", length); 
        });
        processor.load("AAAAA"); // 5
        processor.load("A"); // 1
        processor.load("AAAAAA"); // 6
        processor.load("AAA"); // 3
        processor.load("AAAAAAAAAA"); // 10
        processor.load("AAAAAAAAA"); // 9
        processor.load("AA"); // 2
        processor.load("AAAA"); // 4
        processor.load("AAAAA"); // 5
        processor.load("A"); // 1
        processor.load("AAAAAA"); // 6
        processor.load("AAA"); // 3
        processor.load("AAAAAAAAAA"); // 10
        processor.load("AAAAAAAAA"); // 9
        processor.load("AA"); // 2
        processor.load("AAAA"); // 4
        processor.load("AAAAA"); // 5
        processor.load("A"); // 1
        processor.load("AAAAAA"); // 6
        processor.load("AAA"); // 3
        processor.load("AAAAAAAAAA"); // 10
        processor.load("AAAAAAAAA"); // 9
        processor.load("AA"); // 2
        System.out.println("current thread: " + Thread.currentThread().getId());
        
        
        final Collection<Integer> sizes = processor.resolve();
        System.out.println("sizes: " + sizes);
    }
}
