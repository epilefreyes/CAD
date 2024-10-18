package edu.javeriana.cad.test;

import java.nio.file.Path;
import java.nio.file.Paths;

import edu.javeriana.cad.CADExecuteFull;

public class TestFullExecution {

	//@Test
	public void testFullExecution() throws Exception {
		//m6g.2xlarge, ARM
		Path outputPath = Paths.get("G:\\Mi unidad\\Personales\\felipe\\Maestrías Ingenieria de Sistemas - IA\\2024-03\\Computacion_Alto_Desempeño\\proyecto2\\salidas");
		String instanceType = "m6g.2xlarge";
		boolean isx86 = false;
		CADExecuteFull.executeFullTest(outputPath, instanceType, isx86);
	}
}
