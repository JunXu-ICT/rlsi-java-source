/****************************************************************************************
 *Project: Regularized Latent Semantic Indexing 1.0
Reference: Quan Wang, Jun Xu, Hang Li, and Nick Craswell. Regularized latent semantic indexing. SIGIR 2011: 685-694. 
 ****************************************************************************************
 *Laboratory of Intelligent Information Processing, Nankai University
 *Authors: Zhicheng He, Yingjie Xu, Jun Xu, MaoQiang xie, Yalou Huang
 *****************************************************************************************/
package nkiip.rlsi.thread;

import java.util.LinkedList;

import nkiip.rlsi.basicStruct.KeyValuePair;
import nkiip.rlsi.matrix.DenseMatrix;
import nkiip.rlsi.matrix.SparseMatrixByRow;

public class UT_times_U_MultiThread {

	public static int iMaxThreads = 4;
	public int iThreadID = -1;
	public SparseMatrixByRow U;
	public DenseMatrix Sigma;

	public UT_times_U_MultiThread(int iThread, SparseMatrixByRow inU,
			DenseMatrix inSigma) {
		iThreadID = iThread;
		U = inU;
		Sigma = inSigma;
	}

	public void UT_times_U_ThreadI() throws Exception {
		DenseMatrix localSigma = new DenseMatrix(U.m_NumColumn, U.m_NumColumn);
		localSigma.Zero();

		for (int iTermID = 0; iTermID < U.m_NumRow; iTermID++) {
			if (iTermID % iMaxThreads != iThreadID)
				continue;
			LinkedList<KeyValuePair> pairs = U.GetRow(iTermID);

			KeyValuePair[] lstTopicIDTopicValueForOneTerm = new KeyValuePair[1];
			lstTopicIDTopicValueForOneTerm = pairs
					.toArray(lstTopicIDTopicValueForOneTerm);

			if (lstTopicIDTopicValueForOneTerm[0] == null) {
				continue;
			}

			for (int i = 0; i < lstTopicIDTopicValueForOneTerm.length; i++) {
				int idx1 = lstTopicIDTopicValueForOneTerm[i].key; // topicID 1
				float val1 = lstTopicIDTopicValueForOneTerm[i].value;

				for (int j = 0; j < lstTopicIDTopicValueForOneTerm.length; j++) {
					int idx2 = lstTopicIDTopicValueForOneTerm[j].key; // topicID
																		// 2
					float val2 = lstTopicIDTopicValueForOneTerm[j].value;

					localSigma.SetValue(idx1, idx2,
							localSigma.GetValue(idx1, idx2) + val1 * val2);
				}
			}
		}

		synchronized (Sigma) {
			for (int iTopic1 = 0; iTopic1 < Sigma.m_NumRow; iTopic1++) {
				for (int iTopic2 = 0; iTopic2 < Sigma.m_NumColumn; iTopic2++) {
					Sigma.SetValue(
							iTopic1,
							iTopic2,
							Sigma.GetValue(iTopic1, iTopic2)
									+ localSigma.GetValue(iTopic1, iTopic2));
				}
			}
		}
		localSigma = null;
	}
}
