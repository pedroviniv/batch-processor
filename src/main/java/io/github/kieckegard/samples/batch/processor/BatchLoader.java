/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.kieckegard.samples.batch.processor;

import java.util.Collection;
import java.util.function.Function;

/**
 *
 * @author Pedro Arthur <pfernandesvasconcelos@gmail.com>
 * @param <Input>
 * @param <Output>
 */
public interface BatchLoader<Input, Output> extends Function<Collection<Input>, Collection<Output>> {
    
}
