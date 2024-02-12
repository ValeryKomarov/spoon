package spoon.test.prettyprinter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtComment.CommentType;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.sniper.SniperJavaPrettyPrinter;

import static org.assertj.core.api.Assertions.assertThat;

public class SniperInnerTypeTest {
	private static final Path INPUT_PATH = Paths.get("src/test/java/");
	private static final Path OUTPUT_PATH = Paths.get("target/test-output");

	@BeforeAll
	public static void setup() throws IOException {
		FileUtils.deleteDirectory(OUTPUT_PATH.toFile());
	}

	@Test
	public void innerTypeCrash() throws IOException {
		runSniperJavaPrettyPrinter("spoon/test/prettyprinter/testclasses/innertype/InnerTypeCrash.java");
	}

	@Test
	public void innerTypeOk() throws IOException {
		runSniperJavaPrettyPrinter("spoon/test/prettyprinter/testclasses/innertype/InnerTypeOk.java");
	}

	@Test
	public void innerTypeOk2() throws IOException {
		runSniperJavaPrettyPrinter("spoon/test/prettyprinter/testclasses/innertype/InnerTypeOk2.java");
	}

	private void runSniperJavaPrettyPrinter(String path) throws IOException {
		final Launcher launcher = new Launcher();
		final Environment e = launcher.getEnvironment();
		e.setLevel("INFO");
		e.setPrettyPrinterCreator(() -> new SniperJavaPrettyPrinter(e));

		launcher.addInputResource(INPUT_PATH.resolve(path).toString());
		launcher.setSourceOutputDirectory(OUTPUT_PATH.toString());

		CtModel model = launcher.buildModel();

		CtClass ctClass = model.getElements(new TypeFilter<>(CtClass.class)).get(0);

		ctClass.addComment(launcher.getFactory().Code().createComment("test", CommentType.BLOCK));

		launcher.process();
		launcher.prettyprint();
		// Verify result file exist and is not empty
		assertThat(OUTPUT_PATH.resolve(path))
				.withFailMessage("Output file for %s should exist", path)
				.exists();

		String content = Files.readString(OUTPUT_PATH.resolve(path));

		assertThat(content).isNotNull();
		assertThat(content)
				.withFailMessage("Result class should not be empty")
				.isNotBlank();
	}
}
