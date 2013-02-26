/****************************************************************************************
 *Project: Regularized Latent Semantic Indexing 1.0
 ****************************************************************************************
 *(C) Copyright 2013 Laboratory of Intelligent Information Processing, Nankai University
 *    All Rights Reserved.
 *Authors: Zhicheng He, Yingjie Xu, Jun Xu, MaoQiang xie, Yalou Huang
 *****************************************************************************************/
package nkiip.rlsi.thread;

import java.util.LinkedList;

import nkiip.rlsi.basicStruct.DocID_TFIDF;
import nkiip.rlsi.basicStruct.KeyValuePair;
import nkiip.rlsi.matrix.DenseMatrix;
import nkiip.rlsi.matrix.SparseMatrixByRow;
import nkiip.rlsi.matrix.Term_Doc_tfidf_Matrix;

public class UT_times_D_MultiThread {

	public static int iMaxThreads = 4;
	public int iThreadID = -1;
	public SparseMatrixByRow U;
	public Term_Doc_tfidf_Matrix D;
	public DenseMatrix Phi;

	public UT_times_D_MultiThread(int iThread, SparseMatrixByRow inU,
			Term_Doc_tfidf_Matrix inD, DenseMatrix inPhi) {
		iThreadID = iThread;
		U = inU;
		D = inD;
		Phi = inPhi;
	}

	public void UT_times_D_ThreadI() throws Exception {
		// U: Term * topic
		// D: Term * doc
		// Phi: topic * doc
		for (int iTermID = 0; iTermID < U.m_NumRow; iTermID++) // the id for
																// join and
																// dimishing
		{
			if (iTermID % iMaxThreads != iThreadID)
				continue;

			LinkedList<KeyValuePair> lstTopicIDTopicValueForOneTerm = U
					.GetRow(iTermID);
			DocID_TFIDF[] lstDocIDTfidfValueForOneTerm = D.GetRow(iTermID);

			for (KeyValuePair topic : lstTopicIDTopicValueForOneTerm) {
				int topicid = topic.key;
				float topicValue = topic.value;
				synchronized (Phi.p_MatrixData[topicid]) // lock the specific
															// topicid
				{
					for (int j = 0; j < lstDocIDTfidfValueForOneTerm.length; j++) {
						Phi.SetValue(
								topicid,
								lstDocIDTfidfValueForOneTerm[j].getDocID(),
								Phi.GetValue(topicid,
										lstDocIDTfidfValueForOneTerm[j]
												.getDocID())
										+ topicValue
										* lstDocIDTfidfValueForOneTerm[j]
												.getTFIDF());
					}
				}
			}
		}

	}
}
