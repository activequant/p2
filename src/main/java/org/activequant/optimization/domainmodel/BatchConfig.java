package org.activequant.optimization.domainmodel;

import java.io.Serializable;


/**
 * 
 * @author Ghost Rider
 *
 */
public class BatchConfig  implements Serializable {
	private String algoConfigSourceClass;
	private Long id; 
	private String batchReportTargetFileName;
	private String archiveFolder;
	private static final long serialVersionUID = 1L;
	public String getAlgoConfigSourceClass() {
		return algoConfigSourceClass;
	}
	public void setAlgoConfigSourceClass(String algoConfigSourceClass) {
		this.algoConfigSourceClass = algoConfigSourceClass;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getId() {
		return id;
	}
	public void setBatchReportTargetFileName(String batchReportTargetFileName) {
		this.batchReportTargetFileName = batchReportTargetFileName;
	}
	public String getBatchReportTargetFileName() {
		return batchReportTargetFileName;
	}
	public void setArchiveFolder(String archiveFolder) {
		this.archiveFolder = archiveFolder;
	}
	public String getArchiveFolder() {
		return archiveFolder;
	}
}
