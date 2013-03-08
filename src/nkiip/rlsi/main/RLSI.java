/****************************************************************************************
 *Project: Regularized Latent Semantic Indexing 1.0
Reference: Quan Wang, Jun Xu, Hang Li, and Nick Craswell. Regularized latent semantic indexing. SIGIR 2011: 685-694. 
 ****************************************************************************************
 *Laboratory of Intelligent Information Processing, Nankai University
 *Authors: Zhicheng He, Yingjie Xu, Jun Xu, MaoQiang xie, Yalou Huang
 *****************************************************************************************/
package nkiip.rlsi.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import nkiip.rlsi.basicStruct.KeyValuePair;
import nkiip.rlsi.matrix.DenseMatrix;
import nkiip.rlsi.matrix.SparseMatrixByRow;
import nkiip.rlsi.matrix.Term_Doc_tfidf_Matrix;
import nkiip.rlsi.thread.D_times_VT_MultiThread;
import nkiip.rlsi.thread.STCD_Lasso_MultiThread;
import nkiip.rlsi.thread.Sigma_times_Phi_MultiThread;
import nkiip.rlsi.thread.UT_times_D_MultiThread;
import nkiip.rlsi.thread.UT_times_U_MultiThread;
import nkiip.rlsi.thread.VT_times_V_MultiThread;

public class RLSI {
	// Matrices
	public Term_Doc_tfidf_Matrix m_Term_Doc_DMatrix; // Matrix D, input tf-idf
														// data
	public SparseMatrixByRow m_Term_Topic_UMatrix; // Matrix U, sparse, each row
													// is a term
	public DenseMatrix m_Doc_Topic_VMatrix; // Matrix V^T, dense, each row is a
											// document

	public String path = "";

	// Parameters that can be set by users
	public int m_NumTopics = 100; // -t: topic number
	public float m_lambda1 = 0.5F; // -l1: L1 regularizer
	public float m_lambda2 = 0.5F; // -l2: L2 regularizer
	public int m_iMaxOutIterNum = 500; // -#: Number of iterations
	public String m_UMatrix_prefix = "UMatrix"; // -u: U matrix prefix
	public String m_VMatrix_prefix = "VMatrix"; // -v: V matrix prefix
	public int iOutputIterSkipU = 5; // -su: skip iterations
	public int iOutputIterSkipV = 5; // -sv: skip iterations

	// for restarting the learning
	public String restart = "";

	// parameters that cannot set by users
	public int m_iMaxInnerIterNum = 100;
	public float m_dInnerIterExitThreshold = 1e-6F;

	public void Init(String fnDMatrix, String strNumberOfTopics,
			String strLambda1, String strLambda2) throws Exception {
		if (strNumberOfTopics != null && !strNumberOfTopics.equals("")) {
			m_NumTopics = Integer.parseInt(strNumberOfTopics);
		}
		if (strLambda1 != null && !strLambda1.equals("")) {
			m_lambda1 = Float.parseFloat(strLambda1);
		}
		if (strLambda2 != null && !strLambda2.equals("")) {
			m_lambda2 = Float.parseFloat(strLambda2);
		}

		m_Term_Doc_DMatrix = new Term_Doc_tfidf_Matrix();
		if (false == m_Term_Doc_DMatrix.Load(fnDMatrix)) {
			throw new Exception("Load tf-idf D Matrix Error!" + fnDMatrix);
		}

	}

	public boolean RLSI_Inference() throws Exception {
		UpdateV();
		return true;
	}

	public boolean RLSI_Learn() throws Exception {
		int iter = 0;

		while (iter < m_iMaxOutIterNum) {
			System.out.println("start iteration #" + iter);

			// for restarting from "U"
			if (iter == 0 && restart == "u") {
				UpdateV();
				System.gc();
				System.out.println("Write V matrix to " + m_VMatrix_prefix + ""
						+ ".restart\n");
				OutputV(m_VMatrix_prefix + ".restart");
			}

			UpdateU();
			System.gc();
			if (iter % iOutputIterSkipU == 0 && iter != 0) {
				System.out.println("Write U matrix");
				OutputU(m_UMatrix_prefix + "." + iter);
			}

			UpdateV();
			System.gc();
			if (iter % iOutputIterSkipV == 0 && iter != 0) {
				System.out.println("Write V matrix");
				OutputV(m_VMatrix_prefix + "." + iter);
			}
			System.out.println("complete iteration #" + iter);
			iter++;
		}
		return true;
	}

