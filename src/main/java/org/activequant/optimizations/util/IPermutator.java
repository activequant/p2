package org.activequant.optimizations.util;

import java.util.List;

/**
 * 
 * The Permutator knows how to permute objects of type T. 
 * 
 * @author Ghost Rider
 *
 * @param <T> an object of which to generate permutations.
 */
public interface IPermutator<S,T> {
	public List<T> permutations(S permutateBase);
}
