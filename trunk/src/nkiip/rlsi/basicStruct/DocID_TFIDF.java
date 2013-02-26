/****************************************************************************************
 *Project: Regularized Latent Semantic Indexing 1.0
 ****************************************************************************************
 *(C) Copyright 2013 Laboratory of Intelligent Information Processing, Nankai University
 *    All Rights Reserved.
 *Authors: Zhicheng He, Yingjie Xu, Jun Xu, MaoQiang xie, Yalou Huang
 *****************************************************************************************/
package nkiip.rlsi.basicStruct;

public class DocID_TFIDF {
	private int DocID;
	private float TFIDF;

	public int getDocID() {
		return DocID;
	}

	public void setDocID(int docID) {
		DocID = docID;
	}

	public float getTFIDF() {
		return TFIDF;
	}

	public void setTFIDF(float tFIDF) {
		TFIDF = tFIDF;
	}

}
