package org.activequant.optimization.domainmodel;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Algo Config class = better HashMap. 
 * 
 * @author Ghost Rider
 *
 */
public class AlgoConfig extends HashMap<String, Object>  implements Serializable {

	private static final long serialVersionUID = 1L;
	private String algorithm;
	private Long id; 
	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}
	
	
}
