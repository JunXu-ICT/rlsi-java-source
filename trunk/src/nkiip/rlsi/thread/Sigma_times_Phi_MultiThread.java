/****************************************************************************************
 *Project: Regularized Latent Semantic Indexing 1.0
Reference: Quan Wang, Jun Xu, Hang Li, and Nick Craswell. Regularized latent semantic indexing. SIGIR 2011: 685-694. 
 ****************************************************************************************
 *Laboratory of Intelligent Information Processing, Nankai University
 *Authors: Zhicheng He, Yingjie Xu, Jun Xu, MaoQiang xie, Yalou Huang
 *****************************************************************************************/
package nkiip.rlsi.thread;

import nkiip.rlsi.matrix.DenseMatrix;

public class Sigma_times_Phi_MultiThread {
	public static int iMaxThreads = 4;
	public int iThreadID = -1;
	public DenseMatrix Phi; // topic * doc
	public DenseMatrix Sigma; // topic * topic
	public DenseMatrix V; // output doc*topic

	public Sigma_times_Phi_MultiThread(int iThread, DenseMatrix inSigma,
			DenseMatrix inPhi, DenseMatrix outV) {
		iThreadID = iThread;
		Sigma = inSigma;
		Phi = inPhi;
		V = outV;
	}

	public void Sigma_times_Phi_ThreadI() throws Exception {
		for (int iDoc = 0; iDoc < Phi.m_NumColumn; iDoc++) {
			if (iDoc % iMaxThreads != iThreadID)
				continue;

			for (int iTopic = 0; iTopic < Sigma.m_NumRow; iTopic++) {
				for (int iTopic2 = 0; iTopic2 < Sigma.m_NumRow; iTopic2++) {
					V.SetValue(
							iDoc,
							iTopic,
							V.GetValue(iDoc, iTopic)
									+ Sigma.GetValue(iTopic, iTopic2)
									* Phi.GetValue(iTopic2, iDoc));
				}
			}
		}
	}
}
