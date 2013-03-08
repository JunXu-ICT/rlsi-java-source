/****************************************************************************************
 *Project: Regularized Latent Semantic Indexing 1.0
Reference: Quan Wang, Jun Xu, Hang Li, and Nick Craswell. Regularized latent semantic indexing. SIGIR 2011: 685-694. 
 ****************************************************************************************
 *Laboratory of Intelligent Information Processing, Nankai University
 *Authors: Zhicheng He, Yingjie Xu, Jun Xu, MaoQiang xie, Yalou Huang
 *****************************************************************************************/
package nkiip.rlsi.matrix;

public class DenseMatrix {
	public Float[][] p_MatrixData = null;
	public int m_NumRow;
	public int m_NumColumn;

	public DenseMatrix(int row, int col) {
		p_MatrixData = new Float[row][];
		for (int i = 0; i < row; i++) {
			p_MatrixData[i] = new Float[col];
			for (int j = 0; j < col; j++) {
				p_MatrixData[i][j] = 0.0f;
			}
		}

		m_NumColumn = col;
		m_NumRow = row;
	}

	public Float GetValue(int row, int col) throws Exception {
		if (row >= m_NumRow || col >= m_NumColumn || p_MatrixData == null) {
			throw new Exception("out of index bound");
		}
		return p_MatrixData[row][col];
	}

	public boolean SetValue(int row, int col, float value) throws Exception {
		if (row >= m_NumRow || col >= m_NumColumn || p_MatrixData == null) {
			return false;
		}
		p_MatrixData[row][col] = value;
		return true;
	}

	/**
	 * Matrix Inversion Using Cholesky decomposition
	 */
	public void Inverse() throws Exception {
		// do Cholesky decomposition
		if (m_NumRow != m_NumColumn || m_NumColumn <= 0 || p_MatrixData == null) {
			throw new Exception("cannot conduct inverse");
		}
		int dim = m_NumRow;
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				if (j == i) {
					float sum = 0;
					for (int k = 0; k < i; k++) {
						sum += p_MatrixData[i][k] * p_MatrixData[i][k];
					}
					if ((p_MatrixData[i][i] - sum) <= 0) {
						throw new Exception("Negative x_ii - sum");
					} else {
						p_MatrixData[i][i] = (float) Math
								.sqrt(p_MatrixData[i][i] - sum);
					}
				} else {
					float sum = 0;
					for (int k = 0; k < i; k++) {
						sum += p_MatrixData[j][k] * p_MatrixData[i][k];
					}
					p_MatrixData[j][i] = (p_MatrixData[j][i] - sum)
							/ p_MatrixData[i][i];
				}
			}
		}

		// calculate the inverse matrix of the upper-triangular matrix
		float[][] Cho_matrix_Inv = new float[dim][dim];
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				if (j == i) {
					Cho_matrix_Inv[j][i] = 1 / p_MatrixData[j][i];
				} else {
					float sum = 0;
					for (int k = i; k < j; k++) {
						sum += p_MatrixData[j][k] * Cho_matrix_Inv[k][i];
					}
					Cho_matrix_Inv[j][i] = (-sum / p_MatrixData[j][j]);
				}
			}
		}

		// calcuale the inverse matrix
		float[] temp = new float[dim];
		for (int i = 0; i < dim; i++) {
			for (int k = 0; k < dim; k++) {
				temp[k] = 0.0F;
			}
			for (int j = 0; j < dim; j++) {
				if (i <= j) {
					float sum = 0;
					for (int k = j; k < dim; k++) {
						sum += Cho_matrix_Inv[k][i] * Cho_matrix_Inv[k][j];
					}
					temp[j] = sum;
				} else {
					temp[j] = p_MatrixData[j][i];
				}
			}
			for (int j = 0; j < dim; j++) {
				p_MatrixData[i][j] = temp[j];
			}
		}
	}

	/**
	 * Release all of the memory
	 */
	public void ReleaseMemory() {
		p_MatrixData = null;
		m_NumRow = 0;
		m_NumColumn = 0;

	}

	/**
	 * set all of the entries zero
	 */
	public void Zero() throws Exception {
		if (m_NumRow == 0 || m_NumColumn == 0 || p_MatrixData == null) {
			throw new Exception("cannot zero empty matrix");
		}
		for (int i = 0; i < m_NumRow; i++) {
			for (int j = 0; j < m_NumColumn; j++) {
				p_MatrixData[i][j] = 0F;
			}
		}

	}
}
