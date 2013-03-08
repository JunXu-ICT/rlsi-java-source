/****************************************************************************************
 *Project: Regularized Latent Semantic Indexing 1.0
Reference: Quan Wang, Jun Xu, Hang Li, and Nick Craswell. Regularized latent semantic indexing. SIGIR 2011: 685-694. 
 ****************************************************************************************
 *Laboratory of Intelligent Information Processing, Nankai University
 *Authors: Zhicheng He, Yingjie Xu, Jun Xu, MaoQiang xie, Yalou Huang
 *****************************************************************************************/
package nkiip.rlsi.thread;

import nkiip.rlsi.matrix.DenseMatrix;

public class VT_times_V_MultiThread {
	public static int iMaxThreads = 4;
	public int iThreadID = -1;
	public DenseMatrix V;
	public DenseMatrix S;

	public VT_times_V_MultiThread(int iThread, DenseMatrix inV, DenseMatrix inS) {
		iThreadID = iThread;
		V = inV;
		S = inS;
	}

	public void VT_times_V_ThreadI() throws Exception {
		DenseMatrix localS = new DenseMatrix(S.m_NumRow, S.m_NumColumn);

		for (int iDoc = 0; iDoc < V.m_NumRow; iDoc++) {
			if (iDoc % iMaxThreads != iThreadID)
				continue;

			for (int iTopic1 = 0; iTopic1 < V.m_NumColumn; iTopic1++) {
				for (int iTopic2 = iTopic1; iTopic2 < V.m_NumColumn; iTopic2++) {
					localS.SetValue(
							iTopic1,
							iTopic2,
							localS.GetValue(iTopic1, iTopic2)
									+ V.GetValue(iDoc, iTopic1)
									* V.GetValue(iDoc, iTopic2));
				}
			}
		}

		synchronized (S) {
			for (int iTopic1 = 0; iTopic1 < V.m_NumColumn; iTopic1++) {
				for (int iTopic2 = iTopic1; iTopic2 < V.m_NumColumn; iTopic2++) {
					S.SetValue(iTopic1, iTopic2, S.GetValue(iTopic1, iTopic2)
							+ localS.GetValue(iTopic1, iTopic2));
					if (iTopic2 != iTopic1) {
						S.SetValue(
								iTopic2,
								iTopic1,
								S.GetValue(iTopic2, iTopic1)
										+ localS.GetValue(iTopic1, iTopic2));
					}
				}
			}
		}
	}
}
