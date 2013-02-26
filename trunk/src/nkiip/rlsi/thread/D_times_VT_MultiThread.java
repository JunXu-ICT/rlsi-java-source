/****************************************************************************************
 *Project: Regularized Latent Semantic Indexing 1.0
 ****************************************************************************************
 *(C) Copyright 2013 Laboratory of Intelligent Information Processing, Nankai University
 *    All Rights Reserved.
 *Authors: Zhicheng He, Yingjie Xu, Jun Xu, MaoQiang xie, Yalou Huang
 *****************************************************************************************/
package nkiip.rlsi.thread;

import nkiip.rlsi.basicStruct.DocID_TFIDF;
import nkiip.rlsi.matrix.DenseMatrix;
import nkiip.rlsi.matrix.Term_Doc_tfidf_Matrix;

public class D_times_VT_MultiThread {
	public static int iMaxThreads = 4;
	public int iThreadID = -1;
	public Term_Doc_tfidf_Matrix D; // term * doc
	public DenseMatrix V; // doc * topic
	public DenseMatrix R;

	public D_times_VT_MultiThread(int id, Term_Doc_tfidf_Matrix inD,
			DenseMatrix inV, DenseMatrix inR) {
		iThreadID = id;
		D = inD;
		V = inV;
		R = inR;
	}

	public void Matrix_Mul_ThreadI() throws Exception {
		// for each terms
		for (int iTerm = 0; iTerm < D.iNumberOfTerms; iTerm++) {
			if (iTerm % iMaxThreads != iThreadID)
				continue;

			DocID_TFIDF[] lstDocTfIDF = D.GetRow(iTerm);
			// for each topics
			for (int iTopic = 0; iTopic < V.m_NumColumn; iTopic++) {
				R.SetValue(iTerm, iTopic, 0);
				for (int i = 0; i < lstDocTfIDF.length; i++) {
					DocID_TFIDF pDocTfIDF = lstDocTfIDF[i];
					R.SetValue(iTerm, iTopic,
							R.GetValue(iTerm, iTopic) + pDocTfIDF.getTFIDF()
									* V.GetValue(pDocTfIDF.getDocID(), iTopic));
				}
			}
		}
	}
}
