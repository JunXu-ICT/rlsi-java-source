/****************************************************************************************
 *Project: Regularized Latent Semantic Indexing 1.0
 ****************************************************************************************
 *(C) Copyright 2013 Laboratory of Intelligent Information Processing, Nankai University
 *    All Rights Reserved.
 *Authors: Zhicheng He, Yingjie Xu, Jun Xu, MaoQiang xie, Yalou Huang
 *****************************************************************************************/
package nkiip.rlsi.thread;

import nkiip.rlsi.basicStruct.KeyValuePair;
import nkiip.rlsi.matrix.DenseMatrix;
import nkiip.rlsi.matrix.SparseMatrixByRow;

public class STCD_Lasso_MultiThread {

	public static int iMaxThreads = 4;
	public int iThreadID = -1;
	public DenseMatrix R; // term * doc
	public DenseMatrix S; // doc * topic
	public SparseMatrixByRow U;
	public float m_dInnerIterExitThreshold;
	public int m_iMaxInnerIterNum;
	public float m_lambda1;

	public STCD_Lasso_MultiThread(int iThread, DenseMatrix inS,
			DenseMatrix inR, SparseMatrixByRow inU) {
		iThreadID = iThread;
		S = inS;
		R = inR;
		U = inU;
	}

	public void STCD_Lasso_ThreadI() throws Exception {
		for (int iTerm = 0; iTerm < R.m_NumRow; iTerm++) {
			if (iTerm % iMaxThreads != iThreadID)
				continue;
			STCD_Lasso_OneTerm(iTerm, S, R, U);
		}
	}

	private void STCD_Lasso_OneTerm(int iTermID, DenseMatrix S_Matrix,
			DenseMatrix RMatrix, SparseMatrixByRow UMatrix) throws Exception {
		float deviation = 1;
		float norm = 0;
		float old_value = 0;

		int NumTopics = S_Matrix.m_NumRow;
		float[] row = new float[NumTopics];

		int iter = 0;
		while (deviation > m_dInnerIterExitThreshold
				&& iter < m_iMaxInnerIterNum) {
			deviation = 0;
			norm = 0;
			for (int iTopicID = 0; iTopicID < NumTopics; iTopicID++) {
				old_value = row[iTopicID];
				norm += old_value * old_value;
				float judge = RMatrix.GetValue(iTermID, iTopicID);

				for (int iTopicID2 = 0; iTopicID2 < NumTopics; iTopicID2++) {
					if (iTopicID2 == iTopicID)
						continue;
					judge -= row[iTopicID2]
							* S_Matrix.GetValue(iTopicID, iTopicID2);
				}

				if (judge > 0.5 * m_lambda1) {
					row[iTopicID] = (judge - 0.5F * m_lambda1)
							/ S_Matrix.GetValue(iTopicID, iTopicID);
				} else if (judge < -0.5 * m_lambda1) {
					row[iTopicID] = (judge + 0.5F * m_lambda1)
							/ S_Matrix.GetValue(iTopicID, iTopicID);
				} else {
					row[iTopicID] = 0;
				}

				deviation += (row[iTopicID] - old_value)
						* (row[iTopicID] - old_value);
			}

			deviation = (float) Math.sqrt(deviation);
			norm = (float) Math.sqrt(norm);
			if (norm != 0) {
				deviation /= norm;
			}
			iter++;
		}

		UMatrix.GetRow(iTermID).clear();
		for (int iTopic = 0; iTopic < NumTopics; iTopic++) {

			if (row[iTopic] != 0) {
				UMatrix.GetRow(iTermID).add(
						new KeyValuePair(iTopic, row[iTopic]));
			}
		}
		row = null;
	}
}