	void UpdateU() throws Exception {

		// Matrix S, dense, topic * topic
		System.out.println("\tUpdate U: S = V^T * V");
		Date timeBegin = new Date();

		DenseMatrix m_Topic_Topic_SMatrix = new DenseMatrix(m_NumTopics,
				m_NumTopics);

		// S = V * V^T: Note S is a symmetric matrix
		ArrayList<Thread> lstThread = new ArrayList<Thread>();
		for (int iThread = 0; iThread < VT_times_V_MultiThread.iMaxThreads; iThread++) {
			final VT_times_V_MultiThread m_i = new VT_times_V_MultiThread(
					iThread, m_Doc_Topic_VMatrix, m_Topic_Topic_SMatrix);
			Thread t_i = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						m_i.VT_times_V_ThreadI();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			lstThread.add(t_i);
			t_i.start();
		}
		for (Thread thread : lstThread) {
			thread.join();
		}

		Date timeEnd = new Date();
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println("Time used: "
				+ (timeEnd.getTime() - timeBegin.getTime()) + "ms"
				+ "; finished at " + sdFormat.format(timeEnd));

		System.out.println("\tUpdate U: R = D * V^T");
		timeBegin = new Date();

		DenseMatrix m_Term_Topic_RMatrix = new DenseMatrix(
				m_Term_Doc_DMatrix.iNumberOfTerms, m_NumTopics);
		lstThread.clear();
		for (int iThread = 0; iThread < D_times_VT_MultiThread.iMaxThreads; iThread++) {
			final D_times_VT_MultiThread m_i = new D_times_VT_MultiThread(
					iThread, m_Term_Doc_DMatrix, m_Doc_Topic_VMatrix,
					m_Term_Topic_RMatrix);
			Thread t_i = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						m_i.Matrix_Mul_ThreadI();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			lstThread.add(t_i);
			t_i.start();
		}
		for (Thread thread : lstThread) {
			thread.join();
		}
		timeEnd = new Date();
		System.out.println(" Time used: "
				+ (timeEnd.getTime() - timeBegin.getTime()) + "ms"
				+ "; finished at " + sdFormat.format(timeEnd));

		// conduct lasso on U, update term by term

		System.out.println("\tUpdate U: U = STCD_Lasso(R, S)");
		timeBegin = new Date();

		lstThread.clear();
		for (int iThread = 0; iThread < STCD_Lasso_MultiThread.iMaxThreads; iThread++) {
			final STCD_Lasso_MultiThread s_i = new STCD_Lasso_MultiThread(
					iThread, m_Topic_Topic_SMatrix, m_Term_Topic_RMatrix,
					m_Term_Topic_UMatrix);
			s_i.m_dInnerIterExitThreshold = m_dInnerIterExitThreshold;
			s_i.m_iMaxInnerIterNum = m_iMaxInnerIterNum;
			s_i.m_lambda1 = m_lambda1;
			Thread t_i = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						s_i.STCD_Lasso_ThreadI();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			t_i.start();
			lstThread.add(t_i);
		}
		for (Thread thread : lstThread) {
			thread.join();
		}
		timeEnd = new Date();
		System.out.println(" Time used: "
				+ (timeEnd.getTime() - timeBegin.getTime()) + "ms"
				+ "; finished at " + sdFormat.format(timeEnd));
		m_Topic_Topic_SMatrix.ReleaseMemory();
		m_Term_Topic_RMatrix.ReleaseMemory();

	}

