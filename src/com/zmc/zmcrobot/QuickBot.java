package com.zmc.zmcrobot;

public class QuickBot {

	final double qb_base_plate[][] = { { 0.0335, 0.0534, 1 },
			{ 0.0429, 0.0534, 1 }, { 0.0639, 0.0334, 1 },
			{ 0.0686, 0.0000, 1 }, { 0.0639, -0.0334, 1 },
			{ 0.0429, -0.0534, 1 }, { 0.0335, -0.0534, 1 },
			{ -0.0465, -0.0534, 1 }, { -0.0815, -0.0534, 1 },
			{ -0.1112, -0.0387, 1 }, { -0.1112, 0.0387, 1 },
			{ -0.0815, 0.0534, 1 }, { -0.0465, 0.0534, 1 } };

	final double qb_bbb[][] = { { -0.0914, -0.0406, 1 },
			{ -0.0944, -0.0376, 1 }, { -0.0944, 0.0376, 1 },
			{ -0.0914, 0.0406, 1 }, { -0.0429, 0.0406, 1 },
			{ -0.0399, 0.0376, 1 }, { -0.0399, -0.0376, 1 },
			{ -0.0429, -0.0406, 1 } };

	final double qb_bbb_rail_l[][] = { { -0.0429, -0.0356, 1 },
			{ -0.0429, 0.0233, 1 }, { -0.0479, 0.0233, 1 },
			{ -0.0479, -0.0356, 1 } };

	final double qb_bbb_rail_r[][] = { { -0.0914, -0.0356, 1 },
			{ -0.0914, 0.0233, 1 }, { -0.0864, 0.0233, 1 },
			{ -0.0864, -0.0356, 1 } };

	final double qb_bbb_eth[][] = { { -0.0579, 0.0436, 1 },
			{ -0.0579, 0.0226, 1 }, { -0.0739, 0.0226, 1 },
			{ -0.0739, 0.0436, 1 } };

	final double qb_left_wheel[][] = { { 0.0254, 0.0595, 1 },
			{ 0.0254, 0.0335, 1 }, { -0.0384, 0.0335, 1 },
			{ -0.0384, 0.0595, 1 } };

	final double qb_left_wheel_ol[][] = { { 0.0254, 0.0595, 1 },
			{ 0.0254, 0.0335, 1 }, { -0.0384, 0.0335, 1 },
			{ -0.0384, 0.0595, 1 } };

	final double qb_right_wheel_ol[][] = { { 0.0254, -0.0595, 1 },
			{ 0.0254, -0.0335, 1 }, { -0.0384, -0.0335, 1 },
			{ -0.0384, -0.0595, 1 } };

	final double qb_right_wheel[][] = { { 0.0254, -0.0595, 1 },
			{ 0.0254, -0.0335, 1 }, { -0.0384, -0.0335, 1 },
			{ -0.0384, -0.0595, 1 } };

	final double qb_ir_1[][] = { { -0.0732, 0.0534, 1 },
			{ -0.0732, 0.0634, 1 }, { -0.0432, 0.0634, 1 },
			{ -0.0432, 0.0534, 1 } };

	final double qb_ir_2[][] = { { 0.0643, 0.0214, 1 }, { 0.0714, 0.0285, 1 },
			{ 0.0502, 0.0497, 1 }, { 0.0431, 0.0426, 1 } };

	final double qb_ir_3[][] = { { 0.0636, -0.0042, 1 }, { 0.0636, 0.0258, 1 },
			{ 0.0736, 0.0258, 1 }, { 0.0736, -0.0042, 1 } };

	final double qb_ir_4[][] = { { 0.0643, -0.0214, 1 },
			{ 0.0714, -0.0285, 1 }, { 0.0502, -0.0497, 1 },
			{ 0.0431, -0.0426, 1 } };

	final double qb_ir_5[][] = { { -0.0732, -0.0534, 1 },
			{ -0.0732, -0.0634, 1 }, { -0.0432, -0.0634, 1 },
			{ -0.0432, -0.0534, 1 } };

	final double qb_bbb_usb[][] = { { -0.0824, -0.0418, 1 },
			{ -0.0694, -0.0418, 1 }, { -0.0694, -0.0278, 1 },
			{ -0.0824, -0.0278, 1 } };

	
	
	public void setPos(double x, double y, double theta )
	{
		double tm[][] = getTransformationMatrix(x, y, theta );
		double bas[][] = matrixMultip(qb_base_plate, tm);
		printMatrix( bas );
	}
	
	
	
	private void printMatrix(double mt[][])
	{
		System.out.println("The matrix:");
		for(int i=0; i<mt.length; i++ )
		{
			for(int j=0; j<mt[i].length; j++)
			{
				System.out.print(mt[i][j]);
				System.out.print("\t\t");
			}
			System.out.println();
		}
		
	}
	
	
	private double[][] getTransformationMatrix( double x, double y, double theta ) {
		double[][] tm = new double[3][3];

		tm[0][0] = Math.cos(theta);
		tm[0][1] = -Math.sin(theta);
		tm[0][2] = 0;
		tm[1][0] = Math.sin(theta);
		tm[1][1] = Math.cos(theta);
		tm[1][2] = 0;
		tm[2][0] = x;
		tm[2][1] = y;
		tm[2][2] = 1;
		return tm;

	}
	
	private double[][] matrixMultip(double[][] m1, double[][] m2) {
		int xDim = m2[0].length;
		int yDim = m1.length;
		int kDim = m1[0].length;

		double[][] res = new double[yDim][xDim];

		for (int i = 0; i < yDim; i++) {
			for (int j = 0; j < xDim; j++) {
				res[i][j] = 0;
				for (int k = 0; k < kDim; k++)
					res[i][j] = res[i][j] + m1[i][k] * m2[k][j];
			}
		}

		return res;
	}

}
