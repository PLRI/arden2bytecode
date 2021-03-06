package arden.tests.specification.testcompiler;

import static org.junit.Assume.assumeTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * JUnit rule to check all tests for the <code>@Compatibility</code> annotation
 * and skip tests for compilers which do not support the given version. <br>
 * Usage:
 * 
 * <pre>
 * &#064;Rule
 * public CompatibilityRule rule = new CompatibilityRule(settings);
 * 
 * &#064;Test
 * &#064;Compatibility(ArdenVersion.V1)
 * public void test() {
 * }
 * </pre>
 */
public class CompatibilityRule implements MethodRule {

	private TestCompilerSettings settings;
	private ArdenVersion currentTestMaxVersion = MAX_VERSION;
	private ArdenVersion currentTestMinVersion = MIN_VERSION;

	private static final ArdenVersion MIN_VERSION = getAnnotationDefaultValue("min");
	private static final ArdenVersion MAX_VERSION = getAnnotationDefaultValue("max");

	public CompatibilityRule(TestCompilerSettings settings) {
		this.settings = settings;
	}

	@Override
	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		Statement result = base;

		Compatibility annotation = method.getAnnotation(Compatibility.class);
		if (annotation != null) {
			currentTestMinVersion = annotation.min();
			currentTestMaxVersion = annotation.max();
			if (annotation.pedantic() && !settings.runPedanticTests) {
				String message = "Pedantic tests are not enabled.";
				result = new IgnoreStatement(message);
			} else if (settings.lowestVersion.ordinal() > currentTestMaxVersion.ordinal()) {
				String message = "Compiler is not backwards compatible to version: " + currentTestMaxVersion.toString();
				result = new IgnoreStatement(message);
			} else if (settings.targetVersion.ordinal() < currentTestMinVersion.ordinal()) {
				String message = "Compiler doesn't support newer version: " + currentTestMinVersion.toString();
				result = new IgnoreStatement(message);
			}
		} else {
			currentTestMinVersion = MIN_VERSION;
			currentTestMaxVersion = MAX_VERSION;
		}

		return result;
	}

	/**
	 * @return See {@link Compatibility#min()}
	 */
	public ArdenVersion getCurrentTestMinVersion() {
		return currentTestMinVersion;
	}

	/**
	 * @return See {@link Compatibility#max()}
	 */
	public ArdenVersion getCurrentTestMaxVersion() {
		return currentTestMaxVersion;
	}

	private static ArdenVersion getAnnotationDefaultValue(String methodName) throws ExceptionInInitializerError {
		// easiest way to reuse the annotations default values
		try {
			Method method = Compatibility.class.getMethod(methodName, (Class[]) null);
			return (ArdenVersion) method.getDefaultValue();
		} catch (NoSuchMethodException | SecurityException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	/**
	 * Use this to specify the required Arden Syntax version to run a test
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	public @interface Compatibility {
		/**
		 * The Arden Syntax version, when the tested feature was introduced to
		 * the language.
		 */
		ArdenVersion min() default ArdenVersion.V1;

		/**
		 * The last Arden Syntax version, before the tested feature was
		 * deprecated or removed.
		 */
		ArdenVersion max() default ArdenVersion.V2_10;

		/**
		 * Pedantic tests require that the compiler <b>restricts</b> usage of
		 * features in some way, instead of being more permissive. This forces
		 * that MLMs written with this compiler are guaranteed to be compilable
		 * with other standard compliant compilers. <br>
		 * For example pedantic tests check with regard to the used Arden Syntax
		 * version (as specified in the "arden:" slot), ignoring backward and
		 * forward compatibility.
		 * <p>
		 * <b>Backward compatibility / Deprecation:</b><br>
		 * E.g. the citations and links slot structured format is tested,
		 * ignoring the deprecated free-form text format. <br>
		 * </p>
		 * <p>
		 * <b>Forward compatibility:</b><br>
		 * There are tests, that check whether features introduced in later
		 * versions are not usable with the current version. <br>
		 * For example it should not be possible to use fuzzy operators in an
		 * MLM with <code>"arden: version 2.5"</code> as fuzzy logic requires
		 * Arden Syntax version 2.9 or greater.<br>
		 * </p>
		 */
		boolean pedantic() default false;
	}

	private static class IgnoreStatement extends Statement {
		private final String message;

		private IgnoreStatement(String message) {
			this.message = message;
		}

		@Override
		public void evaluate() {
			// skip test
			assumeTrue(message, false);
		}
	}

}