	void UpdateV() throws Exception {
		/*
		 * Steps 1. Sigma = (U^T * U + \lambda2 I)^-1 //M * M 2. Phi = U^T * D
		 * // K * M \times M * N 3. V = Sigma * Phi // K * K \times K * N
		 */

		// Sigma = (U^T * U + \lambda2 I)^-1
		System.out.println("\tUpdate V: Sigma = (U^T * U + lambda_2 I)^-1");
		Date timeBegin = new Date();

		DenseMatrix Sigma = new DenseMatrix(m_NumTopics, m_NumTopics);

		ArrayList<Thread> lstThread = new ArrayList<Thread>();
		lstThread.clear();
		for (int iThread = 0; iThread < UT_times_U_MultiThread.iMaxThreads; iThread++) {
			final UT_times_U_MultiThread s_i = new UT_times_U_MultiThread(
					iThread, m_Term_Topic_UMatrix, Sigma);
			Thread t_i = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						s_i.UT_times_U_ThreadI();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});
			lstThread.add(t_i);
			t_i.start();

		}
		for (Thread thread : lstThread) {
			thread.join();
		}
		// Sigma = Sigma + \lambda2 I
		for (int i = 0; i < m_NumTopics; i++) {
			Sigma.SetValue(i, i, Sigma.GetValue(i, i) + m_lambda2);
		}

		Sigma.Inverse();

		Date timeEnd = new Date();
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(" Time used: "
				+ (timeEnd.getTime() - timeBegin.getTime()) + "ms"
				+ "; finished at " + sdFormat.format(timeEnd));

		// Phi = U^T * D using single machine version for saving memory
		System.out.println("\tUpdate V: Phi = U^T * D");
		timeBegin = new Date();
		// U[termID] is a list of <topicID, topicValue>
		// D[termID] is a list of <docid, tfidf>
		// Phi[topicID, docID], MaySparse

		lstThread.clear();

		DenseMatrix Phi = new DenseMatrix(m_NumTopics,
				m_Term_Doc_DMatrix.iNumberOfDocuments);
		for (int iThread = 0; iThread < UT_times_D_MultiThread.iMaxThreads; iThread++) {
			final UT_times_D_MultiThread s_i = new UT_times_D_MultiThread(
					iThread, m_Term_Topic_UMatrix, m_Term_Doc_DMatrix, Phi);
			Thread t_i = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						s_i.UT_times_D_ThreadI();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});
			lstThread.add(t_i);
			t_i.start();

		}
		for (Thread thread : lstThread) {
			thread.join();
		}
		timeEnd = new Date();
		System.out.println(" Time used: "
				+ (timeEnd.getTime() - timeBegin.getTime()) + "ms"
				+ "; finished at " + sdFormat.format(timeEnd));

		// V = Sigma * Phi

		System.out.println("\tUpdate V: V = Sigma * Phi");
		timeBegin = new Date();
		// V[doc, topic]^T = Sigma[topic, topic] * Phi[topic, doc]
		m_Doc_Topic_VMatrix.Zero();

		lstThread.clear();
		for (int iThread = 0; iThread < Sigma_times_Phi_MultiThread.iMaxThreads; iThread++) {
			final Sigma_times_Phi_MultiThread s_i = new Sigma_times_Phi_MultiThread(
					iThread, Sigma, Phi, m_Doc_Topic_VMatrix);
			Thread t_i = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						s_i.Sigma_times_Phi_ThreadI();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			lstThread.add(t_i);
			t_i.start();
		}
		for (Thread thread : lstThread) {
			thread.join();
		}

		timeEnd = new Date();
		System.out.println(" Time used: "
				+ (timeEnd.getTime() - timeBegin.getTime()) + "ms"
				+ "; finished at " + sdFormat.format(timeEnd));

		Sigma.ReleaseMemory();
		Phi.ReleaseMemory();
	}

	public void OutputU(String fn) throws Exception {
		BufferedWriter sw = new BufferedWriter(new FileWriter(path + fn));

		int NumberOfWordsInU = 0;
		sw.write("TermID\tTopicID:value TopicID:value ...");
		sw.newLine();
		for (int iTerm = 0; iTerm < m_Term_Topic_UMatrix.m_NumRow; iTerm++) {
			LinkedList<KeyValuePair> lstTopicidValue = m_Term_Topic_UMatrix
					.GetRow(iTerm);

			String value = "";
			for (int iPos = 0; iPos < lstTopicidValue.size(); iPos++) {
				value += lstTopicidValue.get(iPos).key.toString() + ":"
						+ lstTopicidValue.get(iPos).value.toString() + " ";
			}

			if (value.trim() != "") {
				String strTerm = m_Term_Doc_DMatrix.lstInternalTermID2OrgTermID
						.get(iTerm);
				sw.write(strTerm + "\t" + value.trim());
				sw.newLine();
				NumberOfWordsInU++;
			}
		}
		System.out.println("\nOutput U: " + NumberOfWordsInU
				+ " terms written into " + fn);
		if (NumberOfWordsInU < 10) {
			System.out
					.println("Warning: Less than 10 terms in U Matrix. Use smaller l1 paramters");
		}
		sw.close();
	}

	public void OutputV(String fn) throws Exception {

		BufferedWriter sw = new BufferedWriter(new FileWriter(path + fn));

		sw.write("DocID\tTopicID:value TopicID:value ...");
		sw.newLine();
		for (Integer iDoc = 0; iDoc < m_Doc_Topic_VMatrix.m_NumRow; iDoc++) {
			String value = "";
			for (Integer iTopic = 0; iTopic < m_Doc_Topic_VMatrix.m_NumColumn; iTopic++) {
				value += iTopic.toString() + ":"
						+ m_Doc_Topic_VMatrix.GetValue(iDoc, iTopic).toString()
						+ " ";
			}
			sw.write(iDoc.toString() + "\t" + value.trim());
			sw.newLine();
		}

		sw.close();
	}

}
