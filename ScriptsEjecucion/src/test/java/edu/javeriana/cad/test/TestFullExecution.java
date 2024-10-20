package edu.javeriana.cad.test;

import java.nio.file.Path;
import java.nio.file.Paths;

import edu.javeriana.cad.CADExecuteFull;

/**
 * Función para ejecutar la prueba de ejecución de la prueba completa sobre un equipo específico
 * @author FelipeReyesPalacio
 *
 */
public class TestFullExecution {

	//@Test
	public void testFullExecution() throws Exception {
		//m6g.2xlarge, ARM
		Path outputPath = Paths.get("G:\\cadSalidas");
		String instanceType = "a1.metal";
		boolean isx86 = false;
		CADExecuteFull.executeFullTest(outputPath, instanceType,"us-east-2a", isx86);
	}
}
